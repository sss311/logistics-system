package com.logistics.service.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.constant.BusinessConstants;
import com.logistics.common.constant.ClaimStatus;
import com.logistics.mapper.ClaimMapper;
import com.logistics.mapper.ParcelMapper;
import com.logistics.mapper.StationMapper;
import com.logistics.model.Claim;
import com.logistics.model.Parcel;
import com.logistics.service.ClaimService;
import org.springframework.stereotype.Service;
import java.util.List;
import com.logistics.model.Station;
@Service
public class ClaimServiceImpl implements ClaimService {

    private final ClaimMapper claimMapper;
    private final ParcelMapper parcelMapper;
    private final StationMapper stationMapper;

    public ClaimServiceImpl(ClaimMapper claimMapper,
                            ParcelMapper parcelMapper,
                            StationMapper stationMapper) {
        this.claimMapper = claimMapper;
        this.parcelMapper = parcelMapper;
        this.stationMapper = stationMapper;
    }

    @Override
    public Claim getById(String id) {
        Claim claim = claimMapper.selectById(id);
        if (claim == null) {
            throw new BusinessException(404, "Claim not found");
        }
        return claim;
    }

    @Override
    public List<Claim> getAll() {
        return claimMapper.selectAll();
    }

    @Override
    public void updateStatus(String id, String status) {
        claimMapper.updateStatus(id, status);
    }

    @Override
    public Claim assess(String id, int amount) {
        Claim claim = getById(id);
        claimMapper.updateAmountAndStatus(id, amount, ClaimStatus.ASSESSING);
        // 获取关联的包裹
        Parcel parcel = parcelMapper.selectById(claim.getParcelId());
        if (parcel != null) {
            // 更新包裹状态为"已理赔关闭"
            String currentStatus = parcel.getStatus();
            if (currentStatus != null && !currentStatus.equals("已签收")
                    && !currentStatus.equals("已退回") && !currentStatus.equals("已理赔关闭")) {
                parcelMapper.updateStatus(parcel.getId(), "已理赔关闭");
            }

            // 增加网点责任计数
            String stationId = parcel.getCurrentStation();
            if (stationId != null && !stationId.isEmpty()) {
                Station station = stationMapper.selectById(stationId);
                if (station != null) {
                    int newFaultCount = station.getFaultCount() + 1;
                    stationMapper.updateFaultCount(stationId, newFaultCount);

                    // 检查是否触发质量预警
                    if (newFaultCount >= BusinessConstants.STATION_FAULT_THRESHOLD) {
                        stationMapper.updateStatus(stationId, "质量预警");
                    }
                }
            }
        }
        claim.setAmount(amount);
        claim.setStatus(ClaimStatus.ASSESSING);
        return claim;
    }

    @Override
    public Claim pay(String id) {
        Claim claim = getById(id);
        claimMapper.updateStatus(id, ClaimStatus.PAID);
        claim.setStatus(ClaimStatus.PAID);
        return claim;
    }

    @Override
    public Claim reject(String id) {
        Claim claim = getById(id);
        claimMapper.updateStatus(id, ClaimStatus.REJECTED);
        claim.setStatus(ClaimStatus.REJECTED);
        return claim;
    }
}