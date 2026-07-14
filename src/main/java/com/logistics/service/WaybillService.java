package com.logistics.service;

import com.logistics.model.Waybill;
import java.util.List;

public interface WaybillService {
    Waybill createWaybill(String parcelId, String delivTarget, int currentTime);
    Waybill getById(String id);
    Waybill getByParcelId(String parcelId);
    List<Waybill> getAll();
    void updateStatus(String id, String status);
}