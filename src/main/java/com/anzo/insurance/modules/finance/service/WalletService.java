package com.anzo.insurance.modules.finance.service;

import com.anzo.insurance.modules.finance.dto.WalletDTO;
import com.anzo.insurance.modules.finance.dto.WalletQueryDTO;
import com.anzo.insurance.modules.finance.dto.WalletUpdateDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 钱包服务接口
 */
public interface WalletService {

    /**
     * 获取钱包信息
     */
    WalletDTO getWallet(Long id);

    /**
     * 根据企业ID获取钱包信息
     */
    WalletDTO getWalletByEnterpriseId(Long enterpriseId);

    /**
     * 分页查询钱包列表
     */
    IPage<WalletDTO> queryWalletPage(WalletQueryDTO queryDTO);

    /**
     * 初始化企业钱包
     */
    WalletDTO initWallet(Long enterpriseId, String enterpriseName);

    /**
     * 更新钱包余额
     */
    WalletDTO updateWalletBalance(WalletUpdateDTO updateDTO);

    /**
     * 冻结金额
     */
    WalletDTO freezeAmount(Long walletId, java.math.BigDecimal amount, Long businessId, String businessDesc, String remark);

    /**
     * 解冻金额
     */
    WalletDTO unfreezeAmount(Long walletId, java.math.BigDecimal amount, Long businessId, String businessDesc, String remark);

    /**
     * 充值
     */
    WalletDTO recharge(Long walletId, java.math.BigDecimal amount, Integer paymentMethod, String paymentNo, String remark);

    /**
     * 扣款
     */
    WalletDTO deduct(Long walletId, java.math.BigDecimal amount, Long businessId, String businessDesc, String remark);

    /**
     * 退款
     */
    WalletDTO refund(Long walletId, java.math.BigDecimal amount, Long businessId, String businessDesc, String remark);
}