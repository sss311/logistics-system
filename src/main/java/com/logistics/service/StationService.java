package com.logistics.service;

import com.logistics.model.Parcel;
import com.logistics.model.Station;
import java.util.List;

public interface StationService {
    Station createStation(String id, String name, Integer capacity);
    Station getById(String id);
    List<Station> getAll();
    Station disable(String id);
    Station enable(String id);
    Station clearWarning(String id);
    List<Parcel> getParcelsByStation(String id);
}