package com.logistics.common.constant;

public final class ParcelStatus {
    private ParcelStatus() {}

    public static final String PENDING_PICKUP = "待揽收";
    public static final String PICKED_UP = "已揽收";
    public static final String SORTING = "分拣中";
    public static final String IN_TRANSIT = "运输中";
    public static final String PENDING_DELIVERY = "待派送";
    public static final String DELIVERING = "派送中";
    public static final String SIGNED = "已签收";
    public static final String REJECTED = "已拒收";
    public static final String RETURNING = "退回中";
    public static final String RETURNED = "已退回";
    public static final String CLAIM_CLOSED = "已理赔关闭";
}