package com.anzo.insurance.modules.finance.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.modules.finance.dto.BillDTO;
import com.anzo.insurance.modules.finance.dto.BillQueryDTO;
import com.anzo.insurance.modules.finance.dto.InvoiceApplyDTO;
import com.anzo.insurance.modules.finance.dto.InvoiceDTO;
import com.anzo.insurance.modules.finance.dto.InvoiceQueryDTO;
import com.anzo.insurance.modules.finance.dto.InvoiceUpdateDTO;
import com.anzo.insurance.modules.finance.entity.Invoice;
import com.anzo.insurance.modules.finance.repository.InvoiceMapper;
import com.anzo.insurance.modules.finance.service.BillService;
import com.anzo.insurance.modules.finance.service.InvoiceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.URLEncoder;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发票服务实现类
 */
@Service
public class InvoiceServiceImpl extends ServiceImpl<InvoiceMapper, Invoice> implements InvoiceService {

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

        if (isNotBlank(queryDTO.getEnterpriseId())) {
            queryWrapper.eq(Invoice::getEnterpriseId, queryDTO.getEnterpriseId());
        }
        if (isNotBlank(queryDTO.getInvoiceNo())) {
            queryWrapper.like(Invoice::getInvoiceNo, queryDTO.getInvoiceNo());
        }
        if (queryDTO.getStatus() != null) {
            appendStatusFilter(queryWrapper, queryDTO.getStatus());
        }
        if (queryDTO.getInvoiceType() != null) {
            queryWrapper.eq(Invoice::getInvoiceType, queryDTO.getInvoiceType());
        }
        if (queryDTO.getShippingStatus() != null) {
            appendShippingFilter(queryWrapper, queryDTO.getShippingStatus());
        }
        if (queryDTO.getMinInvoiceAmount() != null) {
            queryWrapper.ge(Invoice::getInvoiceAmount, queryDTO.getMinInvoiceAmount());
        }
        if (queryDTO.getMaxInvoiceAmount() != null) {
            queryWrapper.le(Invoice::getInvoiceAmount, queryDTO.getMaxInvoiceAmount());
        }
        if (queryDTO.getMinApplyAmount() != null) {
            queryWrapper.ge(Invoice::getTotalAmount, queryDTO.getMinApplyAmount());
        }
        if (queryDTO.getMaxApplyAmount() != null) {
            queryWrapper.le(Invoice::getTotalAmount, queryDTO.getMaxApplyAmount());
        }
        if (queryDTO.getApplyTimeStart() != null) {
            queryWrapper.ge(Invoice::getApplicationTime, queryDTO.getApplyTimeStart().atStartOfDay());
        }
        if (queryDTO.getApplyTimeEnd() != null) {
            queryWrapper.le(Invoice::getApplicationTime, queryDTO.getApplyTimeEnd().atTime(23, 59, 59));
        }
        if (queryDTO.getInvoiceTimeStart() != null) {
            queryWrapper.ge(Invoice::getInvoiceTime, queryDTO.getInvoiceTimeStart().atStartOfDay());
        }
        if (queryDTO.getInvoiceTimeEnd() != null) {
            queryWrapper.le(Invoice::getInvoiceTime, queryDTO.getInvoiceTimeEnd().atTime(23, 59, 59));
        }

        if ("applyAmount".equals(queryDTO.getSortField())) {
            if ("asc".equalsIgnoreCase(queryDTO.getSortOrder())) {
                queryWrapper.orderByAsc(Invoice::getTotalAmount);
            } else {
                queryWrapper.orderByDesc(Invoice::getTotalAmount);
            }
        } else if ("applyTime".equals(queryDTO.getSortField())) {
            if ("asc".equalsIgnoreCase(queryDTO.getSortOrder())) {
                queryWrapper.orderByAsc(Invoice::getApplicationTime);
            } else {
                queryWrapper.orderByDesc(Invoice::getApplicationTime);
            }
        } else {
            queryWrapper.orderByDesc(Invoice::getApplicationTime);
        }

        return baseMapper.selectPage(page, queryWrapper).convert(this::convertToDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDTO applyInvoice(InvoiceApplyDTO applyDTO) {
        Invoice existingInvoice = baseMapper.selectOne(new LambdaQueryWrapper<Invoice>()
                .eq(Invoice::getRelatedBusinessId, applyDTO.getApplicationId())
                .eq(Invoice::getEnterpriseId, applyDTO.getEnterpriseId())
                .last("limit 1"));
        if (existingInvoice != null) {
            throw new BusinessException("该申请单已申请过发票");
        }

        Invoice invoice = new Invoice();
        invoice.setId(generateId());
        invoice.setInvoiceNo(generateInvoiceNo());
        invoice.setEnterpriseId(applyDTO.getEnterpriseId());
        invoice.setInvoiceType(applyDTO.getInvoiceType());
        invoice.setInvoiceTitle(applyDTO.getInvoiceTitle());
        invoice.setTaxpayerId(applyDTO.getTaxpayerId());
        invoice.setAddress(applyDTO.getRegisterAddress());
        invoice.setPhone(applyDTO.getRegisterPhone());
        invoice.setBankName(applyDTO.getBankName());
        invoice.setBankAccount(applyDTO.getBankAccount());
        invoice.setInvoiceAmount(BigDecimal.ZERO);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(defaultAmount(applyDTO.getApplyAmount()));
        invoice.setCurrency("CNY");
        invoice.setInvoiceContent(applyDTO.getInvoiceContent());
        invoice.setStatus(0);
        invoice.setApplicationTime(LocalDateTime.now());
        invoice.setRelatedBusinessType(resolveRelatedBusinessType(applyDTO));
        invoice.setRelatedBusinessId(applyDTO.getApplicationId());
        invoice.setRelatedBusinessDesc(buildRelatedBusinessDesc(applyDTO));
        invoice.setRemark(applyDTO.getApplyRemark());
        invoice.setDeliveryMethod(Boolean.TRUE.equals(applyDTO.getNeedShipping()) ? "express" : "electronic");
        invoice.setRecipientName(applyDTO.getRecipientName());
        invoice.setRecipientPhone(applyDTO.getRecipientPhone());
        invoice.setRecipientAddress(applyDTO.getShippingAddress());
        invoice.setIsRedInvoice(Boolean.FALSE);
        baseMapper.insert(invoice);
        return convertToDTO(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDTO issueInvoice(InvoiceUpdateDTO updateDTO) {
        Invoice invoice = requireInvoice(updateDTO.getId());
        if (invoice.isInvoiced() || invoice.isCancelled()) {
            throw new BusinessException("当前发票状态不支持开票");
        }

        BigDecimal amount = updateDTO.getInvoiceAmount() == null ? safe(invoice.getTotalAmount()) : updateDTO.getInvoiceAmount();
        invoice.setInvoiceAmount(amount);
        invoice.setTotalAmount(amount);
        invoice.setInvoiceNo(isNotBlank(updateDTO.getInvoiceNo()) ? updateDTO.getInvoiceNo() : invoice.getInvoiceNo());
        invoice.setInvoiceTime(LocalDateTime.now());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setStatus(4);
        invoice.setInvoiceFileUrl(updateDTO.getInvoiceFileUrl());
        invoice.setInvoiceFileName(updateDTO.getInvoiceFileName());
        invoice.setInvoiceFileSize(updateDTO.getInvoiceFileSize());
        appendRemark(invoice, updateDTO.getInvoiceRemark());
        baseMapper.updateById(invoice);
        return convertToDTO(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDTO voidInvoice(InvoiceUpdateDTO updateDTO) {
        Invoice invoice = requireInvoice(updateDTO.getId());
        if (!invoice.isInvoiced()) {
            throw new BusinessException("只有已开票的发票才能作废");
        }

        invoice.setStatus(5);
        invoice.setCancelReason(updateDTO.getVoidReason());
        invoice.setCancelTime(LocalDateTime.now());
        appendRemark(invoice, updateDTO.getInvoiceRemark());
        baseMapper.updateById(invoice);
        return convertToDTO(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDTO updateShippingInfo(InvoiceUpdateDTO updateDTO) {
        Invoice invoice = requireInvoice(updateDTO.getId());
        if (!"express".equals(invoice.getDeliveryMethod())) {
            throw new BusinessException("该发票不需要邮寄");
        }

        invoice.setTrackingNo(updateDTO.getTrackingNo());
        invoice.setDeliveryTime(LocalDateTime.now());
        appendRemark(invoice, updateDTO.getShippingRemark());
        baseMapper.updateById(invoice);
        return convertToDTO(invoice);
    }

    @Override
    public void updateInvoiceStatus(String id, Integer status, String remark) {
        Invoice invoice = requireInvoice(id);
        invoice.setStatus(mapDtoStatusToEntity(status));
        appendRemark(invoice, remark);
        baseMapper.updateById(invoice);
    }

    @Override
    public Map<String, Object> getEnterpriseInvoiceStats(String enterpriseId, String startTime, String endTime) {
        Map<String, Object> stats = new HashMap<>();
        LambdaQueryWrapper<Invoice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Invoice::getEnterpriseId, enterpriseId);

        LocalDateTime start = parseDateTime(startTime, false);
        LocalDateTime end = parseDateTime(endTime, true);
        if (start != null) {
            queryWrapper.ge(Invoice::getCreatedAt, start);
        }
        if (end != null) {
            queryWrapper.le(Invoice::getCreatedAt, end);
        }

        List<Invoice> invoices = baseMapper.selectList(queryWrapper);
        stats.put("totalCount", (long) invoices.size());

        BigDecimal totalApplyAmount = BigDecimal.ZERO;
        BigDecimal totalInvoiceAmount = BigDecimal.ZERO;
        Map<Integer, Map<String, Object>> statusStats = new HashMap<>();
        for (Invoice invoice : invoices) {
            totalApplyAmount = totalApplyAmount.add(safe(invoice.getTotalAmount()));
            totalInvoiceAmount = totalInvoiceAmount.add(safe(invoice.getInvoiceAmount()));

            Integer status = mapEntityStatusToDto(invoice.getStatus());
            Map<String, Object> statusStat = statusStats.computeIfAbsent(status, key -> {
                Map<String, Object> item = new HashMap<>();
                item.put("count", 0L);
                item.put("applyAmount", BigDecimal.ZERO);
                item.put("invoiceAmount", BigDecimal.ZERO);
                item.put("statusName", getStatusName(key));
                return item;
            });
            statusStat.put("count", ((Long) statusStat.get("count")) + 1L);
            statusStat.put("applyAmount", ((BigDecimal) statusStat.get("applyAmount")).add(safe(invoice.getTotalAmount())));
            statusStat.put("invoiceAmount", ((BigDecimal) statusStat.get("invoiceAmount")).add(safe(invoice.getInvoiceAmount())));
        }

        stats.put("totalApplyAmount", totalApplyAmount);
        stats.put("totalInvoiceAmount", totalInvoiceAmount);
        stats.put("statusStats", statusStats);
        return stats;
    }

    @Override
    public void batchIssueInvoices(List<String> invoiceIds) {
        for (String invoiceId : invoiceIds) {
            Invoice invoice = baseMapper.selectById(invoiceId);
            if (invoice != null && !invoice.isInvoiced() && !invoice.isCancelled()) {
                InvoiceUpdateDTO updateDTO = new InvoiceUpdateDTO();
                updateDTO.setId(invoiceId);
                updateDTO.setInvoiceAmount(safe(invoice.getTotalAmount()));
                updateDTO.setInvoiceNo(generateInvoiceNo());
                issueInvoice(updateDTO);
            }
        }
    }

    @Override
    public String downloadInvoiceFile(String id) {
        return "/api/finance/invoice/" + id + "/download";
    }

    @Override
    public void exportInvoices(InvoiceQueryDTO queryDTO, jakarta.servlet.http.HttpServletResponse response) {
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1000);
        List<InvoiceDTO> records = queryInvoicePage(queryDTO).getRecords();

        String fileName = URLEncoder.encode("发票列表.csv", StandardCharsets.UTF_8);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try {
            response.getWriter().write('\uFEFF');
            response.getWriter().write("发票号码,申请金额,开票金额,发票类型,发票抬头,开票状态,邮寄状态,申请时间,开票时间,备注\n");
            for (InvoiceDTO invoice : records) {
                response.getWriter().write(String.join(",",
                        csv(invoice.getInvoiceNo()),
                        csv(invoice.getApplyAmount()),
                        csv(invoice.getInvoiceAmount()),
                        csv(invoice.getInvoiceTypeName()),
                        csv(invoice.getInvoiceTitle()),
                        csv(invoice.getStatusName()),
                        csv(invoice.getShippingStatusName()),
                        csv(invoice.getApplyTime()),
                        csv(invoice.getInvoiceTime()),
                        csv(invoice.getApplyRemark())
                ));
                response.getWriter().write("\n");
            }
        } catch (IOException e) {
            throw new BusinessException("发票导出失败");
        }
    }

    @Override
    public List<BillDTO> queryAvailableBillsForInvoice(String enterpriseId) {
        BillQueryDTO queryDTO = new BillQueryDTO();
        queryDTO.setEnterpriseId(enterpriseId);
        queryDTO.setStatus(3);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(200);
        return billService.queryBillPage(queryDTO).getRecords();
    }

    private InvoiceDTO convertToDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNo(invoice.getInvoiceNo());
        dto.setEnterpriseId(invoice.getEnterpriseId());
        dto.setEnterpriseName("");
        dto.setApplicationId(invoice.getRelatedBusinessId());
        dto.setApplyAmount(safe(invoice.getTotalAmount()));
        dto.setApplyTime(invoice.getApplicationTime());
        dto.setInvoiceAmount(safe(invoice.getInvoiceAmount()));
        dto.setInvoiceTime(invoice.getInvoiceTime());
        dto.setInvoiceType(invoice.getInvoiceType());
        dto.setInvoiceTypeName(invoice.getInvoiceTypeName());
        dto.setInvoiceTitle(invoice.getInvoiceTitle());
        dto.setTaxpayerId(invoice.getTaxpayerId());
        dto.setBankName(invoice.getBankName());
        dto.setBankAccount(invoice.getBankAccount());
        dto.setRegisterAddress(invoice.getAddress());
        dto.setRegisterPhone(invoice.getPhone());
        dto.setBuyerName(null);
        dto.setBuyerTaxpayerId(null);
        dto.setBuyerAddressPhone(null);
        dto.setBuyerBankAccount(null);
        dto.setInvoiceContent(invoice.getInvoiceContent());
        dto.setStatus(mapEntityStatusToDto(invoice.getStatus()));
        dto.setStatusName(getStatusName(dto.getStatus()));
        dto.setVoidReason(invoice.getCancelReason());
        dto.setVoidTime(invoice.getCancelTime());
        dto.setInvoiceFileUrl(invoice.getInvoiceFileUrl());
        dto.setInvoiceFileName(invoice.getInvoiceFileName());
        dto.setInvoiceFileSize(invoice.getInvoiceFileSize());
        dto.setShippingAddress(invoice.getRecipientAddress());
        dto.setRecipientName(invoice.getRecipientName());
        dto.setRecipientPhone(invoice.getRecipientPhone());
        dto.setShippingStatus(getShippingStatus(invoice));
        dto.setShippingStatusName(getShippingStatusName(dto.getShippingStatus()));
        dto.setExpressCompany(null);
        dto.setTrackingNo(invoice.getTrackingNo());
        dto.setReceiveTime(invoice.getReceiveTime());
        dto.setApplyRemark(invoice.getRemark());
        dto.setInvoiceRemark(invoice.getRemark());
        dto.setCreateTime(invoice.getCreatedAt());
        dto.setUpdateTime(invoice.getUpdatedAt());
        return dto;
    }

    private Invoice requireInvoice(String id) {
        Invoice invoice = baseMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        return invoice;
    }

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private void appendStatusFilter(LambdaQueryWrapper<Invoice> queryWrapper, Integer dtoStatus) {
        switch (dtoStatus) {
            case 0:
                queryWrapper.in(Invoice::getStatus, 0, 1, 2);
                break;
            case 1:
                queryWrapper.eq(Invoice::getStatus, 2);
                break;
            case 2:
                queryWrapper.in(Invoice::getStatus, 4, 6);
                break;
            case 3:
                queryWrapper.eq(Invoice::getStatus, 3);
                break;
            case 4:
                queryWrapper.eq(Invoice::getStatus, 5);
                break;
            default:
                queryWrapper.eq(Invoice::getStatus, dtoStatus);
                break;
        }
    }

    private void appendShippingFilter(LambdaQueryWrapper<Invoice> queryWrapper, Integer shippingStatus) {
        switch (shippingStatus) {
            case 0:
                queryWrapper.eq(Invoice::getDeliveryMethod, "express").isNull(Invoice::getDeliveryTime);
                break;
            case 1:
                queryWrapper.eq(Invoice::getDeliveryMethod, "express").isNotNull(Invoice::getDeliveryTime).isNull(Invoice::getReceiveTime);
                break;
            case 2:
                queryWrapper.eq(Invoice::getDeliveryMethod, "express").isNotNull(Invoice::getReceiveTime);
                break;
            default:
                break;
        }
    }

    private Integer getShippingStatus(Invoice invoice) {
        if (!"express".equals(invoice.getDeliveryMethod())) {
            return null;
        }
        if (invoice.getReceiveTime() != null) {
            return 2;
        }
        if (invoice.getDeliveryTime() != null) {
            return 1;
        }
        return 0;
    }

    private Integer mapEntityStatusToDto(Integer status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case 0:
            case 1:
            case 2:
                return 0;
            case 3:
                return 3;
            case 4:
            case 6:
                return 2;
            case 5:
                return 4;
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
                return 2;
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 5;
            default:
                return status;
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待开票";
            case 1:
                return "开票中";
            case 2:
                return "已开票";
            case 3:
                return "开票失败";
            case 4:
                return "已作废";
            default:
                return "未知";
        }
    }

    private String getShippingStatusName(Integer status) {
        if (status == null) {
            return "无需邮寄";
        }
        switch (status) {
            case 0:
                return "待邮寄";
            case 1:
                return "邮寄中";
            case 2:
                return "已签收";
            default:
                return "未知";
        }
    }

    private String resolveRelatedBusinessType(InvoiceApplyDTO applyDTO) {
        return applyDTO.getBillIds() == null || applyDTO.getBillIds().isEmpty() ? "bill" : "bill";
    }

    private String buildRelatedBusinessDesc(InvoiceApplyDTO applyDTO) {
        return "发票申请:" + applyDTO.getApplicationId();
    }

    private void appendRemark(Invoice invoice, String remark) {
        if (!isNotBlank(remark)) {
            return;
        }
        if (isNotBlank(invoice.getRemark())) {
            invoice.setRemark(invoice.getRemark() + ";" + remark);
        } else {
            invoice.setRemark(remark);
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private LocalDateTime parseDateTime(String text, boolean endOfDay) {
        if (!isNotBlank(text)) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(text);
            return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(text);
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generateInvoiceNo() {
        return "INV" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
}
