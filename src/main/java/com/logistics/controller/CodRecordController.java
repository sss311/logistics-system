package com.logistics.controller;

import com.logistics.model.CodRecord;
import com.logistics.service.CodRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cod")
public class CodRecordController {

    private final CodRecordService codRecordService;

    public CodRecordController(CodRecordService codRecordService) {
        this.codRecordService = codRecordService;
    }

    @GetMapping
    public List<CodRecord> getAllCodRecords() {
        return codRecordService.getAll();
    }

    @GetMapping("/{id}")
    public CodRecord getCodRecordById(@PathVariable String id) {
        return codRecordService.getById(id);
    }

    @PostMapping("/{id}/reconcile")
    public Map<String, String> reconcile(@PathVariable String id) {
        codRecordService.updateStatus(id, "对账中");
        return Map.of("status", "reconciling");
    }

    @PostMapping("/{id}/settle")
    public Map<String, String> settle(@PathVariable String id) {
        codRecordService.updateStatus(id, "已回款");
        return Map.of("status", "settled");
    }
}