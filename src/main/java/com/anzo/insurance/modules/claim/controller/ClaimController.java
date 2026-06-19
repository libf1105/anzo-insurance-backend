package com.anzo.insurance.modules.claim.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.claim.dto.*;
import com.anzo.insurance.modules.claim.service.ClaimService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
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
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新理赔")
    public ApiResponse<ClaimResponseDTO> updateClaim(
            @PathVariable Long id,
            @Valid @RequestBody ClaimUpdateDTO dto) {
        log.info("更新理赔: id={}, data={}", id, dto);
        ClaimResponseDTO result = claimService.updateClaim(id, dto);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取理赔详情")
    public ApiResponse<ClaimResponseDTO> getClaim(@PathVariable Long id) {
        log.info("获取理赔详情: id={}", id);
        ClaimResponseDTO result = claimService.getClaim(id);
        return ApiResponse.success(result);
    }

    @GetMapping
    @Operation(summary = "分页查询理赔")
    public ApiResponse<Page<ClaimResponseDTO>> queryClaims(@Valid ClaimQueryDTO queryDTO) {
        log.info("查询理赔列表: {}", queryDTO);
        return ApiResponse.success(claimService.queryClaims(queryDTO));
    }

    @PostMapping("/export")
    @Operation(summary = "导出理赔列表")
    public void exportClaims(@RequestBody ClaimQueryDTO queryDTO, HttpServletResponse response) {
        log.info("导出理赔列表: {}", queryDTO);
        claimService.exportClaims(queryDTO, response);
    }

    @GetMapping("/policy/{policyNo}")
    @Operation(summary = "根据保单号查询理赔列表")
    public ApiResponse<List<ClaimResponseDTO>> getClaimsByPolicy(
            @PathVariable String policyNo) {
        log.info("根据保单号查询理赔: policyNo={}", policyNo);
        List<ClaimResponseDTO> result = claimService.getClaimsByPolicy(policyNo);
        return ApiResponse.success(result);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取理赔统计信息")
    public ApiResponse<ClaimStatisticsDTO> getClaimStatistics() {
        log.info("获取理赔统计信息");
        ClaimStatisticsDTO result = claimService.getClaimStatistics();
        return ApiResponse.success(result);
    }

    @PostMapping("/{id}/materials")
    @Operation(summary = "提交理赔材料")
    public ApiResponse<ClaimResponseDTO> submitMaterials(
            @PathVariable Long id,
            @RequestBody List<ClaimMaterialCreateDTO> materials) {
        log.info("提交理赔材料: id={}, materials={}", id, materials.size());
        ClaimResponseDTO result = claimService.submitMaterials(id, materials);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}/materials/review")
    @Operation(summary = "审核理赔材料")
    public ApiResponse<ClaimResponseDTO> reviewMaterials(
            @PathVariable Long id,
            @RequestBody ClaimMaterialReviewDTO reviewDTO) {
        log.info("审核理赔材料: id={}, review={}", id, reviewDTO);
        ClaimResponseDTO result = claimService.reviewMaterials(id, reviewDTO);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}/assign-handler")
    @Operation(summary = "指派处理人")
    public ApiResponse<ClaimResponseDTO> assignHandler(
            @PathVariable Long id,
            @RequestBody AssignHandlerDTO assignDTO) {
        log.info("指派处理人: id={}, handler={}", id, assignDTO);
        ClaimResponseDTO result = claimService.assignHandler(id, assignDTO.getHandlerUserId(),
                assignDTO.getHandlerUserName());
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}/survey-report")
    @Operation(summary = "提交查勘报告")
    public ApiResponse<ClaimResponseDTO> submitSurveyReport(
            @PathVariable Long id,
            @RequestBody SurveyReportDTO surveyDTO) {
        log.info("提交查勘报告: id={}", id);
        ClaimResponseDTO result = claimService.submitSurveyReport(id, surveyDTO.getSurveyReport());
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}/review")
    @Operation(summary = "审核理赔")
    public ApiResponse<ClaimResponseDTO> reviewClaim(
            @PathVariable Long id,
            @RequestBody ClaimReviewDTO reviewDTO) {
        log.info("审核理赔: id={}, review={}", id, reviewDTO);
        ClaimResponseDTO result = claimService.reviewClaim(id, reviewDTO.getReviewRemark(), reviewDTO.isApproved());
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}/payment")
    @Operation(summary = "处理赔付")
    public ApiResponse<ClaimResponseDTO> processPayment(
            @PathVariable Long id,
            @RequestBody PaymentDTO paymentDTO) {
        log.info("处理赔付: id={}, amount={}", id, paymentDTO.getPaymentAmount());
        ClaimResponseDTO result = claimService.processPayment(id, paymentDTO.getPaymentAmount());
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "拒赔")
    public ApiResponse<ClaimResponseDTO> rejectClaim(
            @PathVariable Long id,
            @RequestBody RejectClaimDTO rejectDTO) {
        log.info("拒赔: id={}, reason={}", id, rejectDTO.getRejectReason());
        ClaimResponseDTO result = claimService.rejectClaim(id, rejectDTO.getRejectReason());
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}/withdraw")
    @Operation(summary = "撤回理赔")
    public ApiResponse<ClaimResponseDTO> withdrawClaim(
            @PathVariable Long id,
            @RequestBody WithdrawClaimDTO withdrawDTO) {
        log.info("撤回理赔: id={}, reason={}", id, withdrawDTO.getWithdrawReason());
        ClaimResponseDTO result = claimService.withdrawClaim(id, withdrawDTO.getWithdrawReason());
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}/process-records")
    @Operation(summary = "获取理赔处理记录")
    public ApiResponse<List<ClaimProcessRecordDTO>> getProcessRecords(@PathVariable Long id) {
        log.info("获取理赔处理记录: id={}", id);
        List<ClaimProcessRecordDTO> result = claimService.getProcessRecords(id);
        return ApiResponse.success(result);
    }

    @PostMapping("/{id}/process-records")
    @Operation(summary = "添加处理记录")
    public ApiResponse<ClaimResponseDTO> addProcessRecord(
            @PathVariable Long id,
            @RequestBody ClaimProcessRecordCreateDTO recordDTO) {
        log.info("添加处理记录: id={}, record={}", id, recordDTO);
        ClaimResponseDTO result = claimService.addProcessRecord(id, recordDTO);
        return ApiResponse.success(result);
    }

    // 辅助DTO类
    public static class AssignHandlerDTO {
        private Long handlerUserId;
        private String handlerUserName;

        public Long getHandlerUserId() {
            return handlerUserId;
        }

        public void setHandlerUserId(Long handlerUserId) {
            this.handlerUserId = handlerUserId;
        }

        public String getHandlerUserName() {
            return handlerUserName;
        }

        public void setHandlerUserName(String handlerUserName) {
            this.handlerUserName = handlerUserName;
        }
    }

    public static class SurveyReportDTO {
        private String surveyReport;

        public String getSurveyReport() {
            return surveyReport;
        }

        public void setSurveyReport(String surveyReport) {
            this.surveyReport = surveyReport;
        }
    }

    public static class ClaimReviewDTO {
        private String reviewRemark;
        private boolean approved;

        public String getReviewRemark() {
            return reviewRemark;
        }

        public void setReviewRemark(String reviewRemark) {
            this.reviewRemark = reviewRemark;
        }

        public boolean isApproved() {
            return approved;
        }

        public void setApproved(boolean approved) {
            this.approved = approved;
        }
    }

    public static class PaymentDTO {
        private BigDecimal paymentAmount;

        public BigDecimal getPaymentAmount() {
            return paymentAmount;
        }

        public void setPaymentAmount(BigDecimal paymentAmount) {
            this.paymentAmount = paymentAmount;
        }
    }

    public static class RejectClaimDTO {
        private String rejectReason;

        public String getRejectReason() {
            return rejectReason;
        }

        public void setRejectReason(String rejectReason) {
            this.rejectReason = rejectReason;
        }
    }

    public static class WithdrawClaimDTO {
        private String withdrawReason;

        public String getWithdrawReason() {
            return withdrawReason;
        }

        public void setWithdrawReason(String withdrawReason) {
            this.withdrawReason = withdrawReason;
        }
    }
}
