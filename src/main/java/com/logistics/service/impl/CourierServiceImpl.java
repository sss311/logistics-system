package com.logistics.service.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.constant.CourierStatus;
import com.logistics.common.constant.BusinessConstants;
import com.logistics.mapper.CourierMapper;
import com.logistics.model.Courier;
import com.logistics.service.CourierService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CourierServiceImpl implements CourierService {

    private final CourierMapper courierMapper;

    public CourierServiceImpl(CourierMapper courierMapper) {
        this.courierMapper = courierMapper;
    }

    @Override
    public Courier createCourier(String id, String stationId) {
        if (stationId == null || stationId.isBlank()) {
            throw new BusinessException("station_id is required");
        }
        if (id == null || id.isBlank()) {
            id = "CR_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        }

        Courier courier = new Courier();
        courier.setId(id);
        courier.setStationId(stationId);
        courier.setDailyQuota(BusinessConstants.DAILY_QUOTA);
        courier.setActiveCount(0);
        courier.setStatus(CourierStatus.PENDING_REVIEW);
        courier.setFailHistory("[]");

        courierMapper.insert(courier);
        return courier;
    }

    @Override
    public Courier getById(String id) {
        Courier courier = courierMapper.selectById(id);
        if (courier == null) {
            throw new BusinessException(404, "Courier not found");
        }
        return courier;
    }

    @Override
    public List<Courier> getAll() {
        return courierMapper.selectAll();
    }

    @Override
    public Courier approve(String id) {
        Courier courier = getById(id);
        if (!CourierStatus.PENDING_REVIEW.equals(courier.getStatus())) {
            throw new BusinessException("Courier not in pending review status");
        }
        courierMapper.updateStatus(id, CourierStatus.IDLE);
        courier.setStatus(CourierStatus.IDLE);
        return courier;
    }



    @Override
    public Courier suspend(String id) {
        Courier courier = getById(id);
        courierMapper.updateStatus(id, CourierStatus.UNDER_REVIEW);
        // 收回在途件
        courierMapper.updateActiveCount(id, 0);
        courier.setStatus(CourierStatus.UNDER_REVIEW);
        courier.setActiveCount(0);
        return courier;
    }

    @Override
    public Courier reassign(String id, String stationId) {
        Courier courier = getById(id);
        if (stationId == null || stationId.isBlank()) {
            throw new BusinessException("station_id is required");
        }
        courierMapper.reassign(id, stationId, CourierStatus.PENDING_REASSIGN);
        courier.setStationId(stationId);
        courier.setStatus(CourierStatus.PENDING_REASSIGN);
        courier.setActiveCount(0);
        return courier;
    }
}