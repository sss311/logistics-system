package com.logistics.common.constant;

public final class BusinessConstants {
    private BusinessConstants() {}

    /** 快递员日派件配额 */
    public static final int DAILY_QUOTA = 5;

    /** 派送失败累计次数达到阈值 → 自动退回 */
    public static final int FAIL_RETURN_THRESHOLD = 3;

    /** 网点滞留时长阈值（小时） */
    public static final int STATION_STAGNATION_HOURS = 24;

    /** 派送失败滑动窗口（小时） */
    public static final int SUSPEND_WINDOW_HOURS = 6;

    /** 滑动窗口内失败次数阈值 */
    public static final int SUSPEND_FAIL_COUNT = 4;

    /** 理赔受理后超时自动定损（小时） */
    public static final int CLAIM_AUTO_ASSESS_HOURS = 48;

    /** 网点责任预警阈值 */
    public static final int STATION_FAULT_THRESHOLD = 3;

    /** 轻微破损赔付比例 */
    public static final double DAMAGE_RATIO_MINOR = 0.3;

    /** 严重破损赔付比例 */
    public static final double DAMAGE_RATIO_MAJOR = 0.6;

    /** 全损赔付比例 */
    public static final double DAMAGE_RATIO_TOTAL = 1.0;

    /** 路由段时效承诺（小时/段） */
    public static final int ROUTE_TIME_PER_LEG = 6;
}