package com.logistics.mapper;

import com.logistics.model.Courier;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CourierMapper {


    @Select("SELECT * FROM couriers")
    @Results({
            @Result(column = "station_id", property = "stationId"),
            @Result(column = "daily_quota", property = "dailyQuota"),
            @Result(column = "active_count", property = "activeCount"),
            @Result(column = "fail_history", property = "failHistory")
    })
    List<Courier> selectAll();

    @Select("SELECT * FROM couriers WHERE id = #{id}")
    @Results({
            @Result(column = "station_id", property = "stationId"),
            @Result(column = "daily_quota", property = "dailyQuota"),
            @Result(column = "active_count", property = "activeCount"),
            @Result(column = "fail_history", property = "failHistory")
    })
    Courier selectById(String id);

    @Select("SELECT * FROM couriers WHERE station_id = #{stationId} AND status IN ('空闲', '派送中') AND active_count < daily_quota")
    @Results({
            @Result(column = "station_id", property = "stationId"),
            @Result(column = "daily_quota", property = "dailyQuota"),
            @Result(column = "active_count", property = "activeCount"),
            @Result(column = "fail_history", property = "failHistory")
    })
    List<Courier> selectAvailable(String stationId);

    @Insert("INSERT INTO couriers (id, station_id, daily_quota, active_count, status, fail_history) " +
            "VALUES (#{id}, #{stationId}, #{dailyQuota}, #{activeCount}, #{status}, #{failHistory})")
    void insert(Courier courier);

    @Update("UPDATE couriers SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") String status);

    @Update("UPDATE couriers SET active_count = #{activeCount} WHERE id = #{id}")
    void updateActiveCount(@Param("id") String id, @Param("activeCount") Integer activeCount);

    @Update("UPDATE couriers SET station_id = #{stationId}, status = #{status}, active_count = 0 WHERE id = #{id}")
    void reassign(@Param("id") String id, @Param("stationId") String stationId, @Param("status") String status);

    @Update("UPDATE couriers SET fail_history = #{failHistory} WHERE id = #{id}")
    void updateFailHistory(@Param("id") String id, @Param("failHistory") String failHistory);
}