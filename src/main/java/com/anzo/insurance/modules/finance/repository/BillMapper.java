package com.anzo.insurance.modules.finance.repository;

import com.anzo.insurance.modules.finance.dto.BillQueryDTO;
import com.anzo.insurance.modules.finance.entity.Bill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 账单Mapper接口
 */
@Mapper
public interface BillMapper extends BaseMapper<Bill> {

    /**
     * 根据账单编号查询
     */
    Bill selectByBillNo(@Param("billNo") String billNo);

    /**
     * 分页查询账单列表
     */
    List<Bill> selectBillPage(@Param("query") BillQueryDTO query);

    /**
     * 获取企业账单统计
     */
    Map<String, Object> selectEnterpriseBillStats(
        @Param("enterpriseId") Long enterpriseId,
        @Param("billingPeriod") String billingPeriod);

    /**
     * 按账单状态统计
     */
    List<Map<String, Object>> selectBillStatusStats(
        @Param("enterpriseId") Long enterpriseId,
        @Param("billingPeriod") String billingPeriod);
}