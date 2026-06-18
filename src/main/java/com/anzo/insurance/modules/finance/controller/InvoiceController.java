package com.anzo.insurance.modules.finance.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.finance.dto.*;
import com.anzo.insurance.modules.finance.service.InvoiceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 发票管理控制器
 */
@Api(tags = "发票管理")
@RestController
@RequestMapping("/api/finance/invoice")
public class InvoiceController {

    @Resource
    private InvoiceService invoiceService;

    @ApiOperation("获取发票详情")
    @GetMapping("/{id}")
    public ApiResponse<InvoiceDTO> getInvoiceDetail(
            @ApiParam("发票ID") @PathVariable String id) {
        InvoiceDTO invoiceDetail = invoiceService.getInvoice(id);
        return ApiResponse.success(invoiceDetail);
    }

    @ApiOperation("分页查询发票列表")
    @GetMapping("/list")
    public ApiResponse<IPage<InvoiceDTO>> queryInvoicePage(@Valid InvoiceQueryDTO queryDTO) {
        IPage<InvoiceDTO> result = invoiceService.queryInvoicePage(queryDTO);
        return ApiResponse.success(result);
    }

    @ApiOperation("申请发票")
    @PostMapping("/apply")
    public ApiResponse<InvoiceDTO> applyInvoice(@Validated @RequestBody InvoiceApplyDTO applyDTO) {
        InvoiceDTO invoice = invoiceService.applyInvoice(applyDTO);
        return ApiResponse.success(invoice);
    }

    @ApiOperation("开票操作")
    @PostMapping("/issue")
    public ApiResponse<InvoiceDTO> issueInvoice(@Validated @RequestBody InvoiceUpdateDTO updateDTO) {
        InvoiceDTO invoice = invoiceService.issueInvoice(updateDTO);
        return ApiResponse.success(invoice);
    }

    @ApiOperation("作废发票")
    @PostMapping("/void")
    public ApiResponse<InvoiceDTO> voidInvoice(@Validated @RequestBody InvoiceUpdateDTO updateDTO) {
        InvoiceDTO invoice = invoiceService.voidInvoice(updateDTO);
        return ApiResponse.success(invoice);
    }

    @ApiOperation("更新邮寄信息")
    @PostMapping("/shipping-info")
    public ApiResponse<InvoiceDTO> updateShippingInfo(@Validated @RequestBody InvoiceUpdateDTO updateDTO) {
        InvoiceDTO invoice = invoiceService.updateShippingInfo(updateDTO);
        return ApiResponse.success(invoice);
    }

    @ApiOperation("更新发票状态")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateInvoiceStatus(
            @ApiParam("发票ID") @PathVariable String id,
            @ApiParam("发票状态（0-待开票，1-开票中，2-已开票，3-开票失败，4-已作废）") @RequestParam Integer status,
            @ApiParam("备注") @RequestParam(required = false) String remark) {
        invoiceService.updateInvoiceStatus(id, status, remark);
        return ApiResponse.success();
    }

    @ApiOperation("获取企业发票统计")
    @GetMapping("/stats/{enterpriseId}")
    public ApiResponse<Map<String, Object>> getEnterpriseInvoiceStats(
            @ApiParam("企业ID") @PathVariable String enterpriseId,
            @ApiParam("开始时间") @RequestParam(required = false) String startTime,
            @ApiParam("结束时间") @RequestParam(required = false) String endTime) {
        Map<String, Object> stats = invoiceService.getEnterpriseInvoiceStats(enterpriseId, startTime, endTime);
        return ApiResponse.success(stats);
    }

    @ApiOperation("批量开票")
    @PostMapping("/batch-issue")
    public ApiResponse<Void> batchIssueInvoices(
            @ApiParam("发票ID列表") @RequestBody List<String> invoiceIds) {
        invoiceService.batchIssueInvoices(invoiceIds);
        return ApiResponse.success();
    }

    @ApiOperation("下载发票文件")
    @GetMapping("/{id}/download")
    public ApiResponse<String> downloadInvoiceFile(
            @ApiParam("发票ID") @PathVariable String id) {
        String fileUrl = invoiceService.downloadInvoiceFile(id);
        return ApiResponse.success(fileUrl);
    }

    @ApiOperation("导出发票列表")
    @PostMapping("/export")
    public void exportInvoices(@RequestBody InvoiceQueryDTO queryDTO,
            jakarta.servlet.http.HttpServletResponse response) {
        invoiceService.exportInvoices(queryDTO, response);
    }

    @ApiOperation("查询可申请发票的账单列表")
    @GetMapping("/available-bills/{enterpriseId}")
    public ApiResponse<List<BillDTO>> queryAvailableBillsForInvoice(
            @ApiParam("企业ID") @PathVariable String enterpriseId) {
        List<BillDTO> bills = invoiceService.queryAvailableBillsForInvoice(enterpriseId);
        return ApiResponse.success(bills);
    }
}
