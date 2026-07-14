package com.logistics.controller;

import com.logistics.model.Waybill;
import com.logistics.service.WaybillService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/waybills")
public class WaybillController {

    private final WaybillService waybillService;

    public WaybillController(WaybillService waybillService) {
        this.waybillService = waybillService;
    }

    @GetMapping
    public List<Waybill> getAllWaybills() {
        return waybillService.getAll();
    }

    @GetMapping("/{id}")
    public Waybill getWaybillById(@PathVariable String id) {
        return waybillService.getById(id);
    }

    @GetMapping("/{id}/route")
    public Map<String, Object> getRoute(@PathVariable String id) {
        Waybill waybill = waybillService.getById(id);
        // 将 route_legs 从 JSON 字符串转为数组返回
        String routeLegs = waybill.getRouteLegs();
        return Map.of(
                "route_legs", routeLegs != null ? routeLegs : "[]",
                "current_station", waybill.getCurrentStation() != null ? waybill.getCurrentStation() : ""
        );
    }

    @PostMapping("/{id}/freeze")
    public Map<String, String> freeze(@PathVariable String id) {
        waybillService.updateStatus(id, "理赔冻结");
        return Map.of("status", "frozen");
    }

    @PostMapping("/{id}/settle")
    public Map<String, String> settle(@PathVariable String id) {
        waybillService.updateStatus(id, "已结算");
        return Map.of("status", "settled");
    }
}