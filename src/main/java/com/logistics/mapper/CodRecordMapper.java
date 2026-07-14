package com.logistics.mapper;

import com.logistics.model.CodRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CodRecordMapper {

    @Insert("INSERT INTO cod_records (id, parcel_id, amount, status) " +
            "VALUES (#{id}, #{parcelId}, #{amount}, #{status})")
    @Options(useGeneratedKeys = false)
    void insert(CodRecord codRecord);

    @Select("SELECT * FROM cod_records WHERE id = #{id}")
    @Results({
            @Result(column = "parcel_id", property = "parcelId")
    })
    CodRecord selectById(String id);

    @Select("SELECT * FROM cod_records WHERE parcel_id = #{parcelId}")
    @Results({
            @Result(column = "parcel_id", property = "parcelId")
    })
    CodRecord selectByParcelId(String parcelId);

    @Select("SELECT * FROM cod_records")
    @Results({
            @Result(column = "parcel_id", property = "parcelId")
    })
    List<CodRecord> selectAll();

    @Update("UPDATE cod_records SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") String status);
}