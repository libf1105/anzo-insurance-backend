package com.anzo.insurance.modules.finance.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.modules.finance.dto.BillDTO;
import com.anzo.insurance.modules.finance.dto.BillPayDTO;
import com.anzo.insurance.modules.finance.dto.BillQueryDTO;
import com.anzo.insurance.modules.finance.dto.BillReconcileDTO;
import com.anzo.insurance.modules.finance.entity.Bill;
import com.anzo.insurance.modules.finance.entity.Wallet;
import com.anzo.insurance.modules.finance.repository.BillMapper;
import com.anzo.insurance.modules.finance.repository.WalletMapper;
import com.anzo.insurance.modules.finance.service.BillService;
import com.anzo.insurance.modules.finance.service.WalletService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账单服务实现类
 */
@Service
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill> implements BillService {

    @Resource
    private WalletMapper walletMapper;
    
    @Resource
    private WalletService walletService;

    @Override
    public BillDTO getBill(String id) {
        Bill bill = baseMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        return convertToDTO(bill);
    }

    @Override
    public IPage<BillDTO> queryBillPage(BillQueryDTO queryDTO) {
        Page<Bill> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Bill> queryWrapper = new LambdaQueryWrapper<>();

        if (isNotBlank(queryDTO.getEnterpriseId())) {
            queryWrapper.eq(Bill::getEnterpriseId, queryDTO.getEnterpriseId());
        }
        if (isNotBlank(queryDTO.getBillNo())) {
            queryWrapper.like(Bill::getBillNo, queryDTO.getBillNo());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Bill::getStatus, mapDtoStatusToEntity(queryDTO.getStatus()));
        }
        if (queryDTO.getReconciliationStatus() != null) {
            queryWrapper.eq(Bill::getReconciliationStatus, queryDTO.getReconciliationStatus());
        }
        if (isNotBlank(queryDTO.getBillingPeriod())) {
            YearMonth yearMonth = YearMonth.parse(queryDTO.getBillingPeriod(), DateTimeFormatter.ofPattern("yyyy-MM"));
            queryWrapper.ge(Bill::getPeriodStartDate, yearMonth.atDay(1));
            queryWrapper.le(Bill::getPeriodStartDate, yearMonth.atEndOfMonth());
        }
        if (queryDTO.getMinBillAmount() != null) {
            queryWrapper.ge(Bill::getTotalAmount, queryDTO.getMinBillAmount());
        }
        if (queryDTO.getMaxBillAmount() != null) {
            queryWrapper.le(Bill::getTotalAmount, queryDTO.getMaxBillAmount());
        }
        if (queryDTO.getMinPaidAmount() != null) {
            queryWrapper.ge(Bill::getPaidAmount, queryDTO.getMinPaidAmount());
        }
        if (queryDTO.getMaxPaidAmount() != null) {
            queryWrapper.le(Bill::getPaidAmount, queryDTO.getMaxPaidAmount());
        }
        if (queryDTO.getDueDateStart() != null && queryDTO.getDueDateEnd() != null) {
            queryWrapper.between(Bill::getDueDate, queryDTO.getDueDateStart(), queryDTO.getDueDateEnd());
        }
        if (queryDTO.getPaymentDateStart() != null && queryDTO.getPaymentDateEnd() != null) {
            queryWrapper.between(
                    Bill::getPaidTime,
                    queryDTO.getPaymentDateStart().atStartOfDay(),
                    queryDTO.getPaymentDateEnd().atTime(23, 59, 59)
            );
        }

        if ("billAmount".equals(queryDTO.getSortField())) {
            if ("asc".equals(queryDTO.getSortOrder())) {
                queryWrapper.orderByAsc(Bill::getTotalAmount);
            } else {
                queryWrapper.orderByDesc(Bill::getTotalAmount);
            }
        } else if ("dueDate".equals(queryDTO.getSortField())) {
            if ("asc".equals(queryDTO.getSortOrder())) {
                queryWrapper.orderByAsc(Bill::getDueDate);
            } else {
                queryWrapper.orderByDesc(Bill::getDueDate);
            }
        } else {
            queryWrapper.orderByDesc(Bill::getPeriodStartDate);
        }

        return baseMapper.selectPage(page, queryWrapper).convert(this::convertToDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillDTO generateMonthlyBill(String enterpriseId, String billingPeriod) {
        YearMonth yearMonth = YearMonth.parse(billingPeriod, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate periodStart = yearMonth.atDay(1);
        LocalDate periodEnd = yearMonth.atEndOfMonth();

        Bill existingBill = baseMapper.selectOne(new LambdaQueryWrapper<Bill>()
                .eq(Bill::getEnterpriseId, enterpriseId)
                .eq(Bill::getBillType, 1)
                .eq(Bill::getPeriodStartDate, periodStart)
                .eq(Bill::getPeriodEndDate, periodEnd));
        if (existingBill != null) {
            return convertToDTO(existingBill);
        }

        Wallet wallet = walletMapper.selectByEnterpriseId(enterpriseId);
        if (wallet == null) {
            throw new BusinessException("企业钱包不存在，无法生成账单");
        }

        BigDecimal billAmount = new BigDecimal("5000.00");
        Bill bill = new Bill();
        bill.setId(generateId());
        bill.setBillNo(generateBillNo());
        bill.setEnterpriseId(enterpriseId);
        bill.setBillType(1);
        bill.setPeriodStartDate(periodStart);
        bill.setPeriodEndDate(periodEnd);
        bill.setGenerationDate(LocalDate.now());
        bill.setTotalAmount(billAmount);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setDeductionAmount(BigDecimal.ZERO);
        bill.setReceivableAmount(billAmount);
        bill.setCurrency(wallet.getCurrency() == null ? "CNY" : wallet.getCurrency());
        bill.setStatus(1);
        bill.setDueDate(yearMonth.plusMonths(1).atDay(10));
        bill.setReconciliationStatus(0);
        bill.setRemark("系统自动生成月度账单");
        baseMapper.insert(bill);
        return convertToDTO(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillDTO payBill(BillPayDTO billPayDTO) {
        Bill bill = baseMapper.selectById(billPayDTO.getId());
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        
        if (bill.getStatus() != null && (bill.getStatus() == 4 || bill.getStatus() == 6)) {
            throw new BusinessException("当前账单状态不支持付款");
        }

        BigDecimal unpaidAmount = getUnpaidAmount(bill);
        if (billPayDTO.getPaymentAmount().compareTo(unpaidAmount) > 0) {
            throw new BusinessException("支付金额不能超过待付金额");
        }

        switch (billPayDTO.getPaymentMethod()) {
            case 1:
                Wallet wallet = walletMapper.selectByEnterpriseId(bill.getEnterpriseId());
                if (wallet == null) {
                    throw new BusinessException("企业钱包不存在");
                }
                walletService.deduct(
                        wallet.getId(),
                        billPayDTO.getPaymentAmount(),
                        bill.getId(), "账单支付-" + bill.getBillNo(), billPayDTO.getRemark());
                break;
            case 2:
            case 3:
                break;
            default:
                throw new BusinessException("不支持的支付方式");
        }

        bill.setPaidAmount(safe(bill.getPaidAmount()).add(billPayDTO.getPaymentAmount()));
        if (bill.getPaidAmount().compareTo(safe(bill.getReceivableAmount())) >= 0) {
            bill.setStatus(4);
            bill.setPaidTime(LocalDateTime.now());
        } else {
            bill.setStatus(3);
        }
        bill.setPaymentMethod(mapPaymentMethod(billPayDTO.getPaymentMethod()));
        bill.setPaymentNo(billPayDTO.getPaymentNo());
        appendRemark(bill, billPayDTO.getRemark());
        baseMapper.updateById(bill);
        return convertToDTO(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillDTO reconcileBill(BillReconcileDTO billReconcileDTO) {
        Bill bill = baseMapper.selectById(billReconcileDTO.getId());
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        
        if (!Boolean.TRUE.equals(bill.isPaid())) {
            throw new BusinessException("只有已付款的账单才能对账");
        }

        bill.setReconciliationStatus(1);
        bill.setReconciliationTime(LocalDateTime.now());
        bill.setReconciliationUserId("system");
        bill.setReconciliationUserName("系统管理员");
        if (billReconcileDTO.getReconciliationFileUrl() != null) {
            bill.setAttachmentUrl(billReconcileDTO.getReconciliationFileUrl());
        }
        appendRemark(bill, billReconcileDTO.getRemark());
        baseMapper.updateById(bill);
        return convertToDTO(bill);
    }

    @Override
    public void updateBillStatus(String id, Integer status, String remark) {
        Bill bill = baseMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        
        bill.setStatus(mapDtoStatusToEntity(status));
        appendRemark(bill, remark);
        baseMapper.updateById(bill);
    }

    @Override
    public Map<String, Object> getEnterpriseBillStats(String enterpriseId, String billingPeriod) {
        Map<String, Object> stats = new HashMap<>();
        LambdaQueryWrapper<Bill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Bill::getEnterpriseId, enterpriseId);

        if (isNotBlank(billingPeriod)) {
            YearMonth yearMonth = YearMonth.parse(billingPeriod, DateTimeFormatter.ofPattern("yyyy-MM"));
            queryWrapper.ge(Bill::getPeriodStartDate, yearMonth.atDay(1));
            queryWrapper.le(Bill::getPeriodStartDate, yearMonth.atEndOfMonth());
        }

        java.util.List<Bill> bills = baseMapper.selectList(queryWrapper);
        stats.put("totalCount", (long) bills.size());

        BigDecimal totalBillAmount = BigDecimal.ZERO;
        BigDecimal totalPaidAmount = BigDecimal.ZERO;
        Map<Integer, Map<String, Object>> statusStats = new HashMap<>();
        for (Bill bill : bills) {
            totalBillAmount = totalBillAmount.add(safe(bill.getTotalAmount()));
            totalPaidAmount = totalPaidAmount.add(safe(bill.getPaidAmount()));

            Integer status = mapEntityStatusToDto(bill.getStatus());
            Map<String, Object> statusStat = statusStats.computeIfAbsent(status, key -> {
                Map<String, Object> item = new HashMap<>();
                item.put("count", 0L);
                item.put("billAmount", BigDecimal.ZERO);
                item.put("paidAmount", BigDecimal.ZERO);
                item.put("statusName", getStatusName(key));
                return item;
            });
            statusStat.put("count", ((Long) statusStat.get("count")) + 1L);
            statusStat.put("billAmount", ((BigDecimal) statusStat.get("billAmount")).add(safe(bill.getTotalAmount())));
            statusStat.put("paidAmount", ((BigDecimal) statusStat.get("paidAmount")).add(safe(bill.getPaidAmount())));
        }

        stats.put("totalBillAmount", totalBillAmount);
        stats.put("totalPaidAmount", totalPaidAmount);
        stats.put("totalUnpaidAmount", totalBillAmount.subtract(totalPaidAmount));
        stats.put("statusStats", statusStats);
        return stats;
    }

    @Override
    public void batchGenerateMonthlyBills(String billingPeriod) {
        // 当前版本缺少企业主数据列表，先保留为空实现以保证接口可用。
    }

    @Override
    public void sendBillReminder(String billId) {
        // 当前版本未集成消息中心，先保留为空实现。
    }

    @Override
    public String downloadBillFile(String id) {
        return "/api/finance/bill/" + id + "/download";
    }

    @Override
    public void exportBills(BillQueryDTO queryDTO, jakarta.servlet.http.HttpServletResponse response) {
        throw new BusinessException("账单导出功能暂未实现");
    }

    /**
     * 转换为DTO
     */
    private BillDTO convertToDTO(Bill bill) {
        BillDTO dto = new BillDTO();
        dto.setId(bill.getId());
        dto.setBillNo(bill.getBillNo());
        dto.setEnterpriseId(bill.getEnterpriseId());
        dto.setEnterpriseName("");
        dto.setBillingPeriod(formatBillingPeriod(bill.getPeriodStartDate()));
        dto.setBillingMonth(bill.getPeriodStartDate() == null ? null : bill.getPeriodStartDate().withDayOfMonth(1));
        dto.setPolicyCount(0);
        dto.setTotalInsuredAmount(BigDecimal.ZERO);
        dto.setTotalPremiumAmount(safe(bill.getTotalAmount()));
        dto.setBillAmount(safe(bill.getTotalAmount()));
        dto.setPaidAmount(safe(bill.getPaidAmount()));
        dto.setUnpaidAmount(getUnpaidAmount(bill));
        dto.setStatus(mapEntityStatusToDto(bill.getStatus()));
        dto.setStatusName(getStatusName(dto.getStatus()));
        dto.setDueDate(bill.getDueDate());
        dto.setPaymentDate(bill.getPaidTime() == null ? null : bill.getPaidTime().toLocalDate());
        dto.setPaymentMethod(mapPaymentMethod(bill.getPaymentMethod()));
        dto.setPaymentMethodName(getPaymentMethodName(bill.getPaymentMethod()));
        dto.setPaymentNo(bill.getPaymentNo());
        dto.setReconciliationStatus(bill.getReconciliationStatus());
        dto.setReconciliationStatusName(bill.getReconciliationStatusName());
        dto.setReconciliationTime(bill.getReconciliationTime());
        dto.setReconciliationUserId(bill.getReconciliationUserId());
        dto.setReconciliationUserName(bill.getReconciliationUserName());
        dto.setRemark(bill.getRemark());
        dto.setBillFileUrl(bill.getAttachmentUrl());
        dto.setBillFileName(bill.getAttachmentName());
        dto.setBillFileSize(bill.getAttachmentSize());
        dto.setCreateTime(bill.getCreatedAt());
        dto.setUpdateTime(bill.getUpdatedAt());
        return dto;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待生成";
            case 1:
                return "待付款";
            case 2:
                return "付款中";
            case 3:
                return "已付款";
            case 4:
                return "已逾期";
            case 5:
                return "已取消";
            default:
                return "未知";
        }
    }

    /**
     * 获取支付方式名称
     */
    private String getPaymentMethodName(String method) {
        if (method == null) {
            return "未支付";
        }
        switch (method) {
            case "wallet_balance":
                return "余额扣款";
            case "wechat_pay":
                return "在线支付";
            case "bank_transfer":
                return "银行转账";
            case "alipay":
                return "支付宝";
            default:
                return "其他";
        }
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generateBillNo() {
        return "BILL" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal getUnpaidAmount(Bill bill) {
        BigDecimal receivable = safe(bill.getReceivableAmount());
        if (receivable.compareTo(BigDecimal.ZERO) <= 0) {
            receivable = safe(bill.getTotalAmount());
        }
        return receivable.subtract(safe(bill.getPaidAmount()));
    }

    private String formatBillingPeriod(LocalDate periodStartDate) {
        return periodStartDate == null ? null : periodStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    private void appendRemark(Bill bill, String remark) {
        if (!isNotBlank(remark)) {
            return;
        }
        if (isNotBlank(bill.getRemark())) {
            bill.setRemark(bill.getRemark() + ";" + remark);
        } else {
            bill.setRemark(remark);
        }
    }

    private Integer mapEntityStatusToDto(Integer status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case 0:
                return 0;
            case 1:
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            case 6:
                return 5;
            default:
                return status;
        }
    }

    private Integer mapDtoStatusToEntity(Integer status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
            case 5:
                return 6;
            default:
                return status;
        }
    }

    private Integer mapPaymentMethod(String method) {
        if (method == null) {
            return null;
        }
        switch (method) {
            case "wallet_balance":
                return 1;
            case "wechat_pay":
            case "alipay":
                return 2;
            case "bank_transfer":
                return 3;
            default:
                return null;
        }
    }

    private String mapPaymentMethod(Integer method) {
        if (method == null) {
            return null;
        }
        switch (method) {
            case 1:
                return "wallet_balance";
            case 2:
                return "wechat_pay";
            case 3:
                return "bank_transfer";
            default:
                return "wallet_balance";
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
