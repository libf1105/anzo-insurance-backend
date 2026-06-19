package com.anzo.insurance.modules.finance.service;

import com.anzo.insurance.modules.finance.dto.InvoiceApplyDTO;
import com.anzo.insurance.modules.finance.dto.InvoiceDTO;
import com.anzo.insurance.modules.finance.dto.InvoiceQueryDTO;
import com.anzo.insurance.modules.finance.dto.InvoiceUpdateDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 发票服务接口
 */
public interface InvoiceService {

    /**
     * 获取发票详情
     */
    InvoiceDTO getInvoice(Long id);

    /**
     * 分页查询发票列表
     */
    IPage<InvoiceDTO> queryInvoicePage(InvoiceQueryDTO queryDTO);

    /**
     * 申请发票
     */
    InvoiceDTO applyInvoice(InvoiceApplyDTO applyDTO);

    /**
     * 开票操作
     */
    InvoiceDTO issueInvoice(InvoiceUpdateDTO updateDTO);

    /**
     * 作废发票
     */
    InvoiceDTO voidInvoice(InvoiceUpdateDTO updateDTO);

    /**
     * 更新邮寄信息
     */
    InvoiceDTO updateShippingInfo(InvoiceUpdateDTO updateDTO);

    /**
     * 更新发票状态
     */
    void updateInvoiceStatus(Long id, Integer status, String remark);

    /**
     * 获取企业发票统计
     */
    java.util.Map<String, Object> getEnterpriseInvoiceStats(Long enterpriseId, String startTime, String endTime);

    /**
     * 批量开票
     */
    void batchIssueInvoices(java.util.List<Long> invoiceIds);

    /**
     * 下载发票文件
     */
    String downloadInvoiceFile(Long id);

    /**
     * 导出发票列表
     */
    void exportInvoices(InvoiceQueryDTO queryDTO, jakarta.servlet.http.HttpServletResponse response);

    /**
     * 查询可申请发票的账单列表
     */
    java.util.List<com.anzo.insurance.modules.finance.dto.BillDTO> queryAvailableBillsForInvoice(Long enterpriseId);
}