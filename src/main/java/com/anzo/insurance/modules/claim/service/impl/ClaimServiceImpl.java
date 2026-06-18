package com.anzo.insurance.modules.claim.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.common.security.SecurityUtil;
import com.anzo.insurance.modules.claim.dto.*;
import com.anzo.insurance.modules.claim.entity.Claim;
import com.anzo.insurance.modules.claim.entity.ClaimProcessRecord;
import com.anzo.insurance.modules.claim.repository.ClaimMapper;
import com.anzo.insurance.modules.claim.repository.ClaimProcessRecordMapper;
import com.anzo.insurance.modules.claim.service.ClaimService;
import com.anzo.insurance.modules.message.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 理赔服务实现类（简化版）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {
    
    private final ClaimMapper claimMapper;
    private final ClaimProcessRecordMapper claimProcessRecordMapper;
    private final NotificationService notificationService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public ClaimResponseDTO createClaim(ClaimCreateDTO dto) {
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
            throw operationFailed();
        }

        appendProcessRecord(claim, null, claim.getStatus(), "CREATE", "提交理赔报案", null, null, false);
        sendClaimNotificationSafe(claim, "CREATED", "理赔 " + claim.getClaimNo() + " 已提交，请等待后续处理。");
        
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
            throw operationFailed();
        }

        appendProcessRecord(claim, claim.getStatus(), claim.getStatus(), "UPDATE", "更新理赔信息", null, null, false);
        sendClaimNotificationSafe(claim, "UPDATED", "理赔 " + claim.getClaimNo() + " 信息已更新。");
        
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
    public void exportClaims(ClaimQueryDTO queryDTO, HttpServletResponse response) {
        queryDTO.setPage(1);
        queryDTO.setSize(1000);
        List<ClaimResponseDTO> records = queryClaims(queryDTO).getRecords();

        String fileName = URLEncoder.encode("理赔列表.csv", StandardCharsets.UTF_8);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try {
            response.getWriter().write('\uFEFF');
            response.getWriter().write(
                    "理赔编号,保单号,被保人,出险类型,理赔金额,货币,报案日期,出险日期,当前状态,材料状态,处理人,审核人,赔付金额,赔付时间,拒赔原因,撤回原因\n");
            for (ClaimResponseDTO record : records) {
                response.getWriter().write(String.join(",",
                        csv(record.getClaimNo()),
                        csv(record.getPolicyNo()),
                        csv(record.getInsuredName()),
                        csv(record.getAccidentTypeName()),
                        csv(record.getClaimAmount()),
                        csv(record.getCurrency()),
                        csv(record.getReportDate()),
                        csv(record.getAccidentDate()),
                        csv(record.getStatusName()),
                        csv(record.getMaterialStatusName()),
                        csv(record.getHandlerUserName()),
                        csv(record.getReviewUserName()),
                        csv(record.getPaymentAmount()),
                        csv(record.getPaymentAt()),
                        csv(record.getRejectReason()),
                        csv(record.getWithdrawReason())
                ));
                response.getWriter().write("\n");
            }
        } catch (IOException e) {
            throw new BusinessException("理赔列表导出失败");
        }
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
        
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        List<Claim> monthlyClaims = claimMapper.findByReportDateRange(enterpriseId, firstDayOfMonth, now);
        dto.setMonthlyNewCount(monthlyClaims.size());
        
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
        
        String previousStatus = claim.getStatus();
        claim.setMaterialStatus("COMPLETE");
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw operationFailed();
        }

        appendProcessRecord(claim, previousStatus, claim.getStatus(), "MATERIAL_SUBMIT",
                "提交理赔材料，共" + materials.size() + "项", null, null, false);
        sendClaimNotificationSafe(claim, "MATERIAL_SUBMITTED",
                "理赔 " + claim.getClaimNo() + " 已补充提交材料，共 " + materials.size() + " 项。");
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO reviewMaterials(String claimId, ClaimMaterialReviewDTO reviewDTO) {
        Claim claim = getClaimEntity(claimId);
        String previousStatus = claim.getStatus();
        
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
            throw operationFailed();
        }

        appendProcessRecord(claim, previousStatus, claim.getStatus(), "MATERIAL_REVIEW",
                reviewDTO.getReviewRemark(), null, null, true);
        sendClaimNotificationSafe(claim, reviewDTO.isApproved() ? "MATERIAL_APPROVED" : "MATERIAL_REJECTED",
                "理赔 " + claim.getClaimNo() + " 材料审核" + (reviewDTO.isApproved() ? "已通过" : "未通过")
                        + (reviewDTO.getReviewRemark() != null ? "，说明：" + reviewDTO.getReviewRemark() : ""));
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO assignHandler(String claimId, String handlerUserId, String handlerUserName) {
        Claim claim = getClaimEntity(claimId);
        String previousStatus = claim.getStatus();
        
        claim.setHandlerUserId(handlerUserId);
        claim.setHandlerUserName(handlerUserName);
        claim.setHandlerAssignedAt(LocalDateTime.now());
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw operationFailed();
        }

        appendProcessRecord(claim, previousStatus, claim.getStatus(), "ASSIGN_HANDLER",
                "指派处理人: " + handlerUserName, null, null, true);
        sendClaimNotificationSafe(claim, "HANDLER_ASSIGNED",
                "理赔 " + claim.getClaimNo() + " 已指派处理人：" + handlerUserName + "。");
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO submitSurveyReport(String claimId, String surveyReport) {
        Claim claim = getClaimEntity(claimId);
        String previousStatus = claim.getStatus();
        
        claim.setSurveyReport(surveyReport);
        claim.setSurveyAt(LocalDateTime.now());
        claim.setSurveyUserId(SecurityUtil.getCurrentUserId());
        claim.setSurveyUserName(SecurityUtil.getCurrentUsername());
        claim.setStatus("NEGOTIATING");
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        claim.setStatusChangedAt(LocalDateTime.now());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw operationFailed();
        }

        appendProcessRecord(claim, previousStatus, claim.getStatus(), "SURVEY_REPORT",
                surveyReport, null, null, true);
        sendClaimNotificationSafe(claim, "SURVEY_SUBMITTED",
                "理赔 " + claim.getClaimNo() + " 查勘报告已提交。");
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO reviewClaim(String claimId, String reviewRemark, boolean approved) {
        Claim claim = getClaimEntity(claimId);
        String previousStatus = claim.getStatus();
        
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
            throw operationFailed();
        }

        appendProcessRecord(claim, previousStatus, claim.getStatus(), "CLAIM_REVIEW",
                reviewRemark, null, null, true);
        sendClaimNotificationSafe(claim, approved ? "APPROVED" : "REJECTED",
                "理赔 " + claim.getClaimNo() + " 审核" + (approved ? "已通过" : "未通过")
                        + (reviewRemark != null ? "，意见：" + reviewRemark : ""));
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO processPayment(String claimId, BigDecimal paymentAmount) {
        Claim claim = getClaimEntity(claimId);
        String previousStatus = claim.getStatus();
        
        claim.setPaymentAmount(paymentAmount);
        claim.setPaymentCurrency(claim.getCurrency());
        claim.setPaymentAt(LocalDateTime.now());
        claim.setPaymentUserId(SecurityUtil.getCurrentUserId());
        claim.setPaymentUserName(SecurityUtil.getCurrentUsername());
        claim.setStatus("PAID");
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        claim.setStatusChangedAt(LocalDateTime.now());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw operationFailed();
        }

        appendProcessRecord(claim, previousStatus, claim.getStatus(), "PAYMENT",
                "赔付金额: " + paymentAmount, null, null, true);
        sendClaimNotificationSafe(claim, "PAID",
                "理赔 " + claim.getClaimNo() + " 已完成赔付，金额：" + paymentAmount + " " + claim.getCurrency() + "。");
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO rejectClaim(String claimId, String rejectReason) {
        Claim claim = getClaimEntity(claimId);
        String previousStatus = claim.getStatus();
        
        claim.setRejectReason(rejectReason);
        claim.setStatus("REJECTED");
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        claim.setStatusChangedAt(LocalDateTime.now());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw operationFailed();
        }

        appendProcessRecord(claim, previousStatus, claim.getStatus(), "REJECT",
                rejectReason, null, null, true);
        sendClaimNotificationSafe(claim, "REJECTED",
                "理赔 " + claim.getClaimNo() + " 已拒赔，原因：" + rejectReason);
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO withdrawClaim(String claimId, String withdrawReason) {
        Claim claim = getClaimEntity(claimId);
        String previousStatus = claim.getStatus();
        
        claim.setWithdrawReason(withdrawReason);
        claim.setStatus("WITHDRAWN");
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        claim.setStatusChangedAt(LocalDateTime.now());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw operationFailed();
        }

        appendProcessRecord(claim, previousStatus, claim.getStatus(), "WITHDRAW",
                withdrawReason, null, null, false);
        sendClaimNotificationSafe(claim, "WITHDRAWN",
                "理赔 " + claim.getClaimNo() + " 已撤回，原因：" + withdrawReason);
        
        return convertToResponseDTO(claim);
    }
    
    @Override
    public List<ClaimProcessRecordDTO> getProcessRecords(String claimId) {
        getClaimEntity(claimId);
        return claimProcessRecordMapper.findByClaimId(claimId).stream()
                .map(this::convertToProcessRecordDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ClaimResponseDTO addProcessRecord(String claimId, ClaimProcessRecordCreateDTO recordDTO) {
        Claim claim = getClaimEntity(claimId);
        String previousStatus = claim.getStatus();
        
        if (recordDTO.getToStatus() != null) {
            claim.setStatus(recordDTO.getToStatus());
            claim.setStatusChangedAt(LocalDateTime.now());
        }
        
        claim.setUpdatedBy(SecurityUtil.getCurrentUserId());
        
        int result = claimMapper.updateById(claim);
        if (result <= 0) {
            throw operationFailed();
        }

        appendProcessRecord(
                claim,
                recordDTO.getFromStatus() != null ? recordDTO.getFromStatus() : previousStatus,
                recordDTO.getToStatus() != null ? recordDTO.getToStatus() : claim.getStatus(),
                recordDTO.getProcessType(),
                recordDTO.getProcessContent(),
                recordDTO.getAttachmentUrl(),
                recordDTO.getAttachmentName(),
                Boolean.TRUE.equals(recordDTO.getInternal())
        );
        
        return convertToResponseDTO(claim);
    }
    
    private Claim getClaimEntity(String claimId) {
        Claim claim = claimMapper.selectById(claimId);
        if (claim == null || claim.getDeleted()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ErrorCode.RESOURCE_NOT_FOUND.getMessage());
        }
        
        if (!claim.getEnterpriseId().equals(SecurityUtil.getCurrentEnterpriseId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
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
        
        dto.setStatusName(getStatusName(claim.getStatus()));
        dto.setAccidentTypeName(getAccidentTypeName(claim.getAccidentType()));
        dto.setMaterialStatusName(getMaterialStatusName(claim.getMaterialStatus()));
        
        return dto;
    }

    private ClaimProcessRecordDTO convertToProcessRecordDTO(ClaimProcessRecord record) {
        ClaimProcessRecordDTO dto = new ClaimProcessRecordDTO();
        BeanUtils.copyProperties(record, dto);
        return dto;
    }

    private void appendProcessRecord(Claim claim, String fromStatus, String toStatus, String processType,
                                     String processContent, String attachmentUrl, String attachmentName,
                                     boolean internal) {
        ClaimProcessRecord record = new ClaimProcessRecord();
        record.setClaimId(claim.getId());
        record.setFromStatus(fromStatus);
        record.setToStatus(toStatus);
        record.setProcessType(processType != null ? processType : "GENERAL");
        record.setProcessContent(processContent);
        record.setAttachmentUrl(attachmentUrl);
        record.setAttachmentName(attachmentName);
        record.setOperatorUserId(SecurityUtil.getCurrentUserId());
        record.setOperatorUserName(SecurityUtil.getCurrentUsername());
        record.setOperationTime(LocalDateTime.now());
        record.setInternal(internal);
        claimProcessRecordMapper.insert(record);
    }

    private BusinessException operationFailed() {
        return new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), ErrorCode.OPERATION_FAILED.getMessage());
    }

    private void sendClaimNotificationSafe(Claim claim, String notificationType, String content) {
        try {
            notificationService.sendClaimNotification(
                    claim.getEnterpriseId(),
                    claim.getHandlerUserId(),
                    claim.getId(),
                    notificationType,
                    content
            );
        } catch (Exception e) {
            log.warn("发送理赔通知失败: claimId={}, error={}", claim.getId(), e.getMessage());
        }
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

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
