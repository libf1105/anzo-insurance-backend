package com.anzo.insurance.modules.finance.service;

import com.anzo.insurance.modules.finance.dto.TransactionRecordDTO;
import com.anzo.insurance.modules.finance.dto.TransactionQueryDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 交易记录服务接口
 */
public interface TransactionRecordService {

    /**
     * 获取交易记录详情
     */
    TransactionRecordDTO getTransactionRecord(Long id);

    /**
     * 分页查询交易记录
     */
    IPage<TransactionRecordDTO> queryTransactionPage(TransactionQueryDTO queryDTO);

    /**
     * 创建交易记录
     */
    TransactionRecordDTO createTransactionRecord(TransactionRecordDTO transactionRecordDTO);

    /**
     * 更新交易记录状态
     */
    void updateTransactionStatus(Long id, Integer status, String remark);

    /**
     * 根据业务ID查询交易记录
     */
    TransactionRecordDTO getTransactionByBusinessId(Long businessId);

    /**
     * 获取企业交易统计
     */
    java.util.Map<String, Object> getEnterpriseTransactionStats(Long enterpriseId, String startTime, String endTime);

    /**
     * 导出交易记录
     */
    void exportTransactions(TransactionQueryDTO queryDTO, jakarta.servlet.http.HttpServletResponse response);
}