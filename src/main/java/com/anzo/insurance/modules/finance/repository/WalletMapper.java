package com.anzo.insurance.modules.finance.repository;

import com.anzo.insurance.modules.finance.dto.WalletQueryDTO;
import com.anzo.insurance.modules.finance.entity.Wallet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包Mapper接口
 */
@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {

    /**
     * 根据企业ID查询钱包
     */
    Wallet selectByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    /**
     * 更新钱包余额
     */
    int updateBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 冻结金额
     */
    int freezeBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 解冻金额
     */
    int unfreezeBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 更新充值统计
     */
    int updateRechargeAmount(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 更新消费统计
     */
    int updateConsumptionAmount(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 更新退款统计
     */
    int updateRefundAmount(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 分页查询钱包列表
     */
    List<Wallet> selectWalletPage(@Param("query") WalletQueryDTO query);
}