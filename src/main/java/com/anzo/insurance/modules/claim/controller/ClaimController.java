package com.anzo.insurance.modules.claim.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.claim.dto.*;
import com.anzo.insurance.modules.claim.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 理赔管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
@Tag(name = "理赔管理", description = "理赔管理相关接口")
public class ClaimController {
    
    private final ClaimService claimService;
    
    @PostMapping
    @Operation(summary = "创建理赔")
    public ApiResponse<ClaimResponseDTO> createClaim(@Valid @RequestBody ClaimCreateDTO dto) {
        log.info("创建理赔: {}", dto);
        ClaimResponseDTO result = claimService.createClaim(dto);
        return ApiResponse.success("理赔创建成功", result);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新理赔")
    public ApiResponse<ClaimResponseDTO> updateClaim(
            @PathVariable String id,
            @Valid @RequestBody ClaimUpdateDTO dto) {
        log.info("更新理赔: id={}, data={}", id, dto);
        ClaimResponseDTO result = claimService.updateClaim(id, dto);
        return ApiResponse.success("理赔更新成功", result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取理赔详情")
    public ApiResponse<ClaimResponseDTO> getClaim(@PathVariable String id) {
        log.info("获取理赔详情: id={}", id);
        ClaimResponseDTO result = claimService.getClaim(id);
        return ApiResponse.success("查询成功", result);
    }
    
    @GetMapping
    @Operation(summary = "分页查询理赔")
    public ApiResponse<Object> queryClaims(@Valid ClaimQueryDTO queryDTO) {
        log.info("查询理赔列表: {}", queryDTO);
        return ApiResponse.success("查询成功", claimService.queryClaims(queryDTO));
    }
    
    @GetMapping("/policy/{policyNo}")
    @Operation(summary = "根据保单号查询理赔列表")
    public ApiResponse<List<ClaimResponseDTO>> getClaimsByPolicy(
            @PathVariable String policyNo) {
        log.info("根据保单号查询理赔: policyNo={}", policyNo);
        List<ClaimResponseDTO> result = claimService.getClaimsByPolicy(policyNo);
        return ApiResponse.success("查询成功", result);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取理赔统计信息")
    public ApiResponse<ClaimStatisticsDTO> getClaimStatistics() {
        log.info("获取理赔统计信息");
        ClaimStatisticsDTO result = claimService.getClaimStatistics();
        return ApiResponse.success("查询成功", result);
    }
    
    @PostMapping("/{id}/materials")
    @Operation(summary = "提交理赔材料")
    public ApiResponse<ClaimResponseDTO> submitMaterials(
            @PathVariable String id,
            @RequestBody List<ClaimMaterialCreateDTO> materials) {
        log.info("提交理赔材料: id={}, materials={}", id, materials.size());
        ClaimResponseDTO result = claimService.submitMaterials(id, materials);
        return ApiResponse.success("材料提交成功", result);
    }
    
    @PutMapping("/{id}/materials/review")
    @Operation(summary = "审核理赔材料")
    public ApiResponse<ClaimResponseDTO> reviewMaterials(
            @PathVariable String id,
            @RequestBody ClaimMaterialReviewDTO reviewDTO) {
        log.info("审核理赔材料: id={}, review={}", id, reviewDTO);
        ClaimResponseDTO result = claimService.reviewMaterials(id, reviewDTO);
        return ApiResponse.success("材料审核完成", result);
    }
    
    @PutMapping("/{id}/assign-handler")
    @Operation(summary = "指派处理人")
    public ApiResponse<ClaimResponseDTO> assignHandler(
            @PathVariable String id,
            @RequestBody AssignHandlerDTO assignDTO) {
        log.info("指派处理人: id={}, handler={}", id, assignDTO);
        ClaimResponseDTO result = claimService.assignHandler(id, assignDTO.getHandlerUserId(), assignDTO.getHandlerUserName());
        return ApiResponse.success("处理人指派成功", result);
    }
    
    @PutMapping("/{id}/survey-report")
    @Operation(summary = "提交查勘报告")
    public ApiResponse<ClaimResponseDTO> submitSurveyReport(
            @PathVariable String id,
            @RequestBody SurveyReportDTO surveyDTO) {
        log.info("提交查勘报告: id={}", id);
        ClaimResponseDTO result = claimService.submitSurveyReport(id, surveyDTO.getSurveyReport());
        return ApiResponse.success("查勘报告提交成功", result);
    }
    
    @PutMapping("/{id}/review")
    @Operation(summary = "审核理赔")
    public ApiResponse<ClaimResponseDTO> reviewClaim(
            @PathVariable String id,
            @RequestBody ClaimReviewDTO reviewDTO) {
        log.info("审核理赔: id={}, review={}", id, reviewDTO);
        ClaimResponseDTO result = claimService.reviewClaim(id, reviewDTO.getReviewRemark(), reviewDTO.isApproved());
        return ApiResponse.success("理赔审核完成", result);
    }
    
    @PutMapping("/{id}/payment")
    @Operation(summary = "处理赔付")
    public ApiResponse<ClaimResponseDTO> processPayment(
            @PathVariable String id,
            @RequestBody PaymentDTO paymentDTO) {
        log.info("处理赔付: id={}, amount={}", id, paymentDTO.getPaymentAmount());
        ClaimResponseDTO result = claimService.processPayment(id, paymentDTO.getPaymentAmount());
        return ApiResponse.success("赔付处理成功", result);
    }
    
    @PutMapping("/{id}/reject")
    @Operation(summary = "拒赔")
    public ApiResponse<ClaimResponseDTO> rejectClaim(
            @PathVariable String id,
            @RequestBody RejectClaimDTO rejectDTO) {
        log.info("拒赔: id={}, reason={}", id, rejectDTO.getRejectReason());
        ClaimResponseDTO result = claimService.rejectClaim(id, rejectDTO.getRejectReason());
        return ApiResponse.success("拒赔处理完成", result);
    }
    
    @PutMapping("/{id}/withdraw")
    @Operation(summary = "撤回理赔")
    public ApiResponse<ClaimResponseDTO> withdrawClaim(
            @PathVariable String id,
            @RequestBody WithdrawClaimDTO withdrawDTO) {
        log.info("撤回理赔: id={}, reason={}", id, withdrawDTO.getWithdrawReason());
        ClaimResponseDTO result = claimService.withdrawClaim(id, withdrawDTO.getWithdrawReason());
        return ApiResponse.success("理赔撤回成功", result);
    }
    
    @GetMapping("/{id}/process-records")
    @Operation(summary = "获取理赔处理记录")
    public ApiResponse<List<ClaimProcessRecordDTO>> getProcessRecords(@PathVariable String id) {
        log.info("获取理赔处理记录: id={}", id);
        List<ClaimProcessRecordDTO> result = claimService.getProcessRecords(id);
        return ApiResponse.success("查询成功", result);
    }
    
    @PostMapping("/{id}/process-records")
    @Operation(summary = "添加处理记录")
    public ApiResponse<ClaimResponseDTO> addProcessRecord(
            @PathVariable String id,
            @RequestBody ClaimProcessRecordCreateDTO recordDTO) {
        log.info("添加处理记录: id={}, record={}", id, recordDTO);
        ClaimResponseDTO result = claimService.addProcessRecord(id, recordDTO);
        return ApiResponse.success("处理记录添加成功", result);
    }
    
    // 辅助DTO类
    public static class AssignHandlerDTO {
        private String handlerUserId;
        private String handlerUserName;
        
        public String getHandlerUserId() { return handlerUserId; }
        public void setHandlerUserId(String handlerUserId) { this.handlerUserId = handlerUserId; }
        public String getHandlerUserName() { return handlerUserName; }
        public void setHandlerUserName(String handlerUserName) { this.handlerUserName = handlerUserName; }
    }
    
    public static class SurveyReportDTO {
        private String surveyReport;
        
        public String getSurveyReport() { return surveyReport; }
        public void setSurveyReport(String surveyReport) { this.surveyReport = surveyReport; }
    }
    
    public static class ClaimReviewDTO {
        private String reviewRemark;
        private boolean approved;
        
        public String getReviewRemark() { return reviewRemark; }
        public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
        public boolean isApproved() { return approved; }
        public void setApproved(boolean approved) { this.approved = approved; }
    }
    
    public static class PaymentDTO {
        private BigDecimal paymentAmount;
        
        public BigDecimal getPaymentAmount() { return paymentAmount; }
        public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
    }
    
    public static class RejectClaimDTO {
        private String rejectReason;
        
        public String getRejectReason() { return rejectReason; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    }
    
    public static class WithdrawClaimDTO {
        private String withdrawReason;
        
        public String getWithdrawReason() { return withdrawReason; }
        public void setWithdrawReason(String withdrawReason) { this.withdrawReason = withdrawReason; }
    }
}