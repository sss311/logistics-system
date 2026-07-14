package com.logistics.controller;

import com.logistics.dto.StationCreateRequest;
import com.logistics.model.Parcel;
import com.logistics.model.Station;
import com.logistics.service.StationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping
    public Station createStation(@RequestBody StationCreateRequest request) {
        return stationService.createStation(request.getId(), request.getName(), request.getCapacity());
    }

    @GetMapping
    public List<Station> getAllStations() {
        return stationService.getAll();
    }

    @GetMapping("/{id}")
    public Station getStationById(@PathVariable String id) {
        return stationService.getById(id);
    }

    @PostMapping("/{id}/disable")
    public Station disable(@PathVariable String id) {
        return stationService.disable(id);
    }

    @PostMapping("/{id}/enable")
    public Station enable(@PathVariable String id) {
        return stationService.enable(id);
    }

    @PostMapping("/{id}/clear-warning")
    public Station clearWarning(@PathVariable String id) {
        return stationService.clearWarning(id);
    }

    @GetMapping("/{id}/parcels")
    public List<Parcel> getParcelsByStation(@PathVariable String id) {
        return stationService.getParcelsByStation(id);
    }
}