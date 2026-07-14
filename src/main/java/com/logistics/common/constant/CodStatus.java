package com.logistics.common.constant;

public final class CodStatus {
    private CodStatus() {}

    public static final String PENDING_RECEIPT = "待收款";
    public static final String RECEIVED = "已收款";
    public static final String RECONCILING = "对账中";
    public static final String SETTLED = "已回款";
    public static final String CLOSED = "已关闭";
}