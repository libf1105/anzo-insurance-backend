package com.anzo.insurance.modules.finance.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.modules.finance.dto.TransactionRecordDTO;
import com.anzo.insurance.modules.finance.dto.TransactionQueryDTO;
import com.anzo.insurance.modules.finance.entity.TransactionRecord;
import com.anzo.insurance.modules.finance.repository.TransactionRecordMapper;
import com.anzo.insurance.modules.finance.service.TransactionRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
        
        if (queryDTO.getEnterpriseId() != null) {
            queryWrapper.eq(TransactionRecord::getEnterpriseId, queryDTO.getEnterpriseId());
        }
        if (queryDTO.getTransactionType() != null) {
            queryWrapper.eq(TransactionRecord::getTransactionType, queryDTO.getTransactionType());
        }
        if (queryDTO.getBusinessId() != null) {
            queryWrapper.eq(TransactionRecord::getRelatedBusinessId, queryDTO.getBusinessId());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(TransactionRecord::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getPaymentMethod() != null) {
            queryWrapper.eq(TransactionRecord::getPaymentMethod, getPaymentMethodString(queryDTO.getPaymentMethod()));
        }
        if (queryDTO.getTransactionNo() != null) {
            queryWrapper.like(TransactionRecord::getTransactionNo, queryDTO.getTransactionNo());
        }
        if (queryDTO.getPaymentNo() != null) {
            queryWrapper.like(TransactionRecord::getPaymentNo, queryDTO.getPaymentNo());
        }
        if (queryDTO.getMinAmount() != null) {
            queryWrapper.ge(TransactionRecord::getAmount, queryDTO.getMinAmount());
        }
        if (queryDTO.getMaxAmount() != null) {
            queryWrapper.le(TransactionRecord::getAmount, queryDTO.getMaxAmount());
        }
        if (queryDTO.getOperatorUserId() != null) {
            queryWrapper.eq(TransactionRecord::getOperatorUserId, queryDTO.getOperatorUserId());
        }
        
        // 时间范围查询
        if (queryDTO.getStartTime() != null && queryDTO.getEndTime() != null) {
            queryWrapper.between(TransactionRecord::getCreatedAt, queryDTO.getStartTime(), queryDTO.getEndTime());
        }
        
        // 排序
        if ("amount".equals(queryDTO.getSortField())) {
            if ("asc".equals(queryDTO.getSortOrder())) {
                queryWrapper.orderByAsc(TransactionRecord::getAmount);
            } else {
                queryWrapper.orderByDesc(TransactionRecord::getAmount);
            }
        } else {
            queryWrapper.orderByDesc(TransactionRecord::getCreatedAt);
        }
        
        IPage<TransactionRecord> recordPage = baseMapper.selectPage(page, queryWrapper);
        return recordPage.convert(this::convertToDTO);
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
        record.setPaymentMethod(getPaymentMethodString(transactionRecordDTO.getPaymentMethod()));
        record.setPaymentNo(transactionRecordDTO.getPaymentNo());
        record.setStatus(transactionRecordDTO.getStatus() != null ? transactionRecordDTO.getStatus() : 1); // 默认成功
        record.setRemark(transactionRecordDTO.getRemark());
        record.setOperatorUserId(transactionRecordDTO.getOperatorUserId());
        record.setTransactionTime(LocalDateTime.now());
        record.setCurrency("CNY");
        record.setIsManual(false);
        record.setAuditStatus(0); // 无需审核
        record.setRelatedBusinessType(getBusinessTypeString(transactionRecordDTO.getBusinessType()));
        
        baseMapper.insert(record);
        return convertToDTO(record);
    }

    @Override
    public void updateTransactionStatus(String id, Integer status, String remark) {
        TransactionRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("交易记录不存在");
        }
        
        record.setStatus(status);
        if (status == 1) { // 成功
            record.setCompletedTime(LocalDateTime.now());
        }
        if (remark != null) {
            record.setRemark(record.getRemark() + ";" + remark);
        }
        
        baseMapper.updateById(record);
    }

    @Override
    public TransactionRecordDTO getTransactionByBusinessId(String businessId) {
        TransactionRecord record = baseMapper.selectOne(new LambdaQueryWrapper<TransactionRecord>()
                .eq(TransactionRecord::getRelatedBusinessId, businessId)
                .orderByDesc(TransactionRecord::getCreatedAt));
        if (record == null) {
            return null;
        }
        return convertToDTO(record);
    }

    @Override
    public Map<String, Object> getEnterpriseTransactionStats(String enterpriseId, String startTime, String endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<TransactionRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TransactionRecord::getEnterpriseId, enterpriseId);
        queryWrapper.eq(TransactionRecord::getStatus, 1); // 成功状态
        
        if (startTime != null && endTime != null) {
            queryWrapper.between(TransactionRecord::getCreatedAt, startTime, endTime);
        }
        
        // 总交易笔数
        Long totalCount = baseMapper.selectCount(queryWrapper);
        stats.put("totalCount", totalCount);
        
        // 总交易金额
        queryWrapper.select("SUM(amount) as totalAmount");
        Map<String, Object> result = baseMapper.selectMaps(queryWrapper).get(0);
        BigDecimal totalAmount = result.get("totalAmount") != null ? 
                (BigDecimal) result.get("totalAmount") : BigDecimal.ZERO;
        stats.put("totalAmount", totalAmount);
        
        // 按交易类型统计
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TransactionRecord::getEnterpriseId, enterpriseId);
        queryWrapper.eq(TransactionRecord::getStatus, 1);
        if (startTime != null && endTime != null) {
            queryWrapper.between(TransactionRecord::getCreatedAt, startTime, endTime);
        }
        queryWrapper.select("transaction_type, COUNT(*) as count, SUM(amount) as amount");
        queryWrapper.groupBy("transaction_type");
        
        Map<Integer, Map<String, Object>> typeStats = new HashMap<>();
        for (Map<String, Object> row : baseMapper.selectMaps(queryWrapper)) {
            Integer type = (Integer) row.get("transaction_type");
            Long count = (Long) row.get("count");
            BigDecimal amount = (BigDecimal) row.get("amount");
            
            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("count", count);
            typeStat.put("amount", amount);
            typeStat.put("typeName", getTransactionTypeName(type));
            typeStats.put(type, typeStat);
        }
        stats.put("typeStats", typeStats);
        
        return stats;
    }

    @Override
    public void exportTransactions(TransactionQueryDTO queryDTO, jakarta.servlet.http.HttpServletResponse response) {
        // TODO: 实现导出功能
        // 这里可以集成Excel导出等工具
    }

    /**
     * 转换为DTO
     */
    private TransactionRecordDTO convertToDTO(TransactionRecord record) {
        TransactionRecordDTO dto = new TransactionRecordDTO();
        dto.setId(record.getId());
        dto.setTransactionNo(record.getTransactionNo());
        dto.setEnterpriseId(record.getEnterpriseId());
        dto.setEnterpriseName(""); // TODO: 需要从企业服务获取企业名称
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
        dto.setStatus(record.getStatus());
        dto.setStatusName(record.getStatusName());
        dto.setRemark(record.getRemark());
        dto.setOperatorUserId(record.getOperatorUserId());
        dto.setOperatorUserName(record.getOperatorUserName());
        dto.setCreateTime(record.getCreatedAt());
        dto.setUpdateTime(record.getUpdatedAt());
        return dto;
    }

    /**
     * 获取交易类型名称
     */
    private String getTransactionTypeName(Integer type) {
        switch (type) {
            case 1: return "充值";
            case 2: return "投保扣费";
            case 3: return "退费";
            case 4: return "退款";
            case 5: return "调整";
            case 6: return "冻结";
            case 7: return "解冻";
            default: return "其他";
        }
    }

    /**
     * 获取业务类型字符串
     */
    private String getBusinessTypeString(Integer type) {
        if (type == null) return "other";
        switch (type) {
            case 1: return "policy";
            case 2: return "refund";
            case 3: return "adjustment";
            case 4: return "other";
            default: return "other";
        }
    }

    /**
     * 从字符串获取业务类型
     */
    private Integer getBusinessTypeFromString(String type) {
        if (type == null) return 4;
        switch (type) {
            case "policy": return 1;
            case "refund": return 2;
            case "adjustment": return 3;
            case "other": return 4;
            default: return 4;
        }
    }

    /**
     * 获取业务类型名称
     */
    private String getBusinessTypeName(Integer type) {
        if (type == null) return "其他";
        switch (type) {
            case 1: return "投保";
            case 2: return "退保";
            case 3: return "手工调整";
            case 4: return "其他";
            default: return "其他";
        }
    }

    /**
     * 获取支付方式字符串
     */
    private String getPaymentMethodString(Integer method) {
        if (method == null) return "wallet_balance";
        switch (method) {
            case 1: return "online_payment";
            case 2: return "bank_transfer";
            case 3: return "wallet_balance";
            default: return "wallet_balance";
        }
    }

    /**
     * 从字符串获取支付方式
     */
    private Integer getPaymentMethodFromString(String method) {
        if (method == null) return 3;
        switch (method) {
            case "online_payment": return 1;
            case "bank_transfer": return 2;
            case "wallet_balance": return 3;
            default: return 3;
        }
    }

    /**
     * 获取支付方式名称
     */
    private String getPaymentMethodName(Integer method) {
        if (method == null) return "余额支付";
        switch (method) {
            case 1: return "在线支付";
            case 2: return "银行转账";
            case 3: return "余额支付";
            default: return "余额支付";
        }
    }

    /**
     * 生成ID（简化版）
     */
    private String generateId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成交易流水号（简化版）
     */
    private String generateTransactionNo() {
        return "TR" + System.currentTimeMillis() + 
                String.format("%04d", new java.util.Random().nextInt(10000));
    }
}