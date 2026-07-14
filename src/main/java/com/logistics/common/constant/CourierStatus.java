package com.logistics.common.constant;

public final class CourierStatus {
    private CourierStatus() {}

    public static final String PENDING_REVIEW = "待审核";
    public static final String IDLE = "空闲";
    public static final String DELIVERING = "派送中";
    public static final String FULLY_LOADED = "已满载";
    public static final String UNDER_REVIEW = "异常审查";
    public static final String PENDING_REASSIGN = "待重分配";
}