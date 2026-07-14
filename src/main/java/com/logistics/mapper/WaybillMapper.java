package com.logistics.mapper;

import com.logistics.model.Waybill;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WaybillMapper {

    @Insert("INSERT INTO waybills (id, parcel_id, route_legs, current_station, deliv_target, promised_at, status) " +
            "VALUES (#{id}, #{parcelId}, #{routeLegs}, #{currentStation}, #{delivTarget}, #{promisedAt}, #{status})")
    void insert(Waybill waybill);

    @Select("SELECT * FROM waybills WHERE id = #{id}")
    @Results({
            @Result(column = "parcel_id", property = "parcelId"),
            @Result(column = "route_legs", property = "routeLegs"),
            @Result(column = "current_station", property = "currentStation"),
            @Result(column = "deliv_target", property = "delivTarget"),
            @Result(column = "promised_at", property = "promisedAt")
    })
    Waybill selectById(String id);

    @Select("SELECT * FROM waybills WHERE parcel_id = #{parcelId}")
    @Results({
            @Result(column = "parcel_id", property = "parcelId"),
            @Result(column = "route_legs", property = "routeLegs"),
            @Result(column = "current_station", property = "currentStation"),
            @Result(column = "deliv_target", property = "delivTarget"),
            @Result(column = "promised_at", property = "promisedAt")
    })
    Waybill selectByParcelId(String parcelId);

    @Select("SELECT * FROM waybills")
    @Results({
            @Result(column = "parcel_id", property = "parcelId"),
            @Result(column = "route_legs", property = "routeLegs"),
            @Result(column = "current_station", property = "currentStation"),
            @Result(column = "deliv_target", property = "delivTarget"),
            @Result(column = "promised_at", property = "promisedAt")
    })
    List<Waybill> selectAll();

    @Update("UPDATE waybills SET current_station = #{currentStation}, status = #{status}, " +
            "promised_at = #{promisedAt}, route_legs = #{routeLegs} WHERE id = #{id}")
    void update(Waybill waybill);

    @Update("UPDATE waybills SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") String status);
}