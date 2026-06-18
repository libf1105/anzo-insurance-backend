package com.anzo.insurance.modules.finance.repository;

import com.anzo.insurance.modules.finance.dto.InvoiceQueryDTO;
import com.anzo.insurance.modules.finance.entity.Invoice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 发票Mapper接口
 */
@Mapper
public interface InvoiceMapper extends BaseMapper<Invoice> {

    /**
     * 根据发票号码查询
     */
    Invoice selectByInvoiceNo(@Param("invoiceNo") String invoiceNo);

    /**
     * 根据申请单ID查询
     */
    Invoice selectByApplicationId(@Param("applicationId") String applicationId);

    /**
     * 分页查询发票列表
     */
    List<Invoice> selectInvoicePage(@Param("query") InvoiceQueryDTO query);

    /**
     * 获取企业发票统计
     */
    Map<String, Object> selectEnterpriseInvoiceStats(
        @Param("enterpriseId") String enterpriseId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime);

    /**
     * 按发票状态统计
     */
    List<Map<String, Object>> selectInvoiceStatusStats(
        @Param("enterpriseId") String enterpriseId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime);
}