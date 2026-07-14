package com.logistics.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Station {
    private String id;
    private String name;
    private Integer capacity;
    private Integer inStock;
    private Integer faultCount;
    private String status;
}