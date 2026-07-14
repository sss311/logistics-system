package com.logistics.service.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.constant.WaybillStatus;
import com.logistics.common.constant.BusinessConstants;
import com.logistics.mapper.WaybillMapper;
import com.logistics.model.Waybill;
import com.logistics.service.WaybillService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WaybillServiceImpl implements WaybillService {

    private final WaybillMapper waybillMapper;

    public WaybillServiceImpl(WaybillMapper waybillMapper) {
        this.waybillMapper = waybillMapper;
    }

    @Override
    public Waybill createWaybill(String parcelId, String delivTarget, int currentTime) {
        // 1. 生成运单ID
        String id = "W_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // 2. 构建路由段序列：ST_PICK → ST_HUB → ST_TRANS → deliv_target
        String routeLegs = "[\"ST_PICK\",\"ST_HUB\",\"ST_TRANS\",\"" + delivTarget + "\"]";

        // 3. 计算时效承诺：当前时间 + 路由段数 × 单段耗时
        int promisedAt = currentTime + 4 * BusinessConstants.ROUTE_TIME_PER_LEG;

        // 4. 构建运单对象
        Waybill waybill = new Waybill();
        waybill.setId(id);
        waybill.setParcelId(parcelId);
        waybill.setRouteLegs(routeLegs);
        waybill.setCurrentStation("ST_PICK");
        waybill.setDelivTarget(delivTarget);
        waybill.setPromisedAt(promisedAt);
        waybill.setStatus(WaybillStatus.CREATED);

        // 5. 入库
        waybillMapper.insert(waybill);
        return waybill;
    }

    @Override
    public Waybill getById(String id) {
        Waybill waybill = waybillMapper.selectById(id);
        if (waybill == null) {
            throw new BusinessException(404, "Waybill not found");
        }
        return waybill;
    }

    @Override
    public Waybill getByParcelId(String parcelId) {
        return waybillMapper.selectByParcelId(parcelId);
    }

    @Override
    public List<Waybill> getAll() {
        return waybillMapper.selectAll();
    }

    @Override
    public void updateStatus(String id, String status) {
        waybillMapper.updateStatus(id, status);
    }
}