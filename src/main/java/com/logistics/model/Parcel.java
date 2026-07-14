package com.logistics.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parcel {
    private String id;
    private String sender;
    private String receiver;
    private String category;
    private Integer declaredValue;
    private String priority;
    private Boolean cod;
    private Integer codAmount;
    private String address;
    private String status;
    private String currentStation;
    private String zone;
    private String courierId;
    private String waybillId;
    private Integer createdAt;
    private Integer failCount;
    private Integer lastStationTime;
    private Integer lastStationClock;   // ← 新增
}