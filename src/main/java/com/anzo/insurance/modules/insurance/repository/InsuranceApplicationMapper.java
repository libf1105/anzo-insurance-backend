package com.anzo.insurance.modules.insurance.repository;

import com.anzo.insurance.modules.insurance.entity.InsuranceApplication;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投保申请Mapper接口
 */
@Mapper
public interface InsuranceApplicationMapper extends BaseMapper<InsuranceApplication> {
}