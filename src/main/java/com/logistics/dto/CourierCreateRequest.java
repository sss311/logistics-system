package com.logistics.dto;

import lombok.Data;

@Data
public class CourierCreateRequest {
    private String id;
    private String stationId;
}