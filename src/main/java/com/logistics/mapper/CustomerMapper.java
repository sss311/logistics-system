package com.logistics.mapper;

import com.logistics.model.Customer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CustomerMapper {

    @Select("SELECT * FROM customers")
    @Results({
            @Result(column = "credit_level", property = "creditLevel"),
            @Result(column = "cod_balance", property = "codBalance")
    })
    List<Customer> selectAll();

    @Select("SELECT * FROM customers WHERE id = #{id}")
    @Results({
            @Result(column = "credit_level", property = "creditLevel"),
            @Result(column = "cod_balance", property = "codBalance")
    })
    Customer selectById(String id);

    @Insert("INSERT INTO customers (id, credit_level, cod_balance, status) " +
            "VALUES (#{id}, #{creditLevel}, #{codBalance}, #{status})")
    void insert(Customer customer);

    @Update("UPDATE customers SET credit_level = #{creditLevel} WHERE id = #{id}")
    void updateCreditLevel(@Param("id") String id, @Param("creditLevel") String creditLevel);

    @Update("UPDATE customers SET cod_balance = #{codBalance} WHERE id = #{id}")
    void updateCodBalance(@Param("id") String id, @Param("codBalance") Integer codBalance);

    @Update("UPDATE customers SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") String status);

    @Update("UPDATE couriers SET fail_history = #{failHistory} WHERE id = #{id}")
    void updateFailHistory(@Param("id") String id, @Param("failHistory") String failHistory);
}