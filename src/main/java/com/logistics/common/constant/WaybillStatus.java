package com.logistics.common.constant;

public final class WaybillStatus {
    private WaybillStatus() {}

    public static final String CREATED = "已创建";
    public static final String IN_TRANSIT = "运输中";
    public static final String DELIVERY_STAGE = "派送阶段";
    public static final String COMPLETED = "已完成";
    public static final String REVERSE_RETURN = "逆向退回";
    public static final String CLAIM_FROZEN = "理赔冻结";
    public static final String CANCELLED = "已作废";
    public static final String SETTLED = "已结算";
}