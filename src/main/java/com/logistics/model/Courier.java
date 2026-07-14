package com.logistics.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Courier {
    private String id;
    private String stationId;
    private Integer dailyQuota;
    private Integer activeCount;
    private String status;
    private String failHistory;
}