package com.anzo.insurance.modules.policy.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 保单修改DTO
 */
@Data
public class PolicyUpdateDTO {

    /**
     * 保单ID
     */
    @NotNull(message = "保单ID不能为空")
    private String id;

    /**
     * 启运日期
     */
    private LocalDate departureDate;

    /**
     * 预计到达日期
     */
    private LocalDate expectedArrivalDate;

    /**
     * 运输工具信息（船名航次/航班号/车次等）
     */
    private String transportVehicleInfo;

    /**
     * 提单号/运单号
     */
    private String documentNo;

    /**
     * 包装数量
     */
    private Integer packingQuantity;

    /**
     * 唛头
     */
    private String shippingMark;

    /**
     * 保险金额
     */
    private BigDecimal insuredAmount;

    /**
     * 发票金额
     */
    private BigDecimal invoiceAmount;

    /**
     * 特别约定
     */
    private String specialAgreements;

    /**
     * 备注
     */
    private String remark;

    /**
     * 修改原因
     */
    private String updateReason;
}