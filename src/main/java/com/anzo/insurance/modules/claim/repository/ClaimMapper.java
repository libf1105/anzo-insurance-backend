package com.anzo.insurance.modules.claim.repository;

import com.anzo.insurance.modules.claim.entity.Claim;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 理赔Mapper接口
 */
@Mapper
public interface ClaimMapper extends BaseMapper<Claim> {

    /**
     * 根据理赔编号查找理赔
     */
    @Select("SELECT * FROM t_claim WHERE claim_no = #{claimNo} AND deleted = false")
    Optional<Claim> findByClaimNo(@Param("claimNo") String claimNo);

    /**
     * 根据保单号查找理赔列表
     */
    @Select("SELECT * FROM t_claim WHERE policy_no = #{policyNo} AND deleted = false ORDER BY created_at DESC")
    List<Claim> findByPolicyNo(@Param("policyNo") String policyNo);

    /**
     * 根据企业ID查找理赔列表
     */
    @Select("SELECT * FROM t_claim WHERE enterprise_id = #{enterpriseId} AND deleted = false ORDER BY created_at DESC")
    List<Claim> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    /**
     * 根据状态查找理赔列表
     */
    @Select("SELECT * FROM t_claim WHERE enterprise_id = #{enterpriseId} AND status = #{status} AND deleted = false ORDER BY created_at DESC")
    List<Claim> findByStatus(@Param("enterpriseId") Long enterpriseId, @Param("status") String status);

    /**
     * 获取理赔统计数据
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN status IN ('REPORTED', 'MATERIAL_REVIEWING', 'MATERIAL_INCOMPLETE', 'SURVEYING', 'NEGOTIATING') THEN 1 END) as processing_count, " +
            "COUNT(CASE WHEN status = 'PAID' THEN 1 END) as paid_count, " +
            "COALESCE(SUM(CASE WHEN status = 'PAID' THEN payment_amount ELSE 0 END), 0) as total_payment_amount, " +
            "COALESCE(SUM(CASE WHEN status = 'PAID' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 0) as payment_rate " +
            "FROM t_claim WHERE enterprise_id = #{enterpriseId} AND deleted = false")
    ClaimStatistics getClaimStatistics(@Param("enterpriseId") Long enterpriseId);

    /**
     * 根据报案日期范围查找理赔
     */
    @Select("SELECT * FROM t_claim WHERE enterprise_id = #{enterpriseId} AND report_date BETWEEN #{startDate} AND #{endDate} AND deleted = false ORDER BY report_date DESC")
    List<Claim> findByReportDateRange(@Param("enterpriseId") Long enterpriseId, 
                                      @Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);

    /**
     * 获取理赔编号最大值
     */
    @Select("SELECT MAX(claim_no) FROM t_claim WHERE claim_no LIKE 'CLM${date}%'")
    String getMaxClaimNoByDate(@Param("date") String date);

    /**
     * 理赔统计结果类
     */
    class ClaimStatistics {
        private Integer processingCount;
        private Integer paidCount;
        private BigDecimal totalPaymentAmount;
        private BigDecimal paymentRate;

        public Integer getProcessingCount() {
            return processingCount;
        }

        public void setProcessingCount(Integer processingCount) {
            this.processingCount = processingCount;
        }

        public Integer getPaidCount() {
            return paidCount;
        }

        public void setPaidCount(Integer paidCount) {
            this.paidCount = paidCount;
        }

        public BigDecimal getTotalPaymentAmount() {
            return totalPaymentAmount;
        }

        public void setTotalPaymentAmount(BigDecimal totalPaymentAmount) {
            this.totalPaymentAmount = totalPaymentAmount;
        }

        public BigDecimal getPaymentRate() {
            return paymentRate;
        }

        public void setPaymentRate(BigDecimal paymentRate) {
            this.paymentRate = paymentRate;
        }
    }
}