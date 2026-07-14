package com.logistics.service;

import com.logistics.model.Courier;
import java.util.List;

public interface CourierService {
    Courier createCourier(String id, String stationId);
    Courier getById(String id);
    List<Courier> getAll();
    Courier approve(String id);
    Courier suspend(String id);
    Courier reassign(String id, String stationId);
}