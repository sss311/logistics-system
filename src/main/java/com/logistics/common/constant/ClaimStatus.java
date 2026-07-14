package com.logistics.common.constant;

public final class ClaimStatus {
    private ClaimStatus() {}

    public static final String ACCEPTED = "已受理";
    public static final String ASSESSING = "定损中";
    public static final String PAID = "已赔付";
    public static final String REJECTED = "已驳回";
    public static final String CLOSED = "已结案";
}