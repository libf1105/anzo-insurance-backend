package com.anzo.insurance.modules.finance.service;

import com.anzo.insurance.modules.finance.dto.BillDTO;
import com.anzo.insurance.modules.finance.dto.BillPayDTO;
import com.anzo.insurance.modules.finance.dto.BillQueryDTO;
import com.anzo.insurance.modules.finance.dto.BillReconcileDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 账单服务接口
 */
public interface BillService {

    /**
     * 获取账单详情
     */
    BillDTO getBill(String id);

    /**
     * 分页查询账单列表
     */
    IPage<BillDTO> queryBillPage(BillQueryDTO queryDTO);

    /**
     * 生成月度账单
     */
    BillDTO generateMonthlyBill(String enterpriseId, String billingPeriod);

    /**
     * 支付账单
     */
    BillDTO payBill(BillPayDTO billPayDTO);

    /**
     * 对账账单
     */
    BillDTO reconcileBill(BillReconcileDTO billReconcileDTO);

    /**
     * 更新账单状态
     */
    void updateBillStatus(String id, Integer status, String remark);

    /**
     * 获取企业账单统计
     */
    java.util.Map<String, Object> getEnterpriseBillStats(String enterpriseId, String billingPeriod);

    /**
     * 批量生成月度账单
     */
    void batchGenerateMonthlyBills(String billingPeriod);

    /**
     * 发送账单提醒
     */
    void sendBillReminder(String billId);

    /**
     * 下载账单文件
     */
    String downloadBillFile(String id);

    /**
     * 导出账单列表
     */
    void exportBills(BillQueryDTO queryDTO, jakarta.servlet.http.HttpServletResponse response);
}