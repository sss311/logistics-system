package com.logistics.mapper;

import com.logistics.model.Parcel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ParcelMapper {

    @Insert("INSERT INTO parcels (id, sender, receiver, category, declared_value, priority, is_cod, cod_amount, address, status, current_station, zone, courier_id, waybill_id, created_at, fail_count, last_station_time, last_station_clock) " +
            "VALUES (#{id}, #{sender}, #{receiver}, #{category}, #{declaredValue}, #{priority}, #{cod}, #{codAmount}, #{address}, #{status}, #{currentStation}, #{zone}, #{courierId}, #{waybillId}, #{createdAt}, #{failCount}, #{lastStationTime}, #{lastStationClock})")
    void insert(Parcel parcel);

    @Select("SELECT * FROM parcels WHERE id = #{id}")
    @Results(id = "parcelResultMap", value = {
            @Result(column = "declared_value", property = "declaredValue"),
            @Result(column = "is_cod", property = "cod"),
            @Result(column = "cod_amount", property = "codAmount"),
            @Result(column = "current_station", property = "currentStation"),
            @Result(column = "courier_id", property = "courierId"),
            @Result(column = "waybill_id", property = "waybillId"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "fail_count", property = "failCount"),
            @Result(column = "last_station_time", property = "lastStationTime"),
            @Result(column = "last_station_clock", property = "lastStationClock")   // ← 新增
    })
    Parcel selectById(String id);

    @Select("SELECT * FROM parcels")
    @ResultMap("parcelResultMap")
    List<Parcel> selectAll();

    @Select("SELECT * FROM parcels WHERE current_station = #{stationId}")
    @ResultMap("parcelResultMap")
    List<Parcel> selectByStation(String stationId);

    @Select("SELECT * FROM parcels WHERE courier_id = #{courierId}")
    @ResultMap("parcelResultMap")
    List<Parcel> selectByCourier(String courierId);

    @Update("UPDATE parcels SET status = #{status}, current_station = #{currentStation}, " +
            "zone = #{zone}, courier_id = #{courierId}, waybill_id = #{waybillId}, " +
            "fail_count = #{failCount}, last_station_time = #{lastStationTime}, " +
            "last_station_clock = #{lastStationClock} WHERE id = #{id}")
    void update(Parcel parcel);

    @Delete("DELETE FROM parcels")
    void deleteAll();

    @Update("UPDATE parcels SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") String status);
}