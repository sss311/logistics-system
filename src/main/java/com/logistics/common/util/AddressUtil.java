package com.logistics.common.util;

public final class AddressUtil {

    private AddressUtil() {}

    /**
     * 地址归一化：首尾去空格、内部连续空格折叠为单个空格
     * 如果归一化后为空字符串，返回 null（调用方需校验）
     */
    public static String normalize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        return raw.trim().replaceAll("\\s+", " ");
    }
}