package com.logistics.service.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.constant.StationStatus;
import com.logistics.mapper.ParcelMapper;
import com.logistics.mapper.StationMapper;
import com.logistics.model.Parcel;
import com.logistics.model.Station;
import com.logistics.service.StationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StationServiceImpl implements StationService {

    private final StationMapper stationMapper;
    private final ParcelMapper parcelMapper;

    public StationServiceImpl(StationMapper stationMapper, ParcelMapper parcelMapper) {
        this.stationMapper = stationMapper;
        this.parcelMapper = parcelMapper;
    }

    @Override
    public Station createStation(String id, String name, Integer capacity) {
        if (id == null || id.isBlank()) {
            id = "ST_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        }
        if (name == null || name.isBlank()) {
            name = "未命名网点";
        }
        if (capacity == null) {
            capacity = 0;
        }

        Station station = new Station();
        station.setId(id);
        station.setName(name);
        station.setCapacity(capacity);
        station.setInStock(0);
        station.setFaultCount(0);
        station.setStatus(StationStatus.NORMAL);

        stationMapper.insert(station);
        return station;
    }

    @Override
    public Station getById(String id) {
        Station station = stationMapper.selectById(id);
        if (station == null) {
            throw new BusinessException(404, "Station not found");
        }
        return station;
    }

    @Override
    public List<Station> getAll() {
        return stationMapper.selectAll();
    }

    @Override
    public Station disable(String id) {
        Station station = getById(id);
        stationMapper.updateStatus(id, StationStatus.DISABLED);
        station.setStatus(StationStatus.DISABLED);
        return station;
    }

    @Override
    public Station enable(String id) {
        Station station = getById(id);
        stationMapper.updateStatus(id, StationStatus.NORMAL);
        station.setStatus(StationStatus.NORMAL);
        return station;
    }

    @Override
    public Station clearWarning(String id) {
        Station station = getById(id);
        stationMapper.clearWarning(id, 0, StationStatus.NORMAL);
        station.setFaultCount(0);
        station.setStatus(StationStatus.NORMAL);
        return station;
    }

    @Override
    public List<Parcel> getParcelsByStation(String id) {
        // 校验网点存在
        getById(id);
        return parcelMapper.selectByStation(id);
    }
}