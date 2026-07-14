package com.logistics.service.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.constant.*;
import com.logistics.mapper.*;
import com.logistics.model.*;
import com.logistics.service.SystemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@Service
public class SystemServiceImpl implements SystemService {

    private final DataSource dataSource;
    private final SystemMapper systemMapper;
    private final ParcelMapper parcelMapper;
    private final WaybillMapper waybillMapper;
    private final StationMapper stationMapper;
    private final CourierMapper courierMapper;
    private final ClaimMapper claimMapper;

    public SystemServiceImpl(DataSource dataSource,
                             SystemMapper systemMapper,
                             ParcelMapper parcelMapper,
                             WaybillMapper waybillMapper,
                             StationMapper stationMapper,
                             CourierMapper courierMapper,
                             ClaimMapper claimMapper) {
        this.dataSource = dataSource;
        this.systemMapper = systemMapper;
        this.parcelMapper = parcelMapper;
        this.waybillMapper = waybillMapper;
        this.stationMapper = stationMapper;
        this.courierMapper = courierMapper;
        this.claimMapper = claimMapper;
    }

    @Override
    @Transactional
    public void reset() {
        // 清空所有业务数据（不删除表结构）
        String[] truncateSqls = {
                "DELETE FROM events",
                "DELETE FROM cod_records",
                "DELETE FROM claims",
                "DELETE FROM couriers",
                "DELETE FROM customers",
                "DELETE FROM stations",
                "DELETE FROM waybills",
                "DELETE FROM parcels",
                "DELETE FROM system_config"
        };

        // 种子数据：和之前完全一致
        String[] seedSqls = {
                "INSERT INTO system_config (`key`, `value`) VALUES ('clock', '0')",
                "INSERT INTO stations (id, name, capacity, in_stock, fault_count, status) VALUES ('ST_HUB', '分拣中心', 100, 0, 0, '正常')",
                "INSERT INTO stations (id, name, capacity, in_stock, fault_count, status) VALUES ('ST_PICK', '揽收网点', 50, 0, 0, '正常')",
                "INSERT INTO stations (id, name, capacity, in_stock, fault_count, status) VALUES ('ST_TRANS', '中转站', 80, 0, 0, '正常')",
                "INSERT INTO stations (id, name, capacity, in_stock, fault_count, status) VALUES ('ST_DELIV_A', '派送网点A', 20, 0, 0, '正常')",
                "INSERT INTO stations (id, name, capacity, in_stock, fault_count, status) VALUES ('ST_DELIV_B', '派送网点B', 20, 0, 0, '正常')",
                "INSERT INTO couriers (id, station_id, daily_quota, active_count, status, fail_history) VALUES ('CR_1', 'ST_DELIV_A', 5, 0, '空闲', '[]')",
                "INSERT INTO couriers (id, station_id, daily_quota, active_count, status, fail_history) VALUES ('CR_2', 'ST_DELIV_A', 5, 0, '空闲', '[]')",
                "INSERT INTO couriers (id, station_id, daily_quota, active_count, status, fail_history) VALUES ('CR_3', 'ST_DELIV_B', 5, 0, '空闲', '[]')",
                "INSERT INTO customers (id, credit_level, cod_balance, status) VALUES ('CUS_GOLD', '金', 0, '正常')",
                "INSERT INTO customers (id, credit_level, cod_balance, status) VALUES ('CUS_SILVER', '银', 0, '正常')",
                "INSERT INTO customers (id, credit_level, cod_balance, status) VALUES ('CUS_BRONZE', '铜', 0, '正常')"
        };

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : truncateSqls) {
                stmt.execute(sql);
            }
            for (String sql : seedSqls) {
                stmt.execute(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();   // ← 加这行
            throw new RuntimeException("重置数据库失败", e);
        }
    }

    @Override
    public int getClock() {
        String value = systemMapper.selectClock();
        return value != null ? Integer.parseInt(value) : 0;
    }

    @Override
    @Transactional
    public int tickClock(int steps) {
        int oldClock = getClock();
        int newClock = oldClock + steps;
        systemMapper.upsertClock(String.valueOf(newClock));

        // ========== 高级规则检查 ==========

        // 规则1：网点滞留 24 小时自动推进
        checkStationStagnation(newClock);

        // 规则2：快递员 6 小时内失败 4 次 → 异常审查
        checkCourierAbnormalReview(newClock);

        // 规则3：理赔受理 48 小时未定损 → 自动按全额定损
        checkClaimAutoAssess(newClock);

        // 规则4：网点 fault_count ≥ 3 → 质量预警
        checkStationQualityWarning();

        return newClock;
    }

    // ========== 高级规则私有方法 ==========

    /**
     * 规则1：网点滞留 24 小时自动推进
     * 包裹在某网点滞留时长（当前时间 - last_station_time）≥ 24 小时
     * → 自动推进到运单路由的下一站
     */
    private void checkStationStagnation(int now) {
        List<Parcel> parcels = parcelMapper.selectAll();
        for (Parcel parcel : parcels) {
            // 跳过终态和待揽收
            if (ParcelStatus.SIGNED.equals(parcel.getStatus()) ||
                    ParcelStatus.RETURNED.equals(parcel.getStatus()) ||
                    ParcelStatus.CLAIM_CLOSED.equals(parcel.getStatus()) ||
                    ParcelStatus.PENDING_PICKUP.equals(parcel.getStatus())) {
                continue;
            }
            // 使用 lastStationClock 判断滞留
            int lastClock = parcel.getLastStationClock() != null ? parcel.getLastStationClock() : 0;
            if (now - lastClock >= BusinessConstants.STATION_STAGNATION_HOURS) {
                Waybill waybill = waybillMapper.selectByParcelId(parcel.getId());
                if (waybill != null && !WaybillStatus.CLAIM_FROZEN.equals(waybill.getStatus())) {
                    autoAdvance(parcel, waybill, now);
                }
            }
        }
    }

    /**
     * 自动推进：和 ParcelServiceImpl.advance 逻辑一致
     */
    private void autoAdvance(Parcel parcel, Waybill waybill, int now) {
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
            return;
        }

        String nextStation = stations[currentIndex + 1].trim();
        String oldStation = currentStation;

        // 更新包裹状态
        String currentStatus = parcel.getStatus();
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

        // ========== 时间字段更新（放在最后，只更新一次） ==========
        parcel.setLastStationTime((int) (System.currentTimeMillis() / 1000));  // 真实时间
        parcel.setLastStationClock(now);  // 逻辑时钟
        parcelMapper.update(parcel);  // ← 只调用一次

        // 更新运单
        waybill.setCurrentStation(nextStation);
        if (ParcelStatus.RETURNED.equals(parcel.getStatus())) {
            waybill.setStatus(WaybillStatus.REVERSE_RETURN);
        } else if (ParcelStatus.PENDING_DELIVERY.equals(parcel.getStatus())) {
            waybill.setStatus(WaybillStatus.DELIVERY_STAGE);
        } else {
            waybill.setStatus(WaybillStatus.IN_TRANSIT);
        }
        waybillMapper.update(waybill);

        // 更新网点在仓量
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
    }

    /**
     * 规则2：快递员 6 小时内失败 4 次 → 异常审查
     */
    private void checkCourierAbnormalReview(int now) {
        List<Courier> couriers = courierMapper.selectAll();
        for (Courier courier : couriers) {
            // ... 状态检查 ...
            String failHistory = courier.getFailHistory();
            if (failHistory == null || failHistory.isBlank() || "[]".equals(failHistory)) {
                continue;
            }
            int failCount = 0;
            String[] timestamps = failHistory.replace("[", "").replace("]", "").split(",");
            for (String ts : timestamps) {
                try {
                    int failTime = Integer.parseInt(ts.trim());
                    // failTime 已经是逻辑时钟值，直接比较
                    if (now - failTime <= BusinessConstants.SUSPEND_WINDOW_HOURS) {
                        failCount++;
                    }
                } catch (NumberFormatException ignored) {}
            }
            // 达到阈值 → 异常审查
            if (failCount >= BusinessConstants.SUSPEND_FAIL_COUNT) {
                courierMapper.updateStatus(courier.getId(), CourierStatus.UNDER_REVIEW);
                courierMapper.updateActiveCount(courier.getId(), 0);

                // 收回在途包裹
                List<Parcel> courierParcels = parcelMapper.selectByCourier(courier.getId());
                for (Parcel p : courierParcels) {
                    if (ParcelStatus.DELIVERING.equals(p.getStatus())) {
                        p.setStatus(ParcelStatus.PENDING_DELIVERY);
                        p.setCourierId(null);
                        parcelMapper.update(p);
                    }
                }
            }
        }
    }

    /**
     * 规则3：理赔受理 48 小时未定损 → 自动按全损金额定损
     */
    private void checkClaimAutoAssess(int now) {
        List<Claim> claims = claimMapper.selectAll();
        for (Claim claim : claims) {
            if (!ClaimStatus.ACCEPTED.equals(claim.getStatus())) {
                continue;
            }

            int createdAt = claim.getCreatedAt() != null ? claim.getCreatedAt() : 0;
            if (now - createdAt >= BusinessConstants.CLAIM_AUTO_ASSESS_HOURS) {
                // 获取包裹的申报价值，按全损比例赔付
                Parcel parcel = parcelMapper.selectById(claim.getParcelId());
                if (parcel != null) {
                    int declaredValue = parcel.getDeclaredValue() != null ? parcel.getDeclaredValue() : 0;
                    int amount = (int) (declaredValue * BusinessConstants.DAMAGE_RATIO_TOTAL);
                    claimMapper.updateAmountAndStatus(claim.getId(), amount, ClaimStatus.ASSESSING);
                }
            }
        }
    }

    /**
     * 规则4：网点 fault_count ≥ 3 → 质量预警
     */
    private void checkStationQualityWarning() {
        List<Station> stations = stationMapper.selectAll();
        for (Station station : stations) {
            int faultCount = station.getFaultCount() != null ? station.getFaultCount() : 0;
            if (faultCount >= BusinessConstants.STATION_FAULT_THRESHOLD &&
                    StationStatus.NORMAL.equals(station.getStatus())) {
                stationMapper.updateStatus(station.getId(), StationStatus.QUALITY_WARNING);
            }
        }
    }
}