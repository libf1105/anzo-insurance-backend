package com.anzo.insurance.modules.enterprise.service.impl;

import cn.hutool.core.util.StrUtil;
import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.modules.auth.entity.Enterprise;
import com.anzo.insurance.modules.auth.entity.User;
import com.anzo.insurance.modules.auth.repository.EnterpriseRepository;
import com.anzo.insurance.modules.auth.repository.UserRepository;
import com.anzo.insurance.modules.enterprise.dto.EnterpriseQueryDTO;
import com.anzo.insurance.modules.enterprise.dto.EnterpriseReviewDTO;
import com.anzo.insurance.modules.enterprise.dto.EnterpriseUpdateDTO;
import com.anzo.insurance.modules.enterprise.service.EnterpriseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
/**
 * 企业服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;

    @Override
    public Enterprise getEnterpriseById(Long enterpriseId) {
        return enterpriseRepository.selectById(enterpriseId);
    }

    @Override
    public Enterprise getCurrentEnterprise() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        return enterpriseRepository.selectById(user.getEnterpriseId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEnterprise(Long enterpriseId, EnterpriseUpdateDTO updateDTO) {
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        // 更新企业信息
        enterprise.setName(updateDTO.getName());
        enterprise.setContactName(updateDTO.getContactName());
        enterprise.setContactPhone(updateDTO.getContactPhone());
        enterprise.setContactEmail(updateDTO.getContactEmail());
        enterprise.setAddress(updateDTO.getAddress());
        enterprise.setDescription(updateDTO.getDescription());
        enterprise.setUpdatedAt(LocalDateTime.now());

        enterpriseRepository.updateById(enterprise);
        log.info("企业信息已更新: enterpriseId={}, name={}", enterpriseId, updateDTO.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentEnterprise(EnterpriseUpdateDTO updateDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        updateEnterprise(user.getEnterpriseId(), updateDTO);
    }

    @Override
    public Page<Enterprise> queryEnterprises(EnterpriseQueryDTO queryDTO, Pageable pageable) {
        LambdaQueryWrapper<Enterprise> queryWrapper = Wrappers.lambdaQuery(Enterprise.class)
                .eq(StrUtil.isNotBlank(queryDTO.getStatus()), Enterprise::getStatus, queryDTO.getStatus())
                .like(StrUtil.isNotBlank(queryDTO.getName()), Enterprise::getName, queryDTO.getName())
                .eq(StrUtil.isNotBlank(queryDTO.getCreditCode()), Enterprise::getCreditCode, queryDTO.getCreditCode())
                .like(StrUtil.isNotBlank(queryDTO.getContactName()), Enterprise::getContactName, queryDTO.getContactName())
                .eq(StrUtil.isNotBlank(queryDTO.getContactPhone()), Enterprise::getContactPhone, queryDTO.getContactPhone())
                .eq(Enterprise::getDeleted, false)
                .orderByDesc(Enterprise::getCreatedAt);

        // 时间范围查询
        if (StrUtil.isNotBlank(queryDTO.getRegisterStartTime()) && StrUtil.isNotBlank(queryDTO.getRegisterEndTime())) {
            // 这里需要根据实际的时间处理逻辑进行实现
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Enterprise> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageable.getPageNumber() + 1, pageable.getPageSize());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Enterprise> result =
            enterpriseRepository.selectPage(page, queryWrapper);

        return new PageImpl<>(
            result.getRecords(),
            pageable,
            result.getTotal()
        );
    }

    @Override
    public List<Enterprise> getPendingReviewEnterprises() {
        return enterpriseRepository.findPendingReviewEnterprises();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewEnterprise(EnterpriseReviewDTO reviewDTO) {
        Enterprise enterprise = enterpriseRepository.selectById(reviewDTO.getEnterpriseId());
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        if (!Enterprise.Status.PENDING_REVIEW.getValue().equals(enterprise.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "企业状态不是待审核");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        enterprise.setReviewBy(username);
        enterprise.setReviewAt(LocalDateTime.now());
        enterprise.setReviewRemark(reviewDTO.getRemark());

        if ("APPROVED".equals(reviewDTO.getReviewResult())) {
            enterprise.setStatus(Enterprise.Status.ACTIVE.getValue());
            log.info("企业审核通过: enterpriseId={}, name={}", reviewDTO.getEnterpriseId(), enterprise.getName());
        } else if ("REJECTED".equals(reviewDTO.getReviewResult())) {
            enterprise.setStatus(Enterprise.Status.REJECTED.getValue());
            log.info("企业审核拒绝: enterpriseId={}, name={}, reason={}", 
                reviewDTO.getEnterpriseId(), enterprise.getName(), reviewDTO.getRejectReason());
        }

        enterpriseRepository.updateById(enterprise);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableEnterprise(Long enterpriseId) {
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        enterprise.setStatus(Enterprise.Status.ACTIVE.getValue());
        enterprise.setUpdatedAt(LocalDateTime.now());
        enterpriseRepository.updateById(enterprise);
        
        log.info("企业已启用: enterpriseId={}, name={}", enterpriseId, enterprise.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableEnterprise(Long enterpriseId, String reason) {
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        enterprise.setStatus(Enterprise.Status.DISABLED.getValue());
        enterprise.setUpdatedAt(LocalDateTime.now());
        enterprise.setReviewRemark(reason);
        enterpriseRepository.updateById(enterprise);
        
        log.info("企业已禁用: enterpriseId={}, name={}, reason={}", enterpriseId, enterprise.getName(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEnterpriseBalance(Long enterpriseId, String amount, String type) {
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        BigDecimal amountDecimal = new BigDecimal(amount);
        BigDecimal currentBalance = enterprise.getBalance() != null ? enterprise.getBalance() : BigDecimal.ZERO;
        BigDecimal currentTotalRecharged = enterprise.getTotalRecharged() != null ? enterprise.getTotalRecharged() : BigDecimal.ZERO;
        BigDecimal currentTotalConsumed = enterprise.getTotalConsumed() != null ? enterprise.getTotalConsumed() : BigDecimal.ZERO;

        if ("RECHARGE".equals(type)) {
            enterprise.setBalance(currentBalance.add(amountDecimal));
            enterprise.setTotalRecharged(currentTotalRecharged.add(amountDecimal));
            log.info("企业余额充值: enterpriseId={}, amount={}, newBalance={}", 
                enterpriseId, amount, enterprise.getBalance());
        } else if ("CONSUME".equals(type)) {
            if (currentBalance.compareTo(amountDecimal) < 0) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE.getCode(), "企业余额不足");
            }
            enterprise.setBalance(currentBalance.subtract(amountDecimal));
            enterprise.setTotalConsumed(currentTotalConsumed.add(amountDecimal));
            log.info("企业余额消费: enterpriseId={}, amount={}, newBalance={}", 
                enterpriseId, amount, enterprise.getBalance());
        }

        enterprise.setUpdatedAt(LocalDateTime.now());
        enterpriseRepository.updateById(enterprise);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeEnterpriseBalance(Long enterpriseId, String amount) {
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        BigDecimal amountDecimal = new BigDecimal(amount);
        BigDecimal currentBalance = enterprise.getBalance() != null ? enterprise.getBalance() : BigDecimal.ZERO;
        BigDecimal currentFrozenBalance = enterprise.getFrozenBalance() != null ? enterprise.getFrozenBalance() : BigDecimal.ZERO;

        if (currentBalance.compareTo(amountDecimal) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE.getCode(), "企业余额不足");
        }

        enterprise.setBalance(currentBalance.subtract(amountDecimal));
        enterprise.setFrozenBalance(currentFrozenBalance.add(amountDecimal));
        enterprise.setUpdatedAt(LocalDateTime.now());
        enterpriseRepository.updateById(enterprise);

        log.info("企业余额已冻结: enterpriseId={}, amount={}, balance={}, frozenBalance={}", 
            enterpriseId, amount, enterprise.getBalance(), enterprise.getFrozenBalance());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeEnterpriseBalance(Long enterpriseId, String amount) {
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        BigDecimal amountDecimal = new BigDecimal(amount);
        BigDecimal currentBalance = enterprise.getBalance() != null ? enterprise.getBalance() : BigDecimal.ZERO;
        BigDecimal currentFrozenBalance = enterprise.getFrozenBalance() != null ? enterprise.getFrozenBalance() : BigDecimal.ZERO;

        if (currentFrozenBalance.compareTo(amountDecimal) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "冻结余额不足");
        }

        enterprise.setBalance(currentBalance.add(amountDecimal));
        enterprise.setFrozenBalance(currentFrozenBalance.subtract(amountDecimal));
        enterprise.setUpdatedAt(LocalDateTime.now());
        enterpriseRepository.updateById(enterprise);

        log.info("企业余额已解冻: enterpriseId={}, amount={}, balance={}, frozenBalance={}", 
            enterpriseId, amount, enterprise.getBalance(), enterprise.getFrozenBalance());
    }

    @Override
    public EnterpriseStatistics getEnterpriseStatistics(Long enterpriseId) {
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        // 获取企业用户数量
        LambdaQueryWrapper<User> userQuery = Wrappers.lambdaQuery(User.class)
                .eq(User::getEnterpriseId, enterpriseId)
                .eq(User::getDeleted, false);
        int totalUsers = userRepository.selectCount(userQuery).intValue();

        // 这里可以添加获取保单数量、保费总额等统计逻辑
        // 目前先返回基本的企业信息

        return new EnterpriseStatistics() {
            @Override
            public Integer getTotalUsers() {
                return totalUsers;
            }

            @Override
            public Integer getActivePolicies() {
                // TODO: 实现获取活跃保单数量
                return 0;
            }

            @Override
            public String getTotalPremium() {
                // TODO: 实现获取保费总额
                return "0";
            }

            @Override
            public String getBalance() {
                return enterprise.getBalance() != null ? enterprise.getBalance().toString() : "0";
            }

            @Override
            public String getFrozenBalance() {
                return enterprise.getFrozenBalance() != null ? enterprise.getFrozenBalance().toString() : "0";
            }
        };
    }

    @Override
    public boolean checkEnterpriseStatus(Long enterpriseId) {
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            return false;
        }
        return Enterprise.Status.ACTIVE.getValue().equals(enterprise.getStatus());
    }
}
