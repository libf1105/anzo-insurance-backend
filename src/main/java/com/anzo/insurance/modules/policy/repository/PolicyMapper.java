package com.anzo.insurance.modules.policy.repository;

import com.anzo.insurance.modules.policy.entity.Policy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 保单Mapper接口
 */
@Mapper
public interface PolicyMapper extends BaseMapper<Policy> {
}