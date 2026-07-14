package com.logistics.controller;

import com.logistics.dto.ClockTickRequest;
import com.logistics.service.SystemService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/")
    public String health() {
        return "OK";
    }

    @PostMapping("/system/reset")
    public Map<String, String> reset() {
        systemService.reset();
        return Map.of("status", "reset");
    }

    @GetMapping("/system/clock")
    public Map<String, Integer> getClock() {
        return Map.of("now", systemService.getClock());
    }

    @PostMapping("/system/clock/tick")
    public Map<String, Integer> tick(@RequestBody ClockTickRequest request) {
        int newClock = systemService.tickClock(request.getSteps() == null ? 1 : request.getSteps());
        return Map.of("now", newClock);
    }
}