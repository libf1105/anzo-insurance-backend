package com.anzo.insurance.modules.auth.repository;

import com.anzo.insurance.modules.auth.entity.Enterprise;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 企业数据访问接口
 */
@Mapper
public interface EnterpriseRepository extends BaseMapper<Enterprise> {
    
    /**
     * 根据信用代码查找企业
     */
    @Select("SELECT * FROM enterprise WHERE credit_code = #{creditCode} AND deleted = false")
    Optional<Enterprise> findByCreditCode(@Param("creditCode") String creditCode);
    
    /**
     * 检查信用代码是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM enterprise WHERE credit_code = #{creditCode} AND deleted = false")
    boolean existsByCreditCode(@Param("creditCode") String creditCode);
    
    /**
     * 根据企业名称查找企业
     */
    @Select("SELECT * FROM enterprise WHERE name = #{name} AND deleted = false")
    Optional<Enterprise> findByName(@Param("name") String name);
    
    /**
     * 根据状态查找企业
     */
    @Select("SELECT * FROM enterprise WHERE status = #{status} AND deleted = false ORDER BY created_at DESC")
    List<Enterprise> findByStatus(@Param("status") String status);
    
    /**
     * 根据联系人手机号查找企业
     */
    @Select("SELECT * FROM enterprise WHERE contact_phone = #{phone} AND deleted = false")
    Optional<Enterprise> findByContactPhone(@Param("phone") String phone);
    
    /**
     * 获取待审核的企业列表
     */
    @Select("SELECT * FROM enterprise WHERE status = 'PENDING_REVIEW' AND deleted = false ORDER BY created_at DESC")
    List<Enterprise> findPendingReviewEnterprises();
}