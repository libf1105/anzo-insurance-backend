package com.anzo.insurance.modules.claim.repository;

import com.anzo.insurance.modules.claim.entity.ClaimMaterial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 理赔材料Mapper接口
 */
@Mapper
public interface ClaimMaterialMapper extends BaseMapper<ClaimMaterial> {

    /**
     * 根据理赔ID查找材料列表
     */
    @Select("SELECT * FROM t_claim_material WHERE claim_id = #{claimId} AND deleted = false ORDER BY upload_time DESC")
    List<ClaimMaterial> findByClaimId(@Param("claimId") String claimId);

    /**
     * 根据理赔ID和材料类型查找材料
     */
    @Select("SELECT * FROM t_claim_material WHERE claim_id = #{claimId} AND material_type = #{materialType} AND deleted = false ORDER BY upload_time DESC")
    List<ClaimMaterial> findByClaimIdAndType(@Param("claimId") String claimId, @Param("materialType") String materialType);

    /**
     * 统计理赔材料状态
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN is_approved = true THEN 1 END) as approved_count, " +
            "COUNT(CASE WHEN is_approved = false AND is_required = true THEN 1 END) as pending_required_count, " +
            "COUNT(*) as total_count " +
            "FROM t_claim_material WHERE claim_id = #{claimId} AND deleted = false")
    MaterialStatistics getMaterialStatistics(@Param("claimId") String claimId);

    /**
     * 检查必需材料是否完整
     */
    @Select("SELECT COUNT(*) = 0 FROM t_claim_material WHERE claim_id = #{claimId} AND is_required = true AND is_approved = false AND deleted = false")
    boolean areRequiredMaterialsComplete(@Param("claimId") String claimId);

    /**
     * 材料统计结果类
     */
    class MaterialStatistics {
        private Integer approvedCount;
        private Integer pendingRequiredCount;
        private Integer totalCount;

        public Integer getApprovedCount() {
            return approvedCount;
        }

        public void setApprovedCount(Integer approvedCount) {
            this.approvedCount = approvedCount;
        }

        public Integer getPendingRequiredCount() {
            return pendingRequiredCount;
        }

        public void setPendingRequiredCount(Integer pendingRequiredCount) {
            this.pendingRequiredCount = pendingRequiredCount;
        }

        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }
    }
}