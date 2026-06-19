package com.anzo.insurance.modules.finance.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.modules.finance.dto.WalletDTO;
import com.anzo.insurance.modules.finance.dto.WalletQueryDTO;
import com.anzo.insurance.modules.finance.dto.WalletUpdateDTO;
import com.anzo.insurance.modules.finance.entity.TransactionRecord;
import com.anzo.insurance.modules.finance.entity.Wallet;
import com.anzo.insurance.modules.finance.repository.TransactionRecordMapper;
import com.anzo.insurance.modules.finance.repository.WalletMapper;
import com.anzo.insurance.modules.finance.service.WalletService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 钱包服务实现类
 */
@Service
public class WalletServiceImpl extends ServiceImpl<WalletMapper, Wallet> implements WalletService {

    private static final int OP_RECHARGE = 1;
    private static final int OP_DEDUCT = 2;
    private static final int OP_FREEZE = 3;
    private static final int OP_UNFREEZE = 4;
    private static final int OP_ADJUST = 5;

    @Resource
    private TransactionRecordMapper transactionRecordMapper;

    @Override
    public WalletDTO getWallet(Long id) {
        Wallet wallet = baseMapper.selectById(id);
        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }
        return convertToDTO(wallet);
    }

    @Override
    public WalletDTO getWalletByEnterpriseId(Long enterpriseId) {
        Wallet wallet = baseMapper.selectByEnterpriseId(enterpriseId);
        if (wallet == null) {
            return initWallet(enterpriseId, null);
        }
        return convertToDTO(wallet);
    }

    @Override
    public IPage<WalletDTO> queryWalletPage(WalletQueryDTO queryDTO) {
        Page<Wallet> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Wallet> queryWrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getEnterpriseId() != null) {
            queryWrapper.eq(Wallet::getEnterpriseId, queryDTO.getEnterpriseId());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Wallet::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getMinBalance() != null) {
            queryWrapper.ge(Wallet::getAvailableBalance, queryDTO.getMinBalance());
        }
        if (queryDTO.getMaxBalance() != null) {
            queryWrapper.le(Wallet::getAvailableBalance, queryDTO.getMaxBalance());
        }

        LocalDateTime startTime = parseDateTime(queryDTO.getStartTime(), false);
        LocalDateTime endTime = parseDateTime(queryDTO.getEndTime(), true);
        if (startTime != null) {
            queryWrapper.ge(Wallet::getCreatedAt, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(Wallet::getCreatedAt, endTime);
        }

        if ("balance".equals(queryDTO.getSortField())) {
            if ("asc".equalsIgnoreCase(queryDTO.getSortOrder())) {
                queryWrapper.orderByAsc(Wallet::getAvailableBalance);
            } else {
                queryWrapper.orderByDesc(Wallet::getAvailableBalance);
            }
        } else {
            queryWrapper.orderByDesc(Wallet::getCreatedAt);
        }

        return baseMapper.selectPage(page, queryWrapper).convert(this::convertToDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletDTO initWallet(Long enterpriseId, String enterpriseName) {
        Wallet existingWallet = baseMapper.selectByEnterpriseId(enterpriseId);
        if (existingWallet != null) {
            return convertToDTO(existingWallet);
        }

        Wallet wallet = new Wallet();
        wallet.setEnterpriseId(enterpriseId);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setFrozenBalance(BigDecimal.ZERO);
        wallet.setTotalRechargeAmount(BigDecimal.ZERO);
        wallet.setTotalConsumptionAmount(BigDecimal.ZERO);
        wallet.setTotalRefundAmount(BigDecimal.ZERO);
        wallet.setCurrency("CNY");
        wallet.setMinBalanceAlert(BigDecimal.ZERO);
        wallet.setBalanceAlertEnabled(Boolean.FALSE);
        wallet.setStatus(0);
        baseMapper.insert(wallet);
        return convertToDTO(wallet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletDTO updateWalletBalance(WalletUpdateDTO updateDTO) {
        return applyWalletOperation(
                updateDTO.getId(),
                updateDTO.getAmount(),
                updateDTO.getOperationType(),
                updateDTO.getBusinessType(),
                updateDTO.getBusinessId(),
                updateDTO.getBusinessDesc(),
                updateDTO.getRemark(),
                null,
                null,
                false
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletDTO freezeAmount(Long walletId, BigDecimal amount, Long businessId, String businessDesc, String remark) {
        return applyWalletOperation(walletId, amount, OP_FREEZE, 1, businessId, businessDesc, remark, null, null, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletDTO unfreezeAmount(Long walletId, BigDecimal amount, Long businessId, String businessDesc, String remark) {
        return applyWalletOperation(walletId, amount, OP_UNFREEZE, 1, businessId, businessDesc, remark, null, null, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletDTO recharge(Long walletId, BigDecimal amount, Integer paymentMethod, String paymentNo, String remark) {
        return applyWalletOperation(walletId, amount, OP_RECHARGE, 4, null, "账户充值", remark, paymentMethod, paymentNo, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletDTO deduct(Long walletId, BigDecimal amount, Long businessId, String businessDesc, String remark) {
        return applyWalletOperation(walletId, amount, OP_DEDUCT, 1, businessId, businessDesc, remark, 3, null, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletDTO refund(Long walletId, BigDecimal amount, Long businessId, String businessDesc, String remark) {
        return applyWalletOperation(walletId, amount, OP_RECHARGE, 2, businessId, businessDesc, remark, null, null, true);
    }

    private WalletDTO applyWalletOperation(
            Long walletId,
            BigDecimal amount,
            Integer operationType,
            Integer businessType,
            Long businessId,
            String businessDesc,
            String remark,
            Integer paymentMethod,
            String paymentNo,
            boolean refundOperation
    ) {
        Wallet wallet = baseMapper.selectById(walletId);
        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("操作金额必须大于0");
        }

        BigDecimal beforeAvailable = safe(wallet.getAvailableBalance());
        BigDecimal beforeFrozen = safe(wallet.getFrozenBalance());
        BigDecimal signedAmount;
        Integer transactionType;

        wallet.setAvailableBalance(beforeAvailable);
        wallet.setFrozenBalance(beforeFrozen);
        wallet.setTotalRechargeAmount(safe(wallet.getTotalRechargeAmount()));
        wallet.setTotalConsumptionAmount(safe(wallet.getTotalConsumptionAmount()));
        wallet.setTotalRefundAmount(safe(wallet.getTotalRefundAmount()));

        switch (operationType) {
            case OP_RECHARGE:
                wallet.setAvailableBalance(beforeAvailable.add(amount));
                if (refundOperation) {
                    wallet.setTotalRefundAmount(wallet.getTotalRefundAmount().add(amount));
                    transactionType = 4;
                } else {
                    wallet.setTotalRechargeAmount(wallet.getTotalRechargeAmount().add(amount));
                    transactionType = 1;
                }
                signedAmount = amount;
                break;
            case OP_DEDUCT:
                if (beforeAvailable.compareTo(amount) < 0) {
                    throw new BusinessException("余额不足");
                }
                wallet.setAvailableBalance(beforeAvailable.subtract(amount));
                wallet.setTotalConsumptionAmount(wallet.getTotalConsumptionAmount().add(amount));
                transactionType = 2;
                signedAmount = amount.negate();
                break;
            case OP_FREEZE:
                if (beforeAvailable.compareTo(amount) < 0) {
                    throw new BusinessException("余额不足，无法冻结");
                }
                wallet.setAvailableBalance(beforeAvailable.subtract(amount));
                wallet.setFrozenBalance(beforeFrozen.add(amount));
                transactionType = 6;
                signedAmount = amount.negate();
                break;
            case OP_UNFREEZE:
                if (beforeFrozen.compareTo(amount) < 0) {
                    throw new BusinessException("冻结金额不足，无法解冻");
                }
                wallet.setFrozenBalance(beforeFrozen.subtract(amount));
                wallet.setAvailableBalance(beforeAvailable.add(amount));
                transactionType = 7;
                signedAmount = amount;
                break;
            case OP_ADJUST:
                if (beforeAvailable.add(amount).compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException("调整后余额不能小于0");
                }
                wallet.setAvailableBalance(beforeAvailable.add(amount));
                transactionType = 5;
                signedAmount = amount;
                break;
            default:
                throw new BusinessException("不支持的操作类型");
        }

        baseMapper.updateById(wallet);
        createTransactionRecord(
                wallet,
                signedAmount,
                transactionType,
                businessType,
                businessId,
                businessDesc,
                remark,
                paymentMethod,
                paymentNo,
                beforeAvailable,
                beforeFrozen
        );
        return convertToDTO(wallet);
    }

    private void createTransactionRecord(
            Wallet wallet,
            BigDecimal amount,
            Integer transactionType,
            Integer businessType,
            Long businessId,
            String businessDesc,
            String remark,
            Integer paymentMethod,
            String paymentNo,
            BigDecimal beforeAvailable,
            BigDecimal beforeFrozen
    ) {
        TransactionRecord record = new TransactionRecord();
        record.setTransactionNo(generateTransactionNo());
        record.setEnterpriseId(wallet.getEnterpriseId());
        record.setTransactionType(transactionType);
        record.setAmount(amount);
        record.setCurrency(wallet.getCurrency());
        record.setBalanceBefore(beforeAvailable);
        record.setBalanceAfter(safe(wallet.getAvailableBalance()));
        record.setFrozenBefore(beforeFrozen);
        record.setFrozenAfter(safe(wallet.getFrozenBalance()));
        record.setRelatedBusinessType(mapBusinessType(businessType));
        record.setRelatedBusinessId(businessId);
        record.setRelatedBusinessDesc(businessDesc);
        record.setStatus(1);
        record.setTransactionTime(LocalDateTime.now());
        record.setCompletedTime(LocalDateTime.now());
        record.setPaymentMethod(mapPaymentMethod(paymentMethod));
        record.setPaymentNo(paymentNo);
        record.setRemark(remark);
        record.setIsManual(Boolean.TRUE);
        record.setAuditStatus(0);
        transactionRecordMapper.insert(record);
    }

    private WalletDTO convertToDTO(Wallet wallet) {
        WalletDTO dto = new WalletDTO();
        dto.setId(wallet.getId());
        dto.setEnterpriseId(wallet.getEnterpriseId());
        dto.setEnterpriseName("");
        dto.setBalance(safe(wallet.getAvailableBalance()).add(safe(wallet.getFrozenBalance())));
        dto.setFrozenAmount(safe(wallet.getFrozenBalance()));
        dto.setAvailableBalance(safe(wallet.getAvailableBalance()));
        dto.setTotalRechargeAmount(safe(wallet.getTotalRechargeAmount()));
        dto.setTotalConsumeAmount(safe(wallet.getTotalConsumptionAmount()));
        dto.setTotalRefundAmount(safe(wallet.getTotalRefundAmount()));
        dto.setStatus(wallet.getStatus());
        dto.setStatusName(getStatusName(wallet.getStatus()));
        dto.setLastTransactionTime(getLastTransactionTime(wallet.getEnterpriseId()));
        dto.setCreateTime(wallet.getCreatedAt());
        dto.setUpdateTime(wallet.getUpdatedAt());
        return dto;
    }

    private LocalDateTime getLastTransactionTime(Long enterpriseId) {
        if (enterpriseId == null) {
            return null;
        }
        LambdaQueryWrapper<TransactionRecord> queryWrapper = new LambdaQueryWrapper<TransactionRecord>()
                .eq(TransactionRecord::getEnterpriseId, enterpriseId)
                .orderByDesc(TransactionRecord::getTransactionTime)
                .last("limit 1");
        List<TransactionRecord> records = transactionRecordMapper.selectList(queryWrapper);
        return records.isEmpty() ? null : records.get(0).getTransactionTime();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String mapBusinessType(Integer businessType) {
        if (businessType == null) {
            return "other";
        }
        switch (businessType) {
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

    private String mapPaymentMethod(Integer paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }
        switch (paymentMethod) {
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

    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "正常";
            case 1:
                return "冻结";
            case 2:
                return "注销";
            default:
                return "未知";
        }
    }

    private LocalDateTime parseDateTime(String text, boolean endOfDay) {
        if (!isNotBlank(text)) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ignored) {
        }
        try {
            LocalDate date = LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String generateTransactionNo() {
        return "TR" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
}
