package com.anzo.insurance.modules.insurance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 投保草稿DTO
 */
@Data
@Schema(description = "投保草稿")
public class InsuranceDraftDTO {
    @Schema(description = "草稿ID")
    private Long id;

    @Schema(description = "当前步骤")
    private Integer currentStep;

    @Schema(description = "步骤1数据")
    private Map<String, Object> step1Data;

    @Schema(description = "步骤2数据")
    private Map<String, Object> step2Data;

    @Schema(description = "步骤3数据")
    private Map<String, Object> step3Data;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "过期时间")
    private LocalDateTime expiredAt;
}
