package com.anzo.insurance.modules.finance.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.finance.dto.BillDTO;
import com.anzo.insurance.modules.finance.dto.BillPayDTO;
import com.anzo.insurance.modules.finance.dto.BillQueryDTO;
import com.anzo.insurance.modules.finance.dto.BillReconcileDTO;
import com.anzo.insurance.modules.finance.service.BillService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * 账单管理控制器
 */
@Api(tags = "账单管理")
@RestController
@RequestMapping("/api/finance/bill")
public class BillController {

    @Resource
    private BillService billService;

    @ApiOperation("获取账单详情")
    @GetMapping("/{id}")
    public ApiResponse<BillDTO> getBillDetail(
            @ApiParam("账单ID") @PathVariable String id) {
        BillDTO billDetail = billService.getBill(id);
        return ApiResponse.success(billDetail);
    }

    @ApiOperation("分页查询账单列表")
    @GetMapping("/list")
    public ApiResponse<IPage<BillDTO>> queryBillPage(@Valid BillQueryDTO queryDTO) {
        IPage<BillDTO> result = billService.queryBillPage(queryDTO);
        return ApiResponse.success(result);
    }

    @ApiOperation("生成月度账单")
    @PostMapping("/generate-monthly")
    public ApiResponse<BillDTO> generateMonthlyBill(
            @ApiParam("企业ID") @RequestParam String enterpriseId,
            @ApiParam("账单周期（YYYY-MM）") @RequestParam String billingPeriod) {
        BillDTO bill = billService.generateMonthlyBill(enterpriseId, billingPeriod);
        return ApiResponse.success(bill);
    }

    @ApiOperation("支付账单")
    @PostMapping("/pay")
    public ApiResponse<BillDTO> payBill(@Validated @RequestBody BillPayDTO billPayDTO) {
        BillDTO bill = billService.payBill(billPayDTO);
        return ApiResponse.success(bill);
    }

    @ApiOperation("对账账单")
    @PostMapping("/reconcile")
    public ApiResponse<BillDTO> reconcileBill(@Validated @RequestBody BillReconcileDTO billReconcileDTO) {
        BillDTO bill = billService.reconcileBill(billReconcileDTO);
        return ApiResponse.success(bill);
    }

    @ApiOperation("更新账单状态")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateBillStatus(
            @ApiParam("账单ID") @PathVariable String id,
            @ApiParam("账单状态（0-待生成，1-待付款，2-付款中，3-已付款，4-已逾期，5-已取消）") @RequestParam Integer status,
            @ApiParam("备注") @RequestParam(required = false) String remark) {
        billService.updateBillStatus(id, status, remark);
        return ApiResponse.success();
    }

    @ApiOperation("获取企业账单统计")
    @GetMapping("/stats/{enterpriseId}")
    public ApiResponse<Map<String, Object>> getEnterpriseBillStats(
            @ApiParam("企业ID") @PathVariable String enterpriseId,
            @ApiParam("账单周期（YYYY-MM）") @RequestParam(required = false) String billingPeriod) {
        Map<String, Object> stats = billService.getEnterpriseBillStats(enterpriseId, billingPeriod);
        return ApiResponse.success(stats);
    }

    @ApiOperation("批量生成月度账单")
    @PostMapping("/batch-generate")
    public ApiResponse<Void> batchGenerateMonthlyBills(
            @ApiParam("账单周期（YYYY-MM）") @RequestParam String billingPeriod) {
        billService.batchGenerateMonthlyBills(billingPeriod);
        return ApiResponse.success();
    }

    @ApiOperation("发送账单提醒")
    @PostMapping("/{id}/reminder")
    public ApiResponse<Void> sendBillReminder(
            @ApiParam("账单ID") @PathVariable String id) {
        billService.sendBillReminder(id);
        return ApiResponse.success();
    }

    @ApiOperation("下载账单文件")
    @GetMapping("/{id}/download")
    public ApiResponse<String> downloadBillFile(
            @ApiParam("账单ID") @PathVariable String id) {
        String fileUrl = billService.downloadBillFile(id);
        return ApiResponse.success(fileUrl, "账单文件下载地址");
    }

    @ApiOperation("导出账单列表")
    @PostMapping("/export")
    public ApiResponse<Void> exportBills(@RequestBody BillQueryDTO queryDTO, 
            jakarta.servlet.http.HttpServletResponse response) {
        billService.exportBills(queryDTO, response);
        return ApiResponse.success();
    }
}