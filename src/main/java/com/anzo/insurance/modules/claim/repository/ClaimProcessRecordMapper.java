package com.anzo.insurance.modules.claim.repository;

import com.anzo.insurance.modules.claim.entity.ClaimProcessRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 理赔处理记录Mapper接口
 */
@Mapper
public interface ClaimProcessRecordMapper extends BaseMapper<ClaimProcessRecord> {

    /**
     * 根据理赔ID查找处理记录列表
     */
    @Select("SELECT * FROM t_claim_process_record WHERE claim_id = #{claimId} AND deleted = false ORDER BY operation_time DESC")
    List<ClaimProcessRecord> findByClaimId(@Param("claimId") Long claimId);

    /**
     * 根据理赔ID和处理类型查找处理记录
     */
    @Select("SELECT * FROM t_claim_process_record WHERE claim_id = #{claimId} AND process_type = #{processType} AND deleted = false ORDER BY operation_time DESC")
    List<ClaimProcessRecord> findByClaimIdAndProcessType(@Param("claimId") Long claimId, @Param("processType") String processType);

    /**
     * 获取理赔的最新状态变更记录
     */
    @Select("SELECT * FROM t_claim_process_record WHERE claim_id = #{claimId} AND process_type = 'STATUS_CHANGE' AND deleted = false ORDER BY operation_time DESC LIMIT 1")
    ClaimProcessRecord getLatestStatusChange(@Param("claimId") Long claimId);

    /**
     * 获取理赔的操作记录统计
     */
    @Select("SELECT process_type, COUNT(*) as count FROM t_claim_process_record WHERE claim_id = #{claimId} AND deleted = false GROUP BY process_type")
    List<ProcessTypeCount> getProcessTypeCounts(@Param("claimId") Long claimId);

    /**
     * 处理类型统计结果类
     */
    class ProcessTypeCount {
        private String processType;
        private Integer count;

        public String getProcessType() {
            return processType;
        }

        public void setProcessType(String processType) {
            this.processType = processType;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }
}