package com.anzo.insurance.modules.finance.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.modules.finance.dto.TransactionQueryDTO;
import com.anzo.insurance.modules.finance.dto.TransactionRecordDTO;
import com.anzo.insurance.modules.finance.entity.TransactionRecord;
import com.anzo.insurance.modules.finance.repository.TransactionRecordMapper;
import com.anzo.insurance.modules.finance.service.TransactionRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.URLEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * 交易记录服务实现类
 */
@Service
public class TransactionRecordServiceImpl extends ServiceImpl<TransactionRecordMapper, TransactionRecord> implements TransactionRecordService {

    @Resource
    private TransactionRecordMapper transactionRecordMapper;

    @Override
    public TransactionRecordDTO getTransactionRecord(String id) {
        TransactionRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("交易记录不存在");
        }
        return convertToDTO(record);
    }

    @Override
    public IPage<TransactionRecordDTO> queryTransactionPage(TransactionQueryDTO queryDTO) {
        Page<TransactionRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<TransactionRecord> queryWrapper = new LambdaQueryWrapper<>();

        if (isNotBlank(queryDTO.getEnterpriseId())) {
            queryWrapper.eq(TransactionRecord::getEnterpriseId, queryDTO.getEnterpriseId());
        }
        if (queryDTO.getTransactionType() != null) {
            queryWrapper.eq(TransactionRecord::getTransactionType, queryDTO.getTransactionType());
        }
        if (queryDTO.getBusinessType() != null) {
            queryWrapper.eq(TransactionRecord::getRelatedBusinessType, getBusinessTypeString(queryDTO.getBusinessType()));
        }
        if (isNotBlank(queryDTO.getBusinessId())) {
            queryWrapper.eq(TransactionRecord::getRelatedBusinessId, queryDTO.getBusinessId());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(TransactionRecord::getStatus, mapDtoStatusToEntity(queryDTO.getStatus()));
        }
        if (queryDTO.getPaymentMethod() != null) {
            queryWrapper.eq(TransactionRecord::getPaymentMethod, getPaymentMethodString(queryDTO.getPaymentMethod()));
        }
        if (isNotBlank(queryDTO.getTransactionNo())) {
            queryWrapper.like(TransactionRecord::getTransactionNo, queryDTO.getTransactionNo());
        }
        if (isNotBlank(queryDTO.getPaymentNo())) {
            queryWrapper.like(TransactionRecord::getPaymentNo, queryDTO.getPaymentNo());
        }
        if (queryDTO.getMinAmount() != null) {
            queryWrapper.ge(TransactionRecord::getAmount, queryDTO.getMinAmount());
        }
        if (queryDTO.getMaxAmount() != null) {
            queryWrapper.le(TransactionRecord::getAmount, queryDTO.getMaxAmount());
        }
        if (isNotBlank(queryDTO.getOperatorUserId())) {
            queryWrapper.eq(TransactionRecord::getOperatorUserId, queryDTO.getOperatorUserId());
        }

        LocalDateTime start = parseDateTime(queryDTO.getStartTime(), false);
        LocalDateTime end = parseDateTime(queryDTO.getEndTime(), true);
        if (start != null) {
            queryWrapper.ge(TransactionRecord::getTransactionTime, start);
        }
        if (end != null) {
            queryWrapper.le(TransactionRecord::getTransactionTime, end);
        }

        if ("amount".equals(queryDTO.getSortField())) {
            if ("asc".equalsIgnoreCase(queryDTO.getSortOrder())) {
                queryWrapper.orderByAsc(TransactionRecord::getAmount);
            } else {
                queryWrapper.orderByDesc(TransactionRecord::getAmount);
            }
        } else {
            queryWrapper.orderByDesc(TransactionRecord::getTransactionTime);
        }

        return baseMapper.selectPage(page, queryWrapper).convert(this::convertToDTO);
    }

    @Override
    public TransactionRecordDTO createTransactionRecord(TransactionRecordDTO transactionRecordDTO) {
        TransactionRecord record = new TransactionRecord();
        record.setId(generateId());
        record.setTransactionNo(generateTransactionNo());
        record.setEnterpriseId(transactionRecordDTO.getEnterpriseId());
        record.setTransactionType(transactionRecordDTO.getTransactionType());
        record.setAmount(transactionRecordDTO.getAmount());
        record.setBalanceBefore(transactionRecordDTO.getBeforeBalance());
        record.setBalanceAfter(transactionRecordDTO.getAfterBalance());
        record.setFrozenBefore(transactionRecordDTO.getBeforeFrozenAmount());
        record.setFrozenAfter(transactionRecordDTO.getAfterFrozenAmount());
        record.setRelatedBusinessId(transactionRecordDTO.getBusinessId());
        record.setRelatedBusinessDesc(transactionRecordDTO.getBusinessDesc());
        record.setRelatedBusinessType(getBusinessTypeString(transactionRecordDTO.getBusinessType()));
        record.setPaymentMethod(getPaymentMethodString(transactionRecordDTO.getPaymentMethod()));
        record.setPaymentNo(transactionRecordDTO.getPaymentNo());
        record.setStatus(mapDtoStatusToEntity(transactionRecordDTO.getStatus() == null ? 2 : transactionRecordDTO.getStatus()));
        record.setRemark(transactionRecordDTO.getRemark());
        record.setOperatorUserId(transactionRecordDTO.getOperatorUserId());
        record.setOperatorUserName(transactionRecordDTO.getOperatorUserName());
        record.setTransactionTime(LocalDateTime.now());
        record.setCompletedTime(record.getStatus() == 1 ? LocalDateTime.now() : null);
        record.setCurrency("CNY");
        record.setIsManual(Boolean.TRUE);
        record.setAuditStatus(0);

        baseMapper.insert(record);
        return convertToDTO(record);
    }

    @Override
    public void updateTransactionStatus(String id, Integer status, String remark) {
        TransactionRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("交易记录不存在");
        }

        Integer entityStatus = mapDtoStatusToEntity(status);
        record.setStatus(entityStatus);
        if (entityStatus != null && entityStatus == 1) {
            record.setCompletedTime(LocalDateTime.now());
        }
        appendRemark(record, remark);
        baseMapper.updateById(record);
    }

    @Override
    public TransactionRecordDTO getTransactionByBusinessId(String businessId) {
        TransactionRecord record = transactionRecordMapper.selectByRelatedBusinessId(businessId);
        return record == null ? null : convertToDTO(record);
    }

    @Override
    public Map<String, Object> getEnterpriseTransactionStats(String enterpriseId, String startTime, String endTime) {
        Map<String, Object> stats = new HashMap<>();
        LambdaQueryWrapper<TransactionRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TransactionRecord::getEnterpriseId, enterpriseId)
                .eq(TransactionRecord::getStatus, 1);

        LocalDateTime start = parseDateTime(startTime, false);
        LocalDateTime end = parseDateTime(endTime, true);
        if (start != null) {
            queryWrapper.ge(TransactionRecord::getTransactionTime, start);
        }
        if (end != null) {
            queryWrapper.le(TransactionRecord::getTransactionTime, end);
        }

        List<TransactionRecord> records = baseMapper.selectList(queryWrapper);
        stats.put("totalCount", (long) records.size());

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Integer, Map<String, Object>> typeStats = new HashMap<>();
        for (TransactionRecord record : records) {
            totalAmount = totalAmount.add(safe(record.getAmount()));
            Integer type = record.getTransactionType();
            Map<String, Object> typeStat = typeStats.computeIfAbsent(type, key -> {
                Map<String, Object> item = new HashMap<>();
                item.put("count", 0L);
                item.put("amount", BigDecimal.ZERO);
                item.put("typeName", getTransactionTypeName(key));
                return item;
            });
            typeStat.put("count", ((Long) typeStat.get("count")) + 1L);
            typeStat.put("amount", ((BigDecimal) typeStat.get("amount")).add(safe(record.getAmount())));
        }
        stats.put("totalAmount", totalAmount);
        stats.put("typeStats", typeStats);
        return stats;
    }

    @Override
    public void exportTransactions(TransactionQueryDTO queryDTO, jakarta.servlet.http.HttpServletResponse response) {
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1000);
        List<TransactionRecordDTO> records = queryTransactionPage(queryDTO).getRecords();

        String fileName = URLEncoder.encode("交易流水.csv", StandardCharsets.UTF_8);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try {
            response.getWriter().write('\uFEFF');
            response.getWriter().write("流水号,交易类型,金额,交易前余额,交易后余额,业务描述,支付方式,状态,操作人,交易时间,备注\n");
            for (TransactionRecordDTO record : records) {
                response.getWriter().write(String.join(",",
                        csv(record.getTransactionNo()),
                        csv(record.getTransactionTypeName()),
                        csv(record.getAmount()),
                        csv(record.getBeforeBalance()),
                        csv(record.getAfterBalance()),
                        csv(record.getBusinessDesc()),
                        csv(record.getPaymentMethodName()),
                        csv(record.getStatusName()),
                        csv(record.getOperatorUserName()),
                        csv(record.getCreateTime()),
                        csv(record.getRemark())
                ));
                response.getWriter().write("\n");
            }
        } catch (IOException e) {
            throw new BusinessException("交易流水导出失败");
        }
    }

    private TransactionRecordDTO convertToDTO(TransactionRecord record) {
        TransactionRecordDTO dto = new TransactionRecordDTO();
        dto.setId(record.getId());
        dto.setTransactionNo(record.getTransactionNo());
        dto.setEnterpriseId(record.getEnterpriseId());
        dto.setEnterpriseName("");
        dto.setTransactionType(record.getTransactionType());
        dto.setTransactionTypeName(record.getTransactionTypeName());
        dto.setAmount(record.getAmount());
        dto.setBeforeBalance(record.getBalanceBefore());
        dto.setAfterBalance(record.getBalanceAfter());
        dto.setBeforeFrozenAmount(record.getFrozenBefore());
        dto.setAfterFrozenAmount(record.getFrozenAfter());
        dto.setBusinessType(getBusinessTypeFromString(record.getRelatedBusinessType()));
        dto.setBusinessTypeName(getBusinessTypeName(dto.getBusinessType()));
        dto.setBusinessId(record.getRelatedBusinessId());
        dto.setBusinessDesc(record.getRelatedBusinessDesc());
        dto.setPaymentMethod(getPaymentMethodFromString(record.getPaymentMethod()));
        dto.setPaymentMethodName(getPaymentMethodName(dto.getPaymentMethod()));
        dto.setPaymentNo(record.getPaymentNo());
        dto.setStatus(mapEntityStatusToDto(record.getStatus()));
        dto.setStatusName(getStatusName(dto.getStatus()));
        dto.setRemark(record.getRemark());
        dto.setOperatorUserId(record.getOperatorUserId());
        dto.setOperatorUserName(record.getOperatorUserName());
        dto.setCreateTime(record.getCreatedAt());
        dto.setUpdateTime(record.getUpdatedAt());
        return dto;
    }

    private String getTransactionTypeName(Integer type) {
        if (type == null) {
            return "其他";
        }
        switch (type) {
            case 1:
                return "充值";
            case 2:
                return "投保扣费";
            case 3:
                return "退费";
            case 4:
                return "退款";
            case 5:
                return "调整";
            case 6:
                return "冻结";
            case 7:
                return "解冻";
            default:
                return "其他";
        }
    }

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private String getBusinessTypeString(Integer type) {
        if (type == null) {
            return "other";
        }
        switch (type) {
            case 1:
                return "policy";
            case 2:
                return "refund";
            case 3:
                return "adjustment";
            default:
                return "other";
        }
    }

    private Integer getBusinessTypeFromString(String type) {
        if (type == null) {
            return 4;
        }
        switch (type) {
            case "policy":
                return 1;
            case "refund":
                return 2;
            case "adjustment":
                return 3;
            default:
                return 4;
        }
    }

    private String getBusinessTypeName(Integer type) {
        if (type == null) {
            return "其他";
        }
        switch (type) {
            case 1:
                return "投保";
            case 2:
                return "退保";
            case 3:
                return "手工调整";
            default:
                return "其他";
        }
    }

    private String getPaymentMethodString(Integer method) {
        if (method == null) {
            return "wallet_balance";
        }
        switch (method) {
            case 1:
                return "wechat_pay";
            case 2:
                return "bank_transfer";
            case 3:
                return "wallet_balance";
            default:
                return "wallet_balance";
        }
    }

    private Integer getPaymentMethodFromString(String method) {
        if (method == null) {
            return 3;
        }
        switch (method) {
            case "wechat_pay":
            case "alipay":
                return 1;
            case "bank_transfer":
                return 2;
            case "wallet_balance":
                return 3;
            default:
                return 3;
        }
    }

    private String getPaymentMethodName(Integer method) {
        if (method == null) {
            return "余额支付";
        }
        switch (method) {
            case 1:
                return "在线支付";
            case 2:
                return "银行转账";
            case 3:
                return "余额支付";
            default:
                return "余额支付";
        }
    }

    private Integer mapDtoStatusToEntity(Integer status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case 0:
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            default:
                return status;
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
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
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
                return "待处理";
            case 1:
                return "处理中";
            case 2:
                return "成功";
            case 3:
                return "失败";
            case 4:
                return "已取消";
            default:
                return "未知";
        }
    }

    private void appendRemark(TransactionRecord record, String remark) {
        if (!isNotBlank(remark)) {
            return;
        }
        if (isNotBlank(record.getRemark())) {
            record.setRemark(record.getRemark() + ";" + remark);
        } else {
            record.setRemark(remark);
        }
    }

    private BigDecimal safe(BigDecimal value) {
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

    private String generateTransactionNo() {
        return "TR" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
}
