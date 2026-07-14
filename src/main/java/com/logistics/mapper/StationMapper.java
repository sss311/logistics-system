package com.logistics.mapper;

import com.logistics.model.Station;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface StationMapper {

    @Select("SELECT * FROM stations")
    @Results({
            @Result(column = "in_stock", property = "inStock"),
            @Result(column = "fault_count", property = "faultCount")
    })
    List<Station> selectAll();

    @Select("SELECT * FROM stations WHERE id = #{id}")
    @Results({
            @Result(column = "in_stock", property = "inStock"),
            @Result(column = "fault_count", property = "faultCount")
    })
    Station selectById(String id);

    @Insert("INSERT INTO stations (id, name, capacity, in_stock, fault_count, status) " +
            "VALUES (#{id}, #{name}, #{capacity}, #{inStock}, #{faultCount}, #{status})")
    void insert(Station station);

    @Update("UPDATE stations SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") String status);

    @Update("UPDATE stations SET fault_count = #{faultCount} WHERE id = #{id}")
    void updateFaultCount(@Param("id") String id, @Param("faultCount") int faultCount);

    @Update("UPDATE stations SET in_stock = #{inStock} WHERE id = #{id}")
    void updateInStock(@Param("id") String id, @Param("inStock") Integer inStock);

    @Update("UPDATE stations SET fault_count = #{faultCount}, status = #{status} WHERE id = #{id}")
    void clearWarning(@Param("id") String id, @Param("faultCount") Integer faultCount, @Param("status") String status);
}