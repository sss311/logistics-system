package com.logistics.service;

import com.logistics.model.CodRecord;
import java.util.List;

public interface CodRecordService {
    CodRecord getById(String id);
    List<CodRecord> getAll();
    void updateStatus(String id, String status);
}