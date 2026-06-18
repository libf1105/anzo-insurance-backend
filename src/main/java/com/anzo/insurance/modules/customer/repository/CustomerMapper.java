package com.anzo.insurance.modules.customer.repository;

import com.anzo.insurance.modules.customer.entity.Customer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 客户Mapper接口
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    /**
     * 根据企业ID查找客户列表
     */
    @Select("SELECT * FROM customer WHERE enterprise_id = #{enterpriseId} AND deleted = false ORDER BY created_at DESC")
    List<Customer> findByEnterpriseId(@Param("enterpriseId") String enterpriseId);

    /**
     * 根据客户名称查找客户（支持模糊查询）
     */
    @Select("SELECT * FROM customer WHERE enterprise_id = #{enterpriseId} AND name LIKE CONCAT('%', #{name}, '%') AND deleted = false ORDER BY created_at DESC")
    List<Customer> findByName(@Param("enterpriseId") String enterpriseId, @Param("name") String name);

    /**
     * 根据信用代码查找客户
     */
    @Select("SELECT * FROM customer WHERE enterprise_id = #{enterpriseId} AND credit_code = #{creditCode} AND deleted = false")
    Optional<Customer> findByCreditCode(@Param("enterpriseId") String enterpriseId, @Param("creditCode") String creditCode);

    /**
     * 检查信用代码是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM customer WHERE enterprise_id = #{enterpriseId} AND credit_code = #{creditCode} AND deleted = false")
    boolean existsByCreditCode(@Param("enterpriseId") String enterpriseId, @Param("creditCode") String creditCode);

    /**
     * 根据状态查找客户
     */
    @Select("SELECT * FROM customer WHERE enterprise_id = #{enterpriseId} AND status = #{status} AND deleted = false ORDER BY created_at DESC")
    List<Customer> findByStatus(@Param("enterpriseId") String enterpriseId, @Param("status") String status);
}