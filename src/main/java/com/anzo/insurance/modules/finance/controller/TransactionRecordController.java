package com.anzo.insurance.modules.finance.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.finance.dto.TransactionRecordDTO;
import com.anzo.insurance.modules.finance.dto.TransactionQueryDTO;
import com.anzo.insurance.modules.finance.service.TransactionRecordService;
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
 * 交易记录管理控制器
 */
@Api(tags = "交易记录管理")
@RestController
@RequestMapping("/api/finance/transaction")
public class TransactionRecordController {

    @Resource
    private TransactionRecordService transactionRecordService;

    @ApiOperation("获取交易记录详情")
    @GetMapping("/{id}")
    public ApiResponse<TransactionRecordDTO> getTransactionDetail(
            @ApiParam("交易记录ID") @PathVariable String id) {
        TransactionRecordDTO transactionDetail = transactionRecordService.getTransactionRecord(id);
        return ApiResponse.success(transactionDetail);
    }

    @ApiOperation("分页查询交易记录")
    @GetMapping("/list")
    public ApiResponse<IPage<TransactionRecordDTO>> queryTransactionPage(@Valid TransactionQueryDTO queryDTO) {
        IPage<TransactionRecordDTO> result = transactionRecordService.queryTransactionPage(queryDTO);
        return ApiResponse.success(result);
    }

    @ApiOperation("根据业务ID查询交易记录")
    @GetMapping("/by-business/{businessId}")
    public ApiResponse<TransactionRecordDTO> getTransactionByBusinessId(
            @ApiParam("业务ID") @PathVariable String businessId) {
        TransactionRecordDTO transaction = transactionRecordService.getTransactionByBusinessId(businessId);
        return ApiResponse.success(transaction);
    }

    @ApiOperation("获取企业交易统计")
    @GetMapping("/stats/{enterpriseId}")
    public ApiResponse<Map<String, Object>> getEnterpriseTransactionStats(
            @ApiParam("企业ID") @PathVariable String enterpriseId,
            @ApiParam("开始时间") @RequestParam(required = false) String startTime,
            @ApiParam("结束时间") @RequestParam(required = false) String endTime) {
        Map<String, Object> stats = transactionRecordService.getEnterpriseTransactionStats(enterpriseId, startTime, endTime);
        return ApiResponse.success(stats);
    }

    @ApiOperation("更新交易记录状态")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateTransactionStatus(
            @ApiParam("交易记录ID") @PathVariable String id,
            @ApiParam("交易状态（0-待处理，1-处理中，2-成功，3-失败，4-已取消）") @RequestParam Integer status,
            @ApiParam("备注") @RequestParam(required = false) String remark) {
        transactionRecordService.updateTransactionStatus(id, status, remark);
        return ApiResponse.success();
    }

    @ApiOperation("导出交易记录")
    @PostMapping("/export")
    public void exportTransactions(@RequestBody TransactionQueryDTO queryDTO,
            jakarta.servlet.http.HttpServletResponse response) {
        transactionRecordService.exportTransactions(queryDTO, response);
    }
}
