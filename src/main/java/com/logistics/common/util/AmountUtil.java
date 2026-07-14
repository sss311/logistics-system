package com.logistics.common.util;

public final class AmountUtil {

    private AmountUtil() {}

    /**
     * 按破损程度计算赔付金额（整数分）
     * @param declaredValue 申报价值（整数分）
     * @param degree 破损程度（轻微破损 / 严重破损 / 全损）
     * @return 赔付金额（整数分）
     */
    public static int calculateDamageAmount(int declaredValue, String degree) {
        double ratio;
        switch (degree) {
            case "轻微破损":
                ratio = 0.3;
                break;
            case "严重破损":
                ratio = 0.6;
                break;
            case "全损":
                ratio = 1.0;
                break;
            default:
                throw new IllegalArgumentException("非法破损程度: " + degree);
        }
        return (int) (declaredValue * ratio);
    }
}