package com.logistics.dto;

import lombok.Data;

@Data
public class CustomerCreateRequest {
    private String id;
    private String creditLevel;
}