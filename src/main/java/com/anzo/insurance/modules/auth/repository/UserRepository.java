package com.anzo.insurance.modules.auth.repository;

import com.anzo.insurance.modules.auth.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
    
    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = false")
    Optional<User> findByUsername(@Param("username") String username);
    
    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM user WHERE username = #{username} AND deleted = false")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 根据手机号查找用户
     */
    @Select("SELECT * FROM user WHERE phone = #{phone} AND deleted = false")
    Optional<User> findByPhone(@Param("phone") String phone);
    
    /**
     * 根据企业ID查找用户
     */
    @Select("SELECT * FROM user WHERE enterprise_id = #{enterpriseId} AND deleted = false")
    Optional<User> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
    
    /**
     * 根据用户ID查找用户（包含企业信息）
     */
    @Select("SELECT u.*, e.name as enterprise_name, e.status as enterprise_status " +
            "FROM user u LEFT JOIN enterprise e ON u.enterprise_id = e.id " +
            "WHERE u.id = #{userId} AND u.deleted = false AND e.deleted = false")
    Optional<User> findUserWithEnterpriseById(@Param("userId") Long userId);
}
