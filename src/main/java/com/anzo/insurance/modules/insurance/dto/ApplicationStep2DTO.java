package com.anzo.insurance.modules.insurance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * 投保步骤2: 运输信息DTO
 */
@Data
@Schema(description = "投保步骤2 - 运输信息")
public class ApplicationStep2DTO {
    
    @NotBlank(message = "启运国家不能为空")
    @Schema(description = "启运国家", required = true, example = "中国")
    private String departureCountry;
    
    @NotBlank(message = "启运城市不能为空")
    @Schema(description = "启运城市", required = true, example = "上海")
    private String departureCity;
    
    @NotBlank(message = "目的国家不能为空")
    @Schema(description = "目的国家", required = true, example = "美国")
    private String arrivalCountry;
    
    @NotBlank(message = "目的城市不能为空")
    @Schema(description = "目的城市", required = true, example = "洛杉矶")
    private String arrivalCity;
    
    @NotNull(message = "启运日期不能为空")
    @Schema(description = "启运日期", required = true, example = "2024-01-20")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate departureDate;
    
    @Schema(description = "到达日期", example = "2024-02-10")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate arrivalDate;
    
    // 海运专属字段
    @Schema(description = "船名", example = "COSCO SHIPPING STAR")
    private String vesselName;
    
    @Schema(description = "航次号", example = "V.2401E")
    private String voyageNo;
    
    @Pattern(regexp = "^[A-Z0-9]{1,20}$", message = "提单号格式不正确")
    @Schema(description = "提单号", example = "COSU1234567890")
    private String billOfLadingNo;
    
    @Schema(description = "中转港口列表")
    private List<String> transitPorts;
    
    // 空运专属字段
    @Schema(description = "航班号", example = "CA983")
    private String flightNo;
    
    @Schema(description = "空运运单号", example = "784-12345675")
    private String airWaybillNo;
    
    @Schema(description = "中转机场列表")
    private List<String> transitAirports;
    
    // 铁路专属字段
    @Schema(description = "车次/班列号", example = "CHINA-RAIL-001")
    private String trainNo;
    
    @Schema(description = "铁路运单号", example = "CR123456789")
    private String railWaybillNo;
    
    // 陆运专属字段
    @Schema(description = "车牌号", example = "沪A12345")
    private String vehiclePlateNo;
    
    @Schema(description = "司机姓名", example = "张三")
    private String driverName;
    
    @Schema(description = "司机电话", example = "13800138000")
    private String driverPhone;
    
    // 多式联运专属字段
    @Schema(description = "多式联运运输段列表")
    private List<TransportSegmentDTO> segments;
    
    @Schema(description = "投保申请ID，续写时传入")
    private String applicationId;
    
    /**
     * 多式联运运输段DTO
     */
    @Data
    @Schema(description = "多式联运运输段")
    public static class TransportSegmentDTO {
        
        @NotBlank(message = "运输方式不能为空")
        @Schema(description = "运输方式: SEA, AIR, RAIL, ROAD", required = true)
        private String transportType;
        
        @NotBlank(message = "启运地不能为空")
        @Schema(description = "启运地", required = true)
        private String departurePlace;
        
        @NotBlank(message = "目的地不能为空")
        @Schema(description = "目的地", required = true)
        private String arrivalPlace;
        
        @NotNull(message = "启运日期不能为空")
        @Schema(description = "启运日期", required = true)
        @JsonFormat(pattern = "yyyy-MM-dd")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate departureDate;
        
        // 海运字段
        @Schema(description = "船名")
        private String vesselName;
        
        @Schema(description = "航次号")
        private String voyageNo;
        
        @Schema(description = "提单号")
        private String billNo;
        
        // 空运字段
        @Schema(description = "航班号")
        private String flightNo;
        
        @Schema(description = "运单号")
        private String waybillNo;
        
        // 铁路字段
        @Schema(description = "车次号")
        private String trainNo;
        
        // 陆运字段
        @Schema(description = "车牌号")
        private String vehiclePlate;
    }
}