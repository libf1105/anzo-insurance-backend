package com.anzo.insurance.modules.finance.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.modules.finance.dto.*;
import com.anzo.insurance.modules.finance.entity.Invoice;
import com.anzo.insurance.modules.finance.repository.InvoiceMapper;
import com.anzo.insurance.modules.finance.service.BillService;
import com.anzo.insurance.modules.finance.service.InvoiceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发票服务实现类
 */
@Service
public class InvoiceServiceImpl extends ServiceImpl<InvoiceMapper, Invoice> implements InvoiceService {

    @Resource
    private InvoiceMapper invoiceMapper;

    @Resource
    private BillService billService;

    @Override
    public InvoiceDTO getInvoice(String id) {
        Invoice invoice = baseMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        return convertToDTO(invoice);
    }

    @Override
    public IPage<InvoiceDTO> queryInvoicePage(InvoiceQueryDTO queryDTO) {
        Page<Invoice> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Invoice> queryWrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getEnterpriseId() != null) {
            queryWrapper.eq(Invoice::getEnterpriseId, queryDTO.getEnterpriseId());
        }
        if (queryDTO.getEnterpriseName() != null) {
            queryWrapper.like(Invoice::getEnterpriseName, queryDTO.getEnterpriseName());
        }
        if (queryDTO.getInvoiceNo() != null) {
            queryWrapper.like(Invoice::getInvoiceNo, queryDTO.getInvoiceNo());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Invoice::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getInvoiceType() != null) {
            queryWrapper.eq(Invoice::getInvoiceType, queryDTO.getInvoiceType());
        }
        if (queryDTO.getShippingStatus() != null) {
            queryWrapper.eq(Invoice::getShippingStatus, queryDTO.getShippingStatus());
        }
        if (queryDTO.getMinInvoiceAmount() != null) {
            queryWrapper.ge(Invoice::getInvoiceAmount, queryDTO.getMinInvoiceAmount());
        }
        if (queryDTO.getMaxInvoiceAmount() != null) {
            queryWrapper.le(Invoice::getInvoiceAmount, queryDTO.getMaxInvoiceAmount());
        }
        if (queryDTO.getMinApplyAmount() != null) {
            queryWrapper.ge(Invoice::getApplyAmount, queryDTO.getMinApplyAmount());
        }
        if (queryDTO.getMaxApplyAmount() != null) {
            queryWrapper.le(Invoice::getApplyAmount, queryDTO.getMaxApplyAmount());
        }
        if (queryDTO.getApplyTimeStart() != null && queryDTO.getApplyTimeEnd() != null) {
            queryWrapper.between(Invoice::getApplyTime, queryDTO.getApplyTimeStart().atStartOfDay(), 
                    queryDTO.getApplyTimeEnd().plusDays(1).atStartOfDay());
        }
        if (queryDTO.getInvoiceTimeStart() != null && queryDTO.getInvoiceTimeEnd() != null) {
            queryWrapper.between(Invoice::getInvoiceTime, queryDTO.getInvoiceTimeStart().atStartOfDay(), 
                    queryDTO.getInvoiceTimeEnd().plusDays(1).atStartOfDay());
        }
        
        // 排序
        if ("applyAmount".equals(queryDTO.getSortField())) {
            if ("asc".equals(queryDTO.getSortOrder())) {
                queryWrapper.orderByAsc(Invoice::getApplyAmount);
            } else {
                queryWrapper.orderByDesc(Invoice::getApplyAmount);
            }
        } else if ("applyTime".equals(queryDTO.getSortField())) {
            if ("asc".equals(queryDTO.getSortOrder())) {
                queryWrapper.orderByAsc(Invoice::getApplyTime);
            } else {
                queryWrapper.orderByDesc(Invoice::getApplyTime);
            }
        } else {
            queryWrapper.orderByDesc(Invoice::getApplyTime);
        }
        
        IPage<Invoice> invoicePage = baseMapper.selectPage(page, queryWrapper);
        return invoicePage.convert(this::convertToDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDTO applyInvoice(InvoiceApplyDTO applyDTO) {
        // 检查是否已存在该申请单的发票
        Invoice existingInvoice = baseMapper.selectOne(new LambdaQueryWrapper<Invoice>()
                .eq(Invoice::getApplicationId, applyDTO.getApplicationId()));
        if (existingInvoice != null) {
            throw new BusinessException("该申请单已申请过发票");
        }
        
        Invoice invoice = new Invoice();
        invoice.setId(generateId());
        invoice.setInvoiceNo(generateInvoiceNo());
        invoice.setEnterpriseId(applyDTO.getEnterpriseId());
        invoice.setEnterpriseName(""); // TODO: 从企业服务获取企业名称
        invoice.setApplicationId(applyDTO.getApplicationId());
        invoice.setApplyAmount(applyDTO.getApplyAmount());
        invoice.setApplyTime(LocalDateTime.now());
        invoice.setInvoiceType(applyDTO.getInvoiceType());
        invoice.setInvoiceTitle(applyDTO.getInvoiceTitle());
        invoice.setTaxpayerId(applyDTO.getTaxpayerId());
        invoice.setBankName(applyDTO.getBankName());
        invoice.setBankAccount(applyDTO.getBankAccount());
        invoice.setRegisterAddress(applyDTO.getRegisterAddress());
        invoice.setRegisterPhone(applyDTO.getRegisterPhone());
        invoice.setBuyerName(applyDTO.getBuyerName());
        invoice.setBuyerTaxpayerId(applyDTO.getBuyerTaxpayerId());
        invoice.setBuyerAddressPhone(applyDTO.getBuyerAddressPhone());
        invoice.setBuyerBankAccount(applyDTO.getBuyerBankAccount());
        invoice.setInvoiceContent(applyDTO.getInvoiceContent());
        invoice.setNeedShipping(applyDTO.getNeedShipping());
        invoice.setShippingAddress(applyDTO.getShippingAddress());
        invoice.setRecipientName(applyDTO.getRecipientName());
        invoice.setRecipientPhone(applyDTO.getRecipientPhone());
        invoice.setStatus(0); // 待开票
        invoice.setShippingStatus(0); // 待邮寄
        invoice.setApplyRemark(applyDTO.getApplyRemark());
        
        baseMapper.insert(invoice);
        return convertToDTO(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDTO issueInvoice(InvoiceUpdateDTO updateDTO) {
        Invoice invoice = baseMapper.selectById(updateDTO.getId());
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        
        // 检查发票状态
        if (invoice.getStatus() != 0) {
            throw new BusinessException("只有待开票的发票才能开票");
        }
        
        invoice.setInvoiceAmount(updateDTO.getInvoiceAmount());
        invoice.setInvoiceNo(updateDTO.getInvoiceNo());
        invoice.setInvoiceTime(LocalDateTime.now());
        invoice.setStatus(2); // 已开票
        invoice.setInvoiceFileUrl(updateDTO.getInvoiceFileUrl());
        invoice.setInvoiceFileName(updateDTO.getInvoiceFileName());
        invoice.setInvoiceFileSize(updateDTO.getInvoiceFileSize());
        invoice.setInvoiceRemark(updateDTO.getInvoiceRemark());
        
        baseMapper.updateById(invoice);
        return convertToDTO(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDTO voidInvoice(InvoiceUpdateDTO updateDTO) {
        Invoice invoice = baseMapper.selectById(updateDTO.getId());
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        
        // 检查发票状态
        if (invoice.getStatus() != 2) {
            throw new BusinessException("只有已开票的发票才能作废");
        }
        
        invoice.setStatus(4); // 已作废
        invoice.setVoidReason(updateDTO.getVoidReason());
        invoice.setVoidTime(LocalDateTime.now());
        
        baseMapper.updateById(invoice);
        return convertToDTO(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDTO updateShippingInfo(InvoiceUpdateDTO updateDTO) {
        Invoice invoice = baseMapper.selectById(updateDTO.getId());
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        
        if (!invoice.getNeedShipping()) {
            throw new BusinessException("该发票不需要邮寄");
        }
        
        invoice.setExpressCompany(updateDTO.getExpressCompany());
        invoice.setTrackingNo(updateDTO.getTrackingNo());
        invoice.setShippingStatus(1); // 邮寄中
        invoice.setShippingRemark(updateDTO.getShippingRemark());
        
        baseMapper.updateById(invoice);
        return convertToDTO(invoice);
    }

    @Override
    public void updateInvoiceStatus(String id, Integer status, String remark) {
        Invoice invoice = baseMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        
        invoice.setStatus(status);
        if (remark != null) {
            invoice.setRemark(invoice.getRemark() + ";" + remark);
        }
        
        baseMapper.updateById(invoice);
    }

    @Override
    public Map<String, Object> getEnterpriseInvoiceStats(String enterpriseId, String startTime, String endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<Invoice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Invoice::getEnterpriseId, enterpriseId);
        
        if (startTime != null && endTime != null) {
            queryWrapper.between(Invoice::getCreatedAt, startTime, endTime);
        }
        
        // 总发票数量
        Long totalCount = baseMapper.selectCount(queryWrapper);
        stats.put("totalCount", totalCount);
        
        // 总申请金额
        queryWrapper.select("SUM(apply_amount) as totalApplyAmount", 
                "SUM(invoice_amount) as totalInvoiceAmount");
        Map<String, Object> result = baseMapper.selectMaps(queryWrapper).get(0);
        BigDecimal totalApplyAmount = result.get("totalApplyAmount") != null ? 
                (BigDecimal) result.get("totalApplyAmount") : BigDecimal.ZERO;
        BigDecimal totalInvoiceAmount = result.get("totalInvoiceAmount") != null ? 
                (BigDecimal) result.get("totalInvoiceAmount") : BigDecimal.ZERO;
        stats.put("totalApplyAmount", totalApplyAmount);
        stats.put("totalInvoiceAmount", totalInvoiceAmount);
        
        // 按状态统计
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Invoice::getEnterpriseId, enterpriseId);
        if (startTime != null && endTime != null) {
            queryWrapper.between(Invoice::getCreatedAt, startTime, endTime);
        }
        queryWrapper.select("status, COUNT(*) as count, SUM(apply_amount) as applyAmount, SUM(invoice_amount) as invoiceAmount");
        queryWrapper.groupBy("status");
        
        Map<Integer, Map<String, Object>> statusStats = new HashMap<>();
        for (Map<String, Object> row : baseMapper.selectMaps(queryWrapper)) {
            Integer status = (Integer) row.get("status");
            Long count = (Long) row.get("count");
            BigDecimal applyAmount = (BigDecimal) row.get("applyAmount");
            BigDecimal invoiceAmount = (BigDecimal) row.get("invoiceAmount");
            
            Map<String, Object> statusStat = new HashMap<>();
            statusStat.put("count", count);
            statusStat.put("applyAmount", applyAmount);
            statusStat.put("invoiceAmount", invoiceAmount);
            statusStat.put("statusName", getStatusName(status));
            statusStats.put(status, statusStat);
        }
        stats.put("statusStats", statusStats);
        
        return stats;
    }

    @Override
    public void batchIssueInvoices(List<String> invoiceIds) {
        // TODO: 批量开票逻辑
        for (String invoiceId : invoiceIds) {
            Invoice invoice = baseMapper.selectById(invoiceId);
            if (invoice != null && invoice.getStatus() == 0) {
                // 模拟开票操作
                invoice.setInvoiceAmount(invoice.getApplyAmount());
                invoice.setInvoiceNo("INV" + System.currentTimeMillis() + invoiceId.substring(0, 4));
                invoice.setInvoiceTime(LocalDateTime.now());
                invoice.setStatus(2);
                baseMapper.updateById(invoice);
            }
        }
    }

    @Override
    public String downloadInvoiceFile(String id) {
        // TODO: 生成并返回发票文件下载地址
        return "/api/finance/invoice/" + id + "/download";
    }

    @Override
    public void exportInvoices(InvoiceQueryDTO queryDTO, jakarta.servlet.http.HttpServletResponse response) {
        // TODO: 实现导出功能
    }

    @Override
    public List<BillDTO> queryAvailableBillsForInvoice(String enterpriseId) {
        // TODO: 查询企业可申请发票的账单列表
        // 通常是可以已付款且未开发票的账单
        return List.of();
    }

    /**
     * 转换为DTO
     */
    private InvoiceDTO convertToDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNo(invoice.getInvoiceNo());
        dto.setEnterpriseId(invoice.getEnterpriseId());
        dto.setEnterpriseName(invoice.getEnterpriseName());
        dto.setApplicationId(invoice.getApplicationId());
        dto.setApplyAmount(invoice.getApplyAmount());
        dto.setApplyTime(invoice.getApplyTime());
        dto.setInvoiceAmount(invoice.getInvoiceAmount());
        dto.setInvoiceTime(invoice.getInvoiceTime());
        dto.setInvoiceType(invoice.getInvoiceType());
        dto.setInvoiceTypeName(getInvoiceTypeName(invoice.getInvoiceType()));
        dto.setInvoiceTitle(invoice.getInvoiceTitle());
        dto.setTaxpayerId(invoice.getTaxpayerId());
        dto.setBankName(invoice.getBankName());
        dto.setBankAccount(invoice.getBankAccount());
        dto.setRegisterAddress(invoice.getRegisterAddress());
        dto.setRegisterPhone(invoice.getRegisterPhone());
        dto.setBuyerName(invoice.getBuyerName());
        dto.setBuyerTaxpayerId(invoice.getBuyerTaxpayerId());
        dto.setBuyerAddressPhone(invoice.getBuyerAddressPhone());
        dto.setBuyerBankAccount(invoice.getBuyerBankAccount());
        dto.setInvoiceContent(invoice.getInvoiceContent());
        dto.setStatus(invoice.getStatus());
        dto.setStatusName(getStatusName(invoice.getStatus()));
        dto.setVoidReason(invoice.getVoidReason());
        dto.setVoidTime(invoice.getVoidTime());
        dto.setInvoiceFileUrl(invoice.getInvoiceFileUrl());
        dto.setInvoiceFileName(invoice.getInvoiceFileName());
        dto.setInvoiceFileSize(invoice.getInvoiceFileSize());
        dto.setNeedShipping(invoice.getNeedShipping());
        dto.setShippingAddress(invoice.getShippingAddress());
        dto.setRecipientName(invoice.getRecipientName());
        dto.setRecipientPhone(invoice.getRecipientPhone());
        dto.setShippingStatus(invoice.getShippingStatus());
        dto.setShippingStatusName(getShippingStatusName(invoice.getShippingStatus()));
        dto.setExpressCompany(invoice.getExpressCompany());
        dto.setTrackingNo(invoice.getTrackingNo());
        dto.setReceiveTime(invoice.getReceiveTime());
        dto.setApplyRemark(invoice.getApplyRemark());
        dto.setInvoiceRemark(invoice.getInvoiceRemark());
        dto.setCreateTime(invoice.getCreatedAt());
        dto.setUpdateTime(invoice.getUpdatedAt());
        return dto;
    }

    /**
     * 获取发票类型名称
     */
    private String getInvoiceTypeName(Integer type) {
        switch (type) {
            case 1: return "增值税普通发票";
            case 2: return "增值税专用发票";
            default: return "其他";
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "待开票";
            case 1: return "开票中";
            case 2: return "已开票";
            case 3: return "开票失败";
            case 4: return "已作废";
            default: return "未知";
        }
    }

    /**
     * 获取邮寄状态名称
     */
    private String getShippingStatusName(Integer status) {
        switch (status) {
            case 0: return "待邮寄";
            case 1: return "邮寄中";
            case 2: return "已签收";
            default: return "未知";
        }
    }

    /**
     * 生成ID（简化版）
     */
    private String generateId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成发票编号（简化版）
     */
    private String generateInvoiceNo() {
        return "INV" + System.currentTimeMillis() + 
                String.format("%04d", new java.util.Random().nextInt(10000));
    }
}
