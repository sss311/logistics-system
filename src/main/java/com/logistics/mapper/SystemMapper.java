package com.logistics.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface SystemMapper {

    // ========== 系统时钟操作 ==========
    @Select("SELECT `value` FROM system_config WHERE `key`='clock'")
    String selectClock();

    @Update("INSERT INTO system_config (`key`, `value`) VALUES ('clock', #{value}) " +
            "ON DUPLICATE KEY UPDATE `value` = #{value}")
    void upsertClock(String value);

    // ========== 种子数据插入 ==========
    @Insert("INSERT INTO stations (id, name, capacity, in_stock, fault_count, status) " +
            "VALUES (#{id}, #{name}, #{capacity}, #{inStock}, #{faultCount}, #{status})")
    void insertStation(@Param("id") String id, @Param("name") String name,
                       @Param("capacity") int capacity, @Param("inStock") int inStock,
                       @Param("faultCount") int faultCount, @Param("status") String status);

    @Insert("INSERT INTO couriers (id, station_id, daily_quota, active_count, status, fail_history) " +
            "VALUES (#{id}, #{stationId}, #{dailyQuota}, #{activeCount}, #{status}, #{failHistory})")
    void insertCourier(@Param("id") String id, @Param("stationId") String stationId,
                       @Param("dailyQuota") int dailyQuota, @Param("activeCount") int activeCount,
                       @Param("status") String status, @Param("failHistory") String failHistory);

    @Insert("INSERT INTO customers (id, credit_level, cod_balance, status) " +
            "VALUES (#{id}, #{creditLevel}, #{codBalance}, #{status})")
    void insertCustomer(@Param("id") String id, @Param("creditLevel") String creditLevel,
                        @Param("codBalance") int codBalance, @Param("status") String status);

    // ========== 清空业务数据 ==========
    @Update("TRUNCATE TABLE events")
    void truncateEvents();

    @Update("TRUNCATE TABLE cod_records")
    void truncateCodRecords();

    @Update("TRUNCATE TABLE claims")
    void truncateClaims();

    @Update("TRUNCATE TABLE couriers")
    void truncateCouriers();

    @Update("TRUNCATE TABLE customers")
    void truncateCustomers();

    @Update("TRUNCATE TABLE stations")
    void truncateStations();

    @Update("TRUNCATE TABLE waybills")
    void truncateWaybills();

    @Update("TRUNCATE TABLE parcels")
    void truncateParcels();

    default void truncateAllTables() {
        truncateEvents();
        truncateCodRecords();
        truncateClaims();
        truncateCouriers();
        truncateCustomers();
        truncateStations();
        truncateWaybills();
        truncateParcels();
    }
}