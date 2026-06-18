package com.anzo.insurance.modules.insurance.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.insurance.dto.*;
import com.anzo.insurance.modules.insurance.entity.InsuranceApplication;
import com.anzo.insurance.modules.insurance.service.InsuranceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 投保业务控制器
 */
@Tag(name = "投保管理", description = "货物运输险投保相关接口")
@RestController
@RequestMapping("/insurance")
@RequiredArgsConstructor
public class InsuranceController {
    
    private final InsuranceService insuranceService;
    
    @Operation(summary = "保存投保步骤1 - 基础信息")
    @PostMapping("/step1")
    public ApiResponse<InsuranceApplication> saveStep1(@Valid @RequestBody ApplicationStep1DTO dto) {
        InsuranceApplication application = insuranceService.saveStep1(dto);
        return ApiResponse.success(application);
    }
    
    @Operation(summary = "保存投保步骤2 - 运输信息")
    @PostMapping("/step2")
    public ApiResponse<InsuranceApplication> saveStep2(@Valid @RequestBody ApplicationStep2DTO dto) {
        InsuranceApplication application = insuranceService.saveStep2(dto);
        return ApiResponse.success(application);
    }
    
    @Operation(summary = "保存投保步骤3 - 货物信息")
    @PostMapping("/step3")
    public ApiResponse<InsuranceApplication> saveStep3(@Valid @RequestBody ApplicationStep3DTO dto) {
        InsuranceApplication application = insuranceService.saveStep3(dto);
        return ApiResponse.success(application);
    }
    
    @Operation(summary = "保费试算")
    @PostMapping("/premium-calculate")
    public ApiResponse<PremiumCalculateResponseDTO> calculatePremium(@Valid @RequestBody PremiumCalculateDTO dto) {
        PremiumCalculateResponseDTO result = insuranceService.calculatePremium(dto);
        return ApiResponse.success(result);
    }
    
    @Operation(summary = "提交投保申请")
    @PostMapping("/{applicationId}/submit")
    public ApiResponse<InsuranceApplication> submitApplication(
            @PathVariable String applicationId,
            @Valid @RequestBody SubmitApplicationDTO dto) {
        InsuranceApplication application = insuranceService.submitApplication(applicationId, dto);
        return ApiResponse.success(application);
    }
    
    @Operation(summary = "获取投保申请详情")
    @GetMapping("/{id}")
    public ApiResponse<InsuranceApplication> getApplication(@PathVariable String id) {
        InsuranceApplication application = insuranceService.getApplication(id);
        return ApiResponse.success(application);
    }
    
    @Operation(summary = "获取投保申请列表")
    @GetMapping("/applications")
    public ApiResponse<Page<InsuranceApplication>> getApplications(@Valid ApplicationQueryDTO query) {
        Page<InsuranceApplication> applications = insuranceService.getApplications(query);
        return ApiResponse.success(applications);
    }
    
    @Operation(summary = "撤销投保申请")
    @PostMapping("/{applicationId}/cancel")
    public ApiResponse<InsuranceApplication> cancelApplication(
            @PathVariable String applicationId,
            @RequestParam String reason) {
        InsuranceApplication application = insuranceService.cancelApplication(applicationId, reason);
        return ApiResponse.success(application);
    }
    
    @Operation(summary = "保存投保草稿")
    @PostMapping("/drafts")
    public ApiResponse<InsuranceDraftDTO> saveDraft(@Valid @RequestBody InsuranceDraftDTO dto) {
        InsuranceDraftDTO draft = insuranceService.saveDraft(dto);
        return ApiResponse.success(draft);
    }
    
    @Operation(summary = "获取投保草稿列表")
    @GetMapping("/drafts")
    public ApiResponse<List<InsuranceDraftDTO>> getDrafts() {
        List<InsuranceDraftDTO> drafts = insuranceService.getDrafts();
        return ApiResponse.success(drafts);
    }
    
    @Operation(summary = "删除投保草稿")
    @DeleteMapping("/drafts/{draftId}")
    public ApiResponse<Void> deleteDraft(@PathVariable String draftId) {
        insuranceService.deleteDraft(draftId);
        return ApiResponse.success();
    }
    
    @Operation(summary = "创建投保模板")
    @PostMapping("/templates")
    public ApiResponse<InsuranceTemplateDTO> createTemplate(@Valid @RequestBody InsuranceTemplateDTO dto) {
        InsuranceTemplateDTO template = insuranceService.createTemplate(dto);
        return ApiResponse.success(template);
    }
    
    @Operation(summary = "获取投保模板列表")
    @GetMapping("/templates")
    public ApiResponse<List<InsuranceTemplateDTO>> getTemplates() {
        List<InsuranceTemplateDTO> templates = insuranceService.getTemplates();
        return ApiResponse.success(templates);
    }
    
    @Operation(summary = "删除投保模板")
    @DeleteMapping("/templates/{templateId}")
    public ApiResponse<Void> deleteTemplate(@PathVariable String templateId) {
        insuranceService.deleteTemplate(templateId);
        return ApiResponse.success();
    }
}