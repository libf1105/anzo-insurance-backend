package com.anzo.insurance.modules.policy.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.policy.dto.*;
import com.anzo.insurance.modules.policy.service.PolicyService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * 保单管理控制器
 */
@Api(tags = "保单管理")
@RestController
@RequestMapping("/api/policy")
public class PolicyController {

    @Resource
    private PolicyService policyService;

    @ApiOperation("分页查询保单列表")
    @GetMapping("/list")
    public ApiResponse<IPage<PolicyDetailDTO>> queryPolicyPage(@Valid PolicyQueryDTO queryDTO) {
        IPage<PolicyDetailDTO> result = policyService.queryPolicyPage(queryDTO);
        return ApiResponse.success(result);
    }

    @ApiOperation("获取保单详情")
    @GetMapping("/{id}")
    public ApiResponse<PolicyDetailDTO> getPolicyDetail(
            @ApiParam("保单ID") @PathVariable String id) {
        PolicyDetailDTO policyDetail = policyService.getPolicyDetail(id);
        return ApiResponse.success(policyDetail);
    }

    @ApiOperation("修改保单信息")
    @PutMapping("/update")
    public ApiResponse<Void> updatePolicy(@Validated @RequestBody PolicyUpdateDTO updateDTO) {
        policyService.updatePolicy(updateDTO);
        return ApiResponse.success();
    }

    @ApiOperation("撤销或退保保单")
    @PostMapping("/cancel")
    public ApiResponse<Void> cancelPolicy(@Validated @RequestBody PolicyCancelDTO cancelDTO) {
        policyService.cancelPolicy(cancelDTO);
        return ApiResponse.success();
    }

    @ApiOperation("批量操作保单")
    @PostMapping("/batch-operate")
    public ApiResponse<Void> batchOperatePolicies(@Validated @RequestBody BatchOperationDTO batchOperationDTO) {
        policyService.batchOperatePolicies(batchOperationDTO);
        return ApiResponse.success();
    }

    @ApiOperation("下载保单文件")
    @GetMapping("/{id}/download")
    public ApiResponse<String> downloadPolicyFile(
            @ApiParam("保单ID") @PathVariable String id) {
        String fileUrl = policyService.downloadPolicyFile(id);
        return ApiResponse.success(fileUrl);
    }

    @ApiOperation("导出保单列表")
    @PostMapping("/export")
    public void exportPolicies(@RequestBody PolicyQueryDTO queryDTO, HttpServletResponse response) {
        policyService.exportPolicies(queryDTO, response);
    }
}
