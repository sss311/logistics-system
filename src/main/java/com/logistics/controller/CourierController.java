package com.logistics.controller;

import com.logistics.dto.CourierCreateRequest;
import com.logistics.model.Courier;
import com.logistics.service.CourierService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/couriers")
public class CourierController {

    private final CourierService courierService;

    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    @PostMapping
    public Courier createCourier(@RequestBody CourierCreateRequest request) {
        return courierService.createCourier(request.getId(), request.getStationId());
    }

    @GetMapping
    public List<Courier> getAllCouriers() {
        return courierService.getAll();
    }

    @GetMapping("/{id}")
    public Courier getCourierById(@PathVariable String id) {
        return courierService.getById(id);
    }

    @PostMapping("/{id}/approve")
    public Courier approve(@PathVariable String id) {
        return courierService.approve(id);
    }

    @PostMapping("/{id}/suspend")
    public Courier suspend(@PathVariable String id) {
        return courierService.suspend(id);
    }

    @PostMapping("/{id}/reassign")
    public Courier reassign(@PathVariable String id, @RequestBody CourierCreateRequest request) {
        return courierService.reassign(id, request.getStationId());
    }
}