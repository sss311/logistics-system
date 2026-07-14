package com.logistics.service.impl;

import com.logistics.common.BusinessException;
import com.logistics.mapper.CodRecordMapper;
import com.logistics.model.CodRecord;
import com.logistics.service.CodRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodRecordServiceImpl implements CodRecordService {

    private final CodRecordMapper codRecordMapper;

    public CodRecordServiceImpl(CodRecordMapper codRecordMapper) {
        this.codRecordMapper = codRecordMapper;
    }

    @Override
    public CodRecord getById(String id) {
        CodRecord codRecord = codRecordMapper.selectById(id);
        if (codRecord == null) {
            throw new BusinessException(404, "COD record not found");
        }
        return codRecord;
    }

    @Override
    public List<CodRecord> getAll() {
        return codRecordMapper.selectAll();
    }

    @Override
    public void updateStatus(String id, String status) {
        codRecordMapper.updateStatus(id, status);
    }
}