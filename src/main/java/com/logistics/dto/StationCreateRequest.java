package com.logistics.dto;

import lombok.Data;

@Data
public class StationCreateRequest {
    private String id;
    private String name;
    private Integer capacity;
}