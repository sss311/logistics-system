package com.logistics.service.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.constant.*;
import com.logistics.common.util.AddressUtil;
import com.logistics.mapper.*;
import com.logistics.model.*;
import com.logistics.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ParcelServiceImpl implements ParcelService {

    private final ParcelMapper parcelMapper;
    private final WaybillService waybillService;
    private final CustomerService customerService;
    private final WaybillMapper waybillMapper;
    private final StationMapper stationMapper;
    private final CourierMapper courierMapper;
    private final CodRecordMapper codRecordMapper;
    private final CustomerMapper customerMapper;
    private final ClaimMapper claimMapper;
    private final SystemService systemService;

    public ParcelServiceImpl(ParcelMapper parcelMapper,
                             WaybillService waybillService,
                             CustomerService customerService,
                             WaybillMapper waybillMapper,
                             StationMapper stationMapper,
                             CourierMapper courierMapper,
                             CodRecordMapper codRecordMapper,
                             CustomerMapper customerMapper,
                             ClaimMapper claimMapper,
                             SystemService systemService) {       // ← 参数列表中已有
        this.parcelMapper = parcelMapper;
        this.waybillService = waybillService;
        this.customerService = customerService;
        this.waybillMapper = waybillMapper;
        this.stationMapper = stationMapper;
        this.courierMapper = courierMapper;
        this.codRecordMapper = codRecordMapper;
        this.customerMapper = customerMapper;
        this.claimMapper = claimMapper;
        this.systemService = systemService;    // ← 补上这一行
    }
    // ========== 终态校验工具方法 ==========

    /**
     * 检查包裹是否处于终态（已签收 / 已退回 / 已理赔关闭），
     * 处于终态的包裹拒绝一切写操作。
     */
    private void checkFinalStatus(Parcel parcel) {
        if (ParcelStatus.SIGNED.equals(parcel.getStatus()) ||
                ParcelStatus.RETURNED.equals(parcel.getStatus()) ||
                ParcelStatus.CLAIM_CLOSED.equals(parcel.getStatus())) {
            throw new BusinessException("Parcel is in final state");
        }
    }

    // ========== 正向履约线 ==========

    @Override
    public Parcel createParcel(String id, String sender, String receiver, String category,
                               Integer declaredValue, String priority, Boolean cod,
                               Integer codAmount, String address, String delivTarget) {
        if (sender == null || sender.isBlank()) {
            throw new BusinessException("sender is required");
        }
        if (receiver == null || receiver.isBlank()) {
            throw new BusinessException("receiver is required");
        }

        Customer senderCustomer = customerService.getById(sender);
        if (senderCustomer == null) {
            throw new BusinessException(404, "Sender not found");
        }
        if (CustomerStatus.RESTRICTED.equals(senderCustomer.getStatus()) ||
                CustomerStatus.FROZEN.equals(senderCustomer.getStatus())) {
            throw new BusinessException(403, "Sender account restricted");
        }

        Customer receiverCustomer = customerService.getById(receiver);
        if (receiverCustomer == null) {
            throw new BusinessException(404, "Receiver not found");
        }

        address = AddressUtil.normalize(address);
        if (address == null) {
            throw new BusinessException("address is required");
        }

        if (category == null || category.isBlank()) {
            category = CategoryEnum.NORMAL;
        }
        if (priority == null || priority.isBlank()) {
            priority = PriorityEnum.NORMAL;
        }
        if (declaredValue == null) declaredValue = 0;
        if (cod == null) cod = false;
        if (codAmount == null) codAmount = 0;

        if (id == null || id.isBlank()) {
            id = "P_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        int now = (int) (System.currentTimeMillis() / 1000);
        Parcel parcel = new Parcel();
        parcel.setId(id);
        parcel.setSender(sender);
        parcel.setReceiver(receiver);
        parcel.setCategory(category);
        parcel.setDeclaredValue(declaredValue);
        parcel.setPriority(priority);
        parcel.setCod(cod);
        parcel.setCodAmount(codAmount);
        parcel.setAddress(address);
        parcel.setStatus(ParcelStatus.PENDING_PICKUP);
        parcel.setCreatedAt(now);
        parcel.setFailCount(0);
        parcel.setLastStationTime(0);
        parcel.setLastStationClock(0);   // 初始逻辑时钟为 0
        parcelMapper.insert(parcel);
        return parcel;
    }

    @Override
    @Transactional
    public Parcel pickup(String parcelId, String waybillNo) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new BusinessException(404, "Parcel not found");
        }
        checkFinalStatus(parcel);  // 终态校验

        if (!ParcelStatus.PENDING_PICKUP.equals(parcel.getStatus())) {
            throw new BusinessException("Invalid status for pickup");
        }

        int now = (int) (System.currentTimeMillis() / 1000);
        Waybill waybill = waybillService.createWaybill(parcelId, "ST_DELIV_A", now);
        if (waybillNo != null && !waybillNo.isBlank()) {
            waybill.setId(waybillNo);
        }
        parcel.setStatus(ParcelStatus.PICKED_UP);
        parcel.setCurrentStation("ST_PICK");
        parcel.setWaybillId(waybill.getId());
        parcel.setLastStationTime((int) (System.currentTimeMillis() / 1000));  // 真实时间戳
        parcel.setLastStationClock(systemService.getClock());                  // 逻辑时钟
        parcelMapper.update(parcel);

        return parcel;
    }

    @Override
    @Transactional
    public Parcel sort(String parcelId, String zone) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new BusinessException(404, "Parcel not found");
        }
        checkFinalStatus(parcel);  // 终态校验

        if (!ParcelStatus.PICKED_UP.equals(parcel.getStatus())) {
            throw new BusinessException("Invalid status for sort");
        }

        String expectedZone;
        switch (parcel.getCategory()) {
            case CategoryEnum.FRAGILE:
                expectedZone = "A";
                break;
            case CategoryEnum.COLD_CHAIN:
                expectedZone = "B";
                break;
            default:
                expectedZone = "C";
        }

        if (zone == null || zone.isBlank()) {
            zone = expectedZone;
        }

        if (!expectedZone.equals(zone)) {
            throw new BusinessException("Zone mismatch. Expected " + expectedZone + ", got " + zone);
        }

        int now = (int) (System.currentTimeMillis() / 1000);
        parcel.setStatus(ParcelStatus.SORTING);
        parcel.setCurrentStation("ST_HUB");
        parcel.setZone(zone);
        parcel.setLastStationTime((int) (System.currentTimeMillis() / 1000));  // 真实时间戳
        parcel.setLastStationClock(systemService.getClock());                  // 逻辑时钟
        parcelMapper.update(parcel);

        return parcel;
    }

    @Override
    @Transactional
    public Parcel advance(String parcelId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new BusinessException(404, "Parcel not found");
        }
        checkFinalStatus(parcel);  // 终态校验

        String currentStatus = parcel.getStatus();
        if (!ParcelStatus.SORTING.equals(currentStatus) &&
                !ParcelStatus.IN_TRANSIT.equals(currentStatus) &&
                !ParcelStatus.RETURNING.equals(currentStatus)) {
            throw new BusinessException("Invalid status for advance: " + currentStatus);
        }

        Waybill waybill = waybillService.getByParcelId(parcelId);
        if (waybill != null && WaybillStatus.CLAIM_FROZEN.equals(waybill.getStatus())) {
            throw new BusinessException("Parcel frozen due to claim");
        }

        String routeLegs = waybill.getRouteLegs();
        String currentStation = waybill.getCurrentStation();

        int currentIndex = -1;
        String[] stations = routeLegs.replace("[", "").replace("]", "").replace("\"", "").split(",");
        for (int i = 0; i < stations.length; i++) {
            if (stations[i].trim().equals(currentStation)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1 || currentIndex >= stations.length - 1) {
            throw new BusinessException("Already at destination or invalid route");
        }

        String nextStation = stations[currentIndex + 1].trim();
        String oldStation = currentStation;

        int now = (int) (System.currentTimeMillis() / 1000);
        if (ParcelStatus.RETURNING.equals(currentStatus)) {
            if ("ST_PICK".equals(nextStation)) {
                parcel.setStatus(ParcelStatus.RETURNED);
            } else {
                parcel.setStatus(ParcelStatus.RETURNING);
            }
        } else {
            String delivTarget = waybill.getDelivTarget();
            if (nextStation.equals(delivTarget)) {
                parcel.setStatus(ParcelStatus.PENDING_DELIVERY);
            } else if (ParcelStatus.SORTING.equals(currentStatus)) {
                parcel.setStatus(ParcelStatus.IN_TRANSIT);
            }
        }

        parcel.setCurrentStation(nextStation);
        parcel.setLastStationTime((int) (System.currentTimeMillis() / 1000));  // 真实时间戳
        parcel.setLastStationClock(systemService.getClock());                  // 逻辑时钟
        parcelMapper.update(parcel);

        waybill.setCurrentStation(nextStation);
        if (ParcelStatus.RETURNED.equals(parcel.getStatus())) {
            waybill.setStatus(WaybillStatus.REVERSE_RETURN);
        } else if (ParcelStatus.PENDING_DELIVERY.equals(parcel.getStatus())) {
            waybill.setStatus(WaybillStatus.DELIVERY_STAGE);
        } else {
            waybill.setStatus(WaybillStatus.IN_TRANSIT);
        }
        waybillMapper.update(waybill);

        Station oldStationObj = stationMapper.selectById(oldStation);
        if (oldStationObj != null && oldStationObj.getInStock() > 0) {
            stationMapper.updateInStock(oldStation, oldStationObj.getInStock() - 1);
        }
        Station nextStationObj = stationMapper.selectById(nextStation);
        if (nextStationObj != null) {
            int newInStock = (nextStationObj.getInStock() != null ? nextStationObj.getInStock() : 0) + 1;
            stationMapper.updateInStock(nextStation, newInStock);

            if (nextStationObj.getCapacity() != null && newInStock >= nextStationObj.getCapacity()) {
                stationMapper.updateStatus(nextStation, StationStatus.OVERFLOW);
            }
        }

        return parcel;
    }

    @Override
    @Transactional
    public Parcel assignCourier(String parcelId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new BusinessException(404, "Parcel not found");
        }
        checkFinalStatus(parcel);  // 终态校验

        if (!ParcelStatus.PENDING_DELIVERY.equals(parcel.getStatus())) {
            throw new BusinessException("Not ready for delivery");
        }

        List<Courier> availableCouriers = courierMapper.selectAvailable(parcel.getCurrentStation());
        if (availableCouriers.isEmpty()) {
            throw new BusinessException("No available courier");
        }

        Courier courier = availableCouriers.get(0);

        int newActiveCount = (courier.getActiveCount() != null ? courier.getActiveCount() : 0) + 1;
        courierMapper.updateActiveCount(courier.getId(), newActiveCount);

        if (newActiveCount >= courier.getDailyQuota()) {
            courierMapper.updateStatus(courier.getId(), CourierStatus.FULLY_LOADED);
        } else {
            courierMapper.updateStatus(courier.getId(), CourierStatus.DELIVERING);
        }

        parcel.setStatus(ParcelStatus.DELIVERING);
        parcel.setCourierId(courier.getId());
        parcelMapper.update(parcel);

        return parcel;
    }

    @Override
    @Transactional
    public Parcel deliver(String parcelId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new BusinessException(404, "Parcel not found");
        }
        checkFinalStatus(parcel);  // 终态校验

        if (!ParcelStatus.DELIVERING.equals(parcel.getStatus())) {
            throw new BusinessException("Invalid status for deliver");
        }

        parcel.setStatus(ParcelStatus.DELIVERING);
        parcelMapper.update(parcel);

        return parcel;
    }

    @Override
    @Transactional
    public Parcel sign(String parcelId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new BusinessException(404, "Parcel not found");
        }

        // 幂等处理：已签收直接返回
        if (ParcelStatus.SIGNED.equals(parcel.getStatus())) {
            return parcel;
        }

        if (!ParcelStatus.DELIVERING.equals(parcel.getStatus())) {
            throw new BusinessException("Invalid status for sign");
        }

        parcel.setStatus(ParcelStatus.SIGNED);
        parcel.setLastStationClock(systemService.getClock());   // ← 新增
        parcelMapper.update(parcel);

        if (parcel.getCourierId() != null && !parcel.getCourierId().isBlank()) {
            Courier courier = courierMapper.selectById(parcel.getCourierId());
            if (courier != null) {
                int newActiveCount = Math.max(0, (courier.getActiveCount() != null ? courier.getActiveCount() : 0) - 1);
                courierMapper.updateActiveCount(courier.getId(), newActiveCount);
                String courierNewStatus = newActiveCount >= courier.getDailyQuota() ? CourierStatus.FULLY_LOADED : CourierStatus.IDLE;
                courierMapper.updateStatus(courier.getId(), courierNewStatus);
            }
        }

        if (parcel.getCod() != null && parcel.getCod() && parcel.getCodAmount() != null && parcel.getCodAmount() > 0) {
            CodRecord codRecord = new CodRecord();
            codRecord.setId("COD_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
            codRecord.setParcelId(parcelId);
            codRecord.setAmount(parcel.getCodAmount());
            codRecord.setStatus(CodStatus.RECEIVED);
            codRecordMapper.insert(codRecord);

            Customer sender = customerService.getById(parcel.getSender());
            if (sender != null) {
                customerMapper.updateCodBalance(parcel.getSender(),
                        (sender.getCodBalance() != null ? sender.getCodBalance() : 0) + parcel.getCodAmount());
            }
        }

        Waybill waybill = waybillService.getByParcelId(parcelId);
        if (waybill != null) {
            waybillMapper.updateStatus(waybill.getId(), WaybillStatus.COMPLETED);
        }
        parcel.setLastStationTime((int) (System.currentTimeMillis() / 1000));  // 真实时间戳
        parcel.setLastStationClock(systemService.getClock());                  // 逻辑时钟
        return parcel;
    }

    // ========== 异常处理线（待实现） ==========

    /**
     * 收件人拒收，触发逆向退回流程。
     * 仅"派送中"状态可拒收。拒收后包裹状态进入"退回中"，构建逆向路由，运单状态变为"逆向退回"。
     * 如有 COD，关闭 COD 记录。
     */
    @Override
    @Transactional
    public Parcel reject(String parcelId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new BusinessException(404, "Parcel not found");
        }
        checkFinalStatus(parcel);

        if (!ParcelStatus.DELIVERING.equals(parcel.getStatus())) {
            throw new BusinessException("Not in delivery");
        }

        // 构建逆向路由（沿原正向已走段倒序）
        Waybill waybill = waybillService.getByParcelId(parcelId);
        if (waybill != null) {
            String routeLegs = waybill.getRouteLegs();
            String currentStation = waybill.getCurrentStation();
            String[] stations = routeLegs.replace("[", "").replace("]", "").replace("\"", "").split(",");

            int currentIndex = -1;
            for (int i = 0; i < stations.length; i++) {
                if (stations[i].trim().equals(currentStation)) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex != -1) {
                StringBuilder reverseRoute = new StringBuilder("[");
                for (int i = currentIndex; i >= 0; i--) {
                    reverseRoute.append("\"").append(stations[i].trim()).append("\"");
                    if (i > 0) reverseRoute.append(",");
                }
                reverseRoute.append("]");

                waybill.setRouteLegs(reverseRoute.toString());
                waybill.setStatus(WaybillStatus.REVERSE_RETURN);
                parcel.setLastStationTime((int) (System.currentTimeMillis() / 1000));  // 真实时间戳
                parcel.setLastStationClock(systemService.getClock());                  // 逻辑时钟
                waybillMapper.update(waybill);
            }
        }

        // 释放快递员
        if (parcel.getCourierId() != null && !parcel.getCourierId().isBlank()) {
            Courier courier = courierMapper.selectById(parcel.getCourierId());
            if (courier != null) {
                int newActiveCount = Math.max(0, (courier.getActiveCount() != null ? courier.getActiveCount() : 0) - 1);
                courierMapper.updateActiveCount(courier.getId(), newActiveCount);
                String courierNewStatus = newActiveCount >= courier.getDailyQuota() ? CourierStatus.FULLY_LOADED : CourierStatus.IDLE;
                courierMapper.updateStatus(courier.getId(), courierNewStatus);
            }
        }

        // 关闭 COD
        if (parcel.getCod() != null && parcel.getCod()) {
            CodRecord codRecord = codRecordMapper.selectByParcelId(parcelId);
            if (codRecord != null) {
                codRecordMapper.updateStatus(codRecord.getId(), CodStatus.CLOSED);
            }
        }

        parcel.setStatus(ParcelStatus.RETURNING);
        parcel.setCourierId(null);
        parcel.setLastStationTime((int) (System.currentTimeMillis() / 1000));  // 真实时间戳
        parcel.setLastStationClock(systemService.getClock());                  // 逻辑时钟
        parcelMapper.update(parcel);

        return parcel;
    }

    /**
     * 在途改址，重算后续路由。
     * 仅"运输中"或"待派送"状态可改址。派送中及之后阶段拒绝。
     * 请求体含 deliv_target，更新运单路由和包裹目标网点。
     */
    @Override
    @Transactional
    public Parcel redirect(String parcelId, String delivTarget) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new BusinessException(404, "Parcel not found");
        }
        checkFinalStatus(parcel);

        String currentStatus = parcel.getStatus();
        if (!ParcelStatus.IN_TRANSIT.equals(currentStatus) && !ParcelStatus.PENDING_DELIVERY.equals(currentStatus)) {
            throw new BusinessException("Redirect only allowed in transit or pending delivery");
        }

        if (delivTarget == null || delivTarget.isBlank()) {
            throw new BusinessException("deliv_target is required");
        }

        // 验证目标网点存在
        Station targetStation = stationMapper.selectById(delivTarget);
        if (targetStation == null) {
            throw new BusinessException(404, "Target station not found");
        }

        // 更新运单路由：从当前站点到新目标网点
        Waybill waybill = waybillService.getByParcelId(parcelId);
        if (waybill != null) {
            String newRoute = "[\"" + waybill.getCurrentStation() + "\",\"" + delivTarget + "\"]";
            waybill.setRouteLegs(newRoute);
            waybill.setDelivTarget(delivTarget);
            waybillMapper.update(waybill);
        }

        return parcel;
    }

    /**
     * 上报破损，创建理赔单。
     * 请求体含破损程度 degree（轻微破损/严重破损/全损）。
     * 创建理赔单后运单状态变为"理赔冻结"，期间包裹不可推进、改址、派单。
     */
    @Override
    @Transactional
    public Claim reportDamage(String parcelId, String degree) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new BusinessException(404, "Parcel not found");
        }
        checkFinalStatus(parcel);

        // 校验破损程度
        if (degree == null || (!"轻微破损".equals(degree) && !"严重破损".equals(degree) && !"全损".equals(degree))) {
            throw new BusinessException("Invalid damage degree");
        }

        // 创建理赔单
        String claimId = "CL_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        int now = (int) (System.currentTimeMillis() / 1000);

        Claim claim = new Claim();
        claim.setId(claimId);
        claim.setParcelId(parcelId);
        claim.setAmount(0);
        claim.setDegree(degree);
        claim.setStatus(ClaimStatus.ACCEPTED);
        claim.setCreatedAt(systemService.getClock());
        claimMapper.insert(claim);

        // 运单冻结
        Waybill waybill = waybillService.getByParcelId(parcelId);
        if (waybill != null) {
            waybillMapper.updateStatus(waybill.getId(), WaybillStatus.CLAIM_FROZEN);
        }

        return claim;
    }

    /**
     * 登记派送失败，累计失败次数。
     * 仅"派送中"状态可登记失败。失败次数达到 3 次自动触发退回流程。
     * 同时更新快递员失败历史记录，用于异常审查滑动窗口判断。
     */
    @Override
    @Transactional
    public Parcel deliverFail(String parcelId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new BusinessException(404, "Parcel not found");
        }
        checkFinalStatus(parcel);

        if (!ParcelStatus.DELIVERING.equals(parcel.getStatus())) {
            throw new BusinessException("Not in delivery");
        }

        int now = (int) (System.currentTimeMillis() / 1000);
        int failCount = (parcel.getFailCount() != null ? parcel.getFailCount() : 0) + 1;
        parcel.setFailCount(failCount);

        // 更新快递员失败历史 + 释放在途件数
        if (parcel.getCourierId() != null && !parcel.getCourierId().isBlank()) {
            Courier courier = courierMapper.selectById(parcel.getCourierId());
            if (courier != null) {
                int newActiveCount = Math.max(0, (courier.getActiveCount() != null ? courier.getActiveCount() : 0) - 1);
                courierMapper.updateActiveCount(courier.getId(), newActiveCount);
                String courierNewStatus = newActiveCount >= courier.getDailyQuota() ? CourierStatus.FULLY_LOADED : CourierStatus.IDLE;
                courierMapper.updateStatus(courier.getId(), courierNewStatus);

                // 追加失败时间戳到失败历史
                String failHistory = courier.getFailHistory(); // 必须有这一行
                int clock = systemService.getClock();  // 逻辑时钟值，如 0、1、2
                if (failHistory == null || failHistory.isBlank() || "[]".equals(failHistory)) {
                    failHistory = "[" + clock + "]";
                } else {
                    failHistory = failHistory.replace("]", "," + clock + "]");
                }
                courierMapper.updateFailHistory(courier.getId(), failHistory);
            }
        }

        // 累计失败达到 3 次 → 自动退回
        if (failCount >= BusinessConstants.FAIL_RETURN_THRESHOLD) {
            // 释放快递员
            if (parcel.getCourierId() != null && !parcel.getCourierId().isBlank()) {
                Courier courier = courierMapper.selectById(parcel.getCourierId());
                if (courier != null) {
                    int newActiveCount = Math.max(0, (courier.getActiveCount() != null ? courier.getActiveCount() : 0) - 1);
                    courierMapper.updateActiveCount(courier.getId(), newActiveCount);
                    String courierNewStatus = newActiveCount >= courier.getDailyQuota() ? CourierStatus.FULLY_LOADED : CourierStatus.IDLE;
                    courierMapper.updateStatus(courier.getId(), courierNewStatus);
                }
            }
            parcel.setCourierId(null);  // 清空快递员绑定

            // 构建逆向路由...
            parcel.setStatus(ParcelStatus.RETURNING);
            // 构建逆向路由
            Waybill waybill = waybillService.getByParcelId(parcelId);
            if (waybill != null) {
                String routeLegs = waybill.getRouteLegs();
                String currentStation = waybill.getCurrentStation();
                String[] stations = routeLegs.replace("[", "").replace("]", "").replace("\"", "").split(",");

                int currentIndex = -1;
                for (int i = 0; i < stations.length; i++) {
                    if (stations[i].trim().equals(currentStation)) {
                        currentIndex = i;
                        break;
                    }
                }

                if (currentIndex != -1) {
                    StringBuilder reverseRoute = new StringBuilder("[");
                    for (int i = currentIndex; i >= 0; i--) {
                        reverseRoute.append("\"").append(stations[i].trim()).append("\"");
                        if (i > 0) reverseRoute.append(",");
                    }
                    reverseRoute.append("]");

                    waybill.setRouteLegs(reverseRoute.toString());
                    waybill.setStatus(WaybillStatus.REVERSE_RETURN);
                    waybillMapper.update(waybill);
                }
            }

            parcel.setStatus(ParcelStatus.RETURNING);
        } else {
            // 未达退回阈值，重置为待派送
            parcel.setStatus(ParcelStatus.PENDING_DELIVERY);
            parcel.setCourierId(null);
        }
        parcel.setLastStationTime((int) (System.currentTimeMillis() / 1000));  // 真实时间戳
        parcel.setLastStationClock(systemService.getClock());// 逻辑时钟
        parcelMapper.update(parcel);
        return parcel;
    }

    /**
     * 更新包裹的 lastStationClock 字段为当前逻辑时钟
     */

    private void updateLastStationClock(Parcel parcel) {
        parcel.setLastStationClock(systemService.getClock());
    }
    // ========== 查询方法 ==========

    @Override
    public Parcel getById(String id) {
        Parcel parcel = parcelMapper.selectById(id);
        if (parcel == null) {
            throw new BusinessException(404, "Parcel not found");
        }
        return parcel;
    }

    @Override
    public List<Parcel> getAll() {
        return parcelMapper.selectAll();
    }
}