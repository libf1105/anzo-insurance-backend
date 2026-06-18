package com.anzo.insurance.modules.claim.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.common.security.SecurityUtil;
import com.anzo.insurance.modules.claim.dto.*;
import com.anzo.insurance.modules.claim.entity.Claim;
import com.anzo.insurance.modules.claim.repository.ClaimMapper;
import com.anzo.insurance.modules.claim.service.ClaimService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 理赔服务实现类（简化版）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {
    
    private final ClaimMapper claimMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public ClaimResponseDTO createClaim(ClaimCreateDTO dto) {
        // 生成理赔编号
        String claimNo = generateClaimNo();
        
        Claim claim = new Claim();
        BeanUtils.copyProperties(dto, claim);
        claim.setClaimNo(claimNo);
        claim.setEnterpriseId(SecurityUtil.getCurrentEnterpriseId());
        claim.setMaterialStatus("PENDING");
        claim.setStatus("REPORTED");
        claim.setCreatedBy(SecurityUtil.getCurrentUserId());
        
        int result = claimMapper.insert(claim);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO updateClaim(String claimId, ClaimUpdateDTO dto) {
        Claim claim = getClaimEntity(claimId);
        
        BeanUtils.copyProperties(dto, claim, "id", "enterpriseId", "claimNo", "policyId", "policyNo", "insuredName");
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    public ClaimResponseDTO getClaim(String claimId) {
        Claim claim = getClaimEntity(claimId);
        return convertToResponseDTO(claim);
    }
    
    @Override
    public Page<ClaimResponseDTO> queryClaims(ClaimQueryDTO queryDTO) {
        LambdaQueryWrapper<Claim> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Claim::getEnterpriseId, SecurityUtil.getCurrentEnterpriseId())
                    .eq(Claim::getDeleted, false);
        
        // 添加查询条件
        if (queryDTO.getSearchKeyword() != null && !queryDTO.getSearchKeyword().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like(Claim::getClaimNo, queryDTO.getSearchKeyword())
                .or()
                .like(Claim::getPolicyNo, queryDTO.getSearchKeyword())
            );
        }
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            queryWrapper.eq(Claim::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getAccidentType() != null && !queryDTO.getAccidentType().isEmpty()) {
            queryWrapper.eq(Claim::getAccidentType, queryDTO.getAccidentType());
        }
        if (queryDTO.getReportStartDate() != null) {
            queryWrapper.ge(Claim::getReportDate, queryDTO.getReportStartDate());
        }
        if (queryDTO.getReportEndDate() != null) {
            queryWrapper.le(Claim::getReportDate, queryDTO.getReportEndDate());
        }
        
        // 排序
        if ("reportDate".equals(queryDTO.getSortField())) {
            if ("desc".equals(queryDTO.getSortOrder())) {
                queryWrapper.orderByDesc(Claim::getReportDate);
            } else {
                queryWrapper.orderByAsc(Claim::getReportDate);
            }
        } else {
            queryWrapper.orderByDesc(Claim::getCreatedAt);
        }
        
        Page<Claim> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        Page<Claim> claimPage = claimMapper.selectPage(page, queryWrapper);
        
        Page<ClaimResponseDTO> resultPage = new Page<>();
        BeanUtils.copyProperties(claimPage, resultPage);
        
        List<ClaimResponseDTO> records = claimPage.getRecords().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        resultPage.setRecords(records);
        return resultPage;
    }
    
    @Override
    public List<ClaimResponseDTO> getClaimsByPolicy(String policyNo) {
        List<Claim> claims = claimMapper.findByPolicyNo(policyNo);
        return claims.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public ClaimStatisticsDTO getClaimStatistics() {
        String enterpriseId = SecurityUtil.getCurrentEnterpriseId();
        ClaimMapper.ClaimStatistics statistics = claimMapper.getClaimStatistics(enterpriseId);
        
        ClaimStatisticsDTO dto = new ClaimStatisticsDTO();
        if (statistics != null) {
            dto.setProcessingCount(statistics.getProcessingCount() != null ? statistics.getProcessingCount() : 0);
            dto.setPaidCount(statistics.getPaidCount() != null ? statistics.getPaidCount() : 0);
            dto.setTotalPaymentAmount(statistics.getTotalPaymentAmount() != null ? statistics.getTotalPaymentAmount() : BigDecimal.ZERO);
            dto.setPaymentRate(statistics.getPaymentRate() != null ? statistics.getPaymentRate() : BigDecimal.ZERO);
        }
        
        // 获取本月新增数量（简化实现）
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        List<Claim> monthlyClaims = claimMapper.findByReportDateRange(enterpriseId, firstDayOfMonth, now);
        dto.setMonthlyNewCount(monthlyClaims.size());
        
        // 获取其他统计数据
        List<Claim> allClaims = claimMapper.findByEnterpriseId(enterpriseId);
        dto.setTotalCount(allClaims.size());
        
        long rejectedCount = allClaims.stream().filter(c -> "REJECTED".equals(c.getStatus())).count();
        dto.setRejectedCount((int) rejectedCount);
        
        long withdrawnCount = allClaims.stream().filter(c -> "WITHDRAWN".equals(c.getStatus())).count();
        dto.setWithdrawnCount((int) withdrawnCount);
        
        return dto;
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO submitMaterials(String claimId, List<ClaimMaterialCreateDTO> materials) {
        Claim claim = getClaimEntity(claimId);
        
        // 更新材料状态
        claim.setMaterialStatus("COMPLETE");
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO reviewMaterials(String claimId, ClaimMaterialReviewDTO reviewDTO) {
        Claim claim = getClaimEntity(claimId);
        
        if (reviewDTO.isApproved()) {
            claim.setMaterialStatus("COMPLETE");
            claim.setStatus("MATERIAL_REVIEWING");
        } else {
            claim.setMaterialStatus("INCOMPLETE");
            claim.setMissingMaterials(reviewDTO.getReviewRemark());
            claim.setStatus("MATERIAL_INCOMPLETE");
        }
        
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        claim.setStatusChangedAt(LocalDateTime.now());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO assignHandler(String claimId, String handlerUserId, String handlerUserName) {
        Claim claim = getClaimEntity(claimId);
        
        claim.setHandlerUserId(handlerUserId);
        claim.setHandlerUserName(handlerUserName);
        claim.setHandlerAssignedAt(LocalDateTime.now());
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO submitSurveyReport(String claimId, String surveyReport) {
        Claim claim = getClaimEntity(claimId);
        
        claim.setSurveyReport(surveyReport);
        claim.setSurveyAt(LocalDateTime.now());
        claim.setSurveyUserId(SecurityUtil.getCurrentUserId());
        claim.setSurveyUserName(SecurityUtil.getCurrentUsername());
        claim.setStatus("NEGOTIATING");
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        claim.setStatusChangedAt(LocalDateTime.now());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO reviewClaim(String claimId, String reviewRemark, boolean approved) {
        Claim claim = getClaimEntity(claimId);
        
        claim.setReviewRemark(reviewRemark);
        claim.setReviewAt(LocalDateTime.now());
        claim.setReviewUserId(SecurityUtil.getCurrentUserId());
        claim.setReviewUserName(SecurityUtil.getCurrentUsername());
        
        if (approved) {
            claim.setStatus("SURVEYING");
        } else {
            claim.setStatus("REJECTED");
            claim.setRejectReason(reviewRemark);
        }
        
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        claim.setStatusChangedAt(LocalDateTime.now());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO processPayment(String claimId, BigDecimal paymentAmount) {
        Claim claim = getClaimEntity(claimId);
        
        claim.setPaymentAmount(paymentAmount);
        claim.setPaymentAt(LocalDateTime.now());
        claim.setPaymentUserId(SecurityUtil.getCurrentUserId());
        claim.setPaymentUserName(SecurityUtil.getCurrentUsername());
        claim.setStatus("PAID");
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        claim.setStatusChangedAt(LocalDateTime.now());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO rejectClaim(String claimId, String rejectReason) {
        Claim claim = getClaimEntity(claimId);
        
        claim.setRejectReason(rejectReason);
        claim.setStatus("REJECTED");
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        claim.setStatusChangedAt(LocalDateTime.now());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO withdrawClaim(String claimId, String withdrawReason) {
        Claim claim = getClaimEntity(claimId);
        
        claim.setWithdrawReason(withdrawReason);
        claim.setStatus("WITHDRAWN");
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        claim.setStatusChangedAt(LocalDateTime.now());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    public List<ClaimProcessRecordDTO> getProcessRecords(String claimId) {
        // 简化实现，返回空列表
        return List.of();
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO addProcessRecord(String claimId, ClaimProcessRecordCreateDTO recordDTO) {
        // 简化实现，只更新理赔状态
        Claim claim = getClaimEntity(claimId);
        
        if (recordDTO.getToStatus() != null) {
            claim.setStatus(recordDTO.getToStatus());
            claim.setStatusChangedAt(LocalDateTime.now());
        }
        
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        
        return convertToResponseDTO(claim);
    }
    
    private Claim getClaimEntity(String claimId) {
        Claim claim = claimMapper.selectById(claimId);
        if (claim == null || claim.getDeleted()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        
        // 检查权限：只能操作本企业的理赔
        if (!claim.getEnterpriseId().equals(SecurityUtil.getCurrentEnterpriseId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        return claim;
    }
    
    private String generateClaimNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String maxClaimNo = claimMapper.getMaxClaimNoByDate(dateStr);
        
        int sequence = 1;
        if (maxClaimNo != null && maxClaimNo.startsWith("CLM" + dateStr)) {
            String seqStr = maxClaimNo.substring(11); // CLM + yyyyMMdd = 11位
            try {
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        return String.format("CLM%s%04d", dateStr, sequence);
    }
    
    private ClaimResponseDTO convertToResponseDTO(Claim claim) {
        ClaimResponseDTO dto = new ClaimResponseDTO();
        BeanUtils.copyProperties(claim, dto);
        dto.setCreatedAt(claim.getCreatedAt());
        dto.setUpdatedAt(claim.getUpdatedAt());
        
        // 设置状态名称
        dto.setStatusName(getStatusName(claim.getStatus()));
        dto.setAccidentTypeName(getAccidentTypeName(claim.getAccidentType()));
        dto.setMaterialStatusName(getMaterialStatusName(claim.getMaterialStatus()));
        
        return dto;
    }
    
    private String getStatusName(String status) {
        switch (status) {
            case "REPORTED": return "已报案";
            case "MATERIAL_REVIEWING": return "材料审核中";
            case "MATERIAL_INCOMPLETE": return "待补充材料";
            case "SURVEYING": return "理赔查勘中";
            case "NEGOTIATING": return "赔付协商中";
            case "PAID": return "已赔付";
            case "REJECTED": return "已拒赔";
            case "WITHDRAWN": return "已撤回";
            default: return status;
        }
    }
    
    private String getAccidentTypeName(String accidentType) {
        switch (accidentType) {
            case "WATER_DAMAGE": return "水渍损";
            case "IMPACT_BREAKAGE": return "碰损破碎";
            case "THEFT": return "偷盗提货不着";
            case "DAMP_HEAT": return "受潮受热";
            case "OTHER": return "其他";
            default: return accidentType;
        }
    }
    
    private String getMaterialStatusName(String materialStatus) {
        switch (materialStatus) {
            case "PENDING": return "待提交";
            case "INCOMPLETE": return "不完整";
            case "COMPLETE": return "已完整";
            default: return materialStatus;
        }
    }
}