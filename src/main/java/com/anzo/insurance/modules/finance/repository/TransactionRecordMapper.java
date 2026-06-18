package com.anzo.insurance.modules.finance.repository;

import com.anzo.insurance.modules.finance.dto.TransactionQueryDTO;
import com.anzo.insurance.modules.finance.entity.TransactionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 交易记录Mapper接口
 */
@Mapper
public interface TransactionRecordMapper extends BaseMapper<TransactionRecord> {

    /**
     * 根据业务ID查询交易记录
     */
    TransactionRecord selectByRelatedBusinessId(@Param("businessId") String businessId);

    /**
     * 分页查询交易记录
     */
    List<TransactionRecord> selectTransactionPage(@Param("query") TransactionQueryDTO query);

    /**
     * 获取企业交易统计
     */
    Map<String, Object> selectEnterpriseTransactionStats(
        @Param("enterpriseId") String enterpriseId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime);

    /**
     * 按交易类型统计
     */
    List<Map<String, Object>> selectTransactionTypeStats(
        @Param("enterpriseId") String enterpriseId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime);
}