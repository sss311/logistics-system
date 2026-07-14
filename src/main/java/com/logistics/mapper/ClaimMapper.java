package com.logistics.mapper;

import com.logistics.model.Claim;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClaimMapper {

    @Insert("INSERT INTO claims (id, parcel_id, amount, degree, status, created_at) " +
            "VALUES (#{id}, #{parcelId}, #{amount}, #{degree}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = false)
    void insert(Claim claim);

    @Select("SELECT * FROM claims WHERE id = #{id}")
    @Results({
            @Result(column = "parcel_id", property = "parcelId"),
            @Result(column = "created_at", property = "createdAt")
    })
    Claim selectById(String id);

    @Select("SELECT * FROM claims")
    @Results({
            @Result(column = "parcel_id", property = "parcelId"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Claim> selectAll();

    @Update("UPDATE claims SET amount = #{amount}, status = #{status} WHERE id = #{id}")
    void updateAmountAndStatus(@Param("id") String id, @Param("amount") int amount, @Param("status") String status);

    @Update("UPDATE claims SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") String status);
}