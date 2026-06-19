package com.anzo.insurance.modules.finance.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.finance.dto.WalletDTO;
import com.anzo.insurance.modules.finance.dto.WalletQueryDTO;
import com.anzo.insurance.modules.finance.dto.WalletUpdateDTO;
import com.anzo.insurance.modules.finance.service.WalletService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.math.BigDecimal;

/**
 * 钱包管理控制器
 */
@Api(tags = "钱包管理")
@RestController
@RequestMapping("/api/finance/wallet")
public class WalletController {

    @Resource
    private WalletService walletService;

    @ApiOperation("获取钱包详情")
    @GetMapping("/{id}")
    public ApiResponse<WalletDTO> getWalletDetail(
            @ApiParam("钱包ID") @PathVariable Long id) {
        WalletDTO walletDetail = walletService.getWallet(id);
        return ApiResponse.success(walletDetail);
    }

    @ApiOperation("根据企业ID获取钱包")
    @GetMapping("/by-enterprise/{enterpriseId}")
    public ApiResponse<WalletDTO> getWalletByEnterpriseId(
            @ApiParam("企业ID") @PathVariable Long enterpriseId) {
        WalletDTO walletDetail = walletService.getWalletByEnterpriseId(enterpriseId);
        return ApiResponse.success(walletDetail);
    }

    @ApiOperation("分页查询钱包列表")
    @GetMapping("/list")
    public ApiResponse<IPage<WalletDTO>> queryWalletPage(@Valid WalletQueryDTO queryDTO) {
        IPage<WalletDTO> result = walletService.queryWalletPage(queryDTO);
        return ApiResponse.success(result);
    }

    @ApiOperation("初始化企业钱包")
    @PostMapping("/init")
    public ApiResponse<WalletDTO> initWallet(
            @ApiParam("企业ID") @RequestParam Long enterpriseId,
            @ApiParam("企业名称") @RequestParam(required = false) String enterpriseName) {
        WalletDTO wallet = walletService.initWallet(enterpriseId, enterpriseName);
        return ApiResponse.success(wallet);
    }

    @ApiOperation("更新钱包余额")
    @PostMapping("/update-balance")
    public ApiResponse<WalletDTO> updateWalletBalance(@Validated @RequestBody WalletUpdateDTO updateDTO) {
        WalletDTO wallet = walletService.updateWalletBalance(updateDTO);
        return ApiResponse.success(wallet);
    }

    @ApiOperation("冻结金额")
    @PostMapping("/freeze")
    public ApiResponse<WalletDTO> freezeAmount(
            @ApiParam("钱包ID") @RequestParam Long walletId,
            @ApiParam("冻结金额") @RequestParam BigDecimal amount,
            @ApiParam("业务ID") @RequestParam(required = false) Long businessId,
            @ApiParam("业务描述") @RequestParam(required = false) String businessDesc,
            @ApiParam("备注") @RequestParam(required = false) String remark) {
        WalletDTO wallet = walletService.freezeAmount(walletId, amount, businessId, businessDesc, remark);
        return ApiResponse.success(wallet);
    }

    @ApiOperation("解冻金额")
    @PostMapping("/unfreeze")
    public ApiResponse<WalletDTO> unfreezeAmount(
            @ApiParam("钱包ID") @RequestParam Long walletId,
            @ApiParam("解冻金额") @RequestParam BigDecimal amount,
            @ApiParam("业务ID") @RequestParam(required = false) Long businessId,
            @ApiParam("业务描述") @RequestParam(required = false) String businessDesc,
            @ApiParam("备注") @RequestParam(required = false) String remark) {
        WalletDTO wallet = walletService.unfreezeAmount(walletId, amount, businessId, businessDesc, remark);
        return ApiResponse.success(wallet);
    }

    @ApiOperation("账户充值")
    @PostMapping("/recharge")
    public ApiResponse<WalletDTO> recharge(
            @ApiParam("钱包ID") @RequestParam Long walletId,
            @ApiParam("充值金额") @RequestParam BigDecimal amount,
            @ApiParam("支付方式（1-在线支付，2-银行转账，3-余额支付）") @RequestParam Integer paymentMethod,
            @ApiParam("支付流水号") @RequestParam(required = false) String paymentNo,
            @ApiParam("备注") @RequestParam(required = false) String remark) {
        WalletDTO wallet = walletService.recharge(walletId, amount, paymentMethod, paymentNo, remark);
        return ApiResponse.success(wallet);
    }

    @ApiOperation("账户扣款")
    @PostMapping("/deduct")
    public ApiResponse<WalletDTO> deduct(
            @ApiParam("钱包ID") @RequestParam Long walletId,
            @ApiParam("扣款金额") @RequestParam BigDecimal amount,
            @ApiParam("业务ID") @RequestParam Long businessId,
            @ApiParam("业务描述") @RequestParam String businessDesc,
            @ApiParam("备注") @RequestParam(required = false) String remark) {
        WalletDTO wallet = walletService.deduct(walletId, amount, businessId, businessDesc, remark);
        return ApiResponse.success(wallet);
    }

    @ApiOperation("账户退款")
    @PostMapping("/refund")
    public ApiResponse<WalletDTO> refund(
            @ApiParam("钱包ID") @RequestParam Long walletId,
            @ApiParam("退款金额") @RequestParam BigDecimal amount,
            @ApiParam("业务ID") @RequestParam Long businessId,
            @ApiParam("业务描述") @RequestParam String businessDesc,
            @ApiParam("备注") @RequestParam(required = false) String remark) {
        WalletDTO wallet = walletService.refund(walletId, amount, businessId, businessDesc, remark);
        return ApiResponse.success(wallet);
    }
}