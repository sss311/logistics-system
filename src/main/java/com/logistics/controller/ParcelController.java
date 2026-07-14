package com.logistics.controller;

import com.logistics.dto.ParcelCreateRequest;
import com.logistics.dto.ParcelPickupRequest;
import com.logistics.dto.ParcelSortRequest;
import com.logistics.model.Claim;
import com.logistics.model.Parcel;
import com.logistics.service.ParcelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parcels")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping
    public Parcel createParcel(@RequestBody ParcelCreateRequest request) {
        return parcelService.createParcel(
                request.getId(),
                request.getSender(),
                request.getReceiver(),
                request.getCategory(),
                request.getDeclaredValue(),
                request.getPriority(),
                request.getCod(),
                request.getCodAmount(),
                request.getAddress(),
                request.getDelivTarget()
        );
    }

    @GetMapping
    public List<Parcel> getAllParcels() {
        return parcelService.getAll();
    }

    @GetMapping("/{id}")
    public Parcel getParcelById(@PathVariable String id) {
        return parcelService.getById(id);
    }

    @PostMapping("/{id}/pickup")
    public Parcel pickup(@PathVariable String id, @RequestBody ParcelPickupRequest request) {
        return parcelService.pickup(id, request.getWaybillNo());
    }

    @PostMapping("/{id}/sort")
    public Parcel sort(@PathVariable String id, @RequestBody ParcelSortRequest request) {
        return parcelService.sort(id, request.getZone());
    }

    @PostMapping("/{id}/advance")
    public Parcel advance(@PathVariable String id) {
        return parcelService.advance(id);
    }

    @PostMapping("/{id}/assign-courier")
    public Parcel assignCourier(@PathVariable String id) {
        return parcelService.assignCourier(id);
    }

    @PostMapping("/{id}/deliver")
    public Parcel deliver(@PathVariable String id) {
        return parcelService.deliver(id);
    }

    @PostMapping("/{id}/sign")
    public Parcel sign(@PathVariable String id) {
        return parcelService.sign(id);
    }

    @PostMapping("/{id}/deliver-fail")
    public Parcel deliverFail(@PathVariable String id) {
        return parcelService.deliverFail(id);
    }

    @PostMapping("/{id}/reject")
    public Parcel reject(@PathVariable String id) {
        return parcelService.reject(id);
    }

    @PostMapping("/{id}/redirect")
    public Parcel redirect(@PathVariable String id, @RequestBody Map<String, String> request) {
        String delivTarget = request.get("deliv_target");
        return parcelService.redirect(id, delivTarget);
    }

    @PostMapping("/{id}/report-damage")
    public Claim reportDamage(@PathVariable String id, @RequestBody Map<String, String> request) {
        String degree = request.get("degree");
        return parcelService.reportDamage(id, degree);
    }
}