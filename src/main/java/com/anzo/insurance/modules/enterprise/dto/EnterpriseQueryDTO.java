package com.anzo.insurance.modules.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 企业查询DTO
 */
@Data
@Schema(description = "企业查询参数")
public class EnterpriseQueryDTO {
    
    @Schema(description = "企业名称")
    private String name;
    
    @Schema(description = "信用代码")
    private String creditCode;
    
    @Schema(description = "联系人姓名")
    private String contactName;
    
    @Schema(description = "联系人手机")
    private String contactPhone;
    
    @Schema(description = "企业状态")
    private String status;
    
    @Schema(description = "注册开始时间")
    private String registerStartTime;
    
    @Schema(description = "注册结束时间")
    private String registerEndTime;
    
    @Schema(description = "页码")
    private Integer page = 1;
    
    @Schema(description = "每页条数")
    private Integer pageSize = 20;
}