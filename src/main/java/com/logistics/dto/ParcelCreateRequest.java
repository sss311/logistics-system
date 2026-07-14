package com.logistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ParcelCreateRequest {
    private String id;
    private String sender;
    private String receiver;
    private String category;
    private Integer declaredValue;
    private String priority;

    @JsonProperty("is_cod")
    private Boolean cod;

    @JsonProperty("cod_amount")
    private Integer codAmount;

    private String address;
    private String delivTarget;
}