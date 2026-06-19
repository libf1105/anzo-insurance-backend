package com.anzo.insurance.modules.file.repository;

import com.anzo.insurance.modules.file.entity.EnterpriseFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 企业文件数据访问接口
 */
@Mapper
public interface EnterpriseFileRepository extends BaseMapper<EnterpriseFile> {

    /**
     * 根据企业ID和文件类型查询文件
     */
    @Select("SELECT * FROM enterprise_files WHERE enterprise_id = #{enterpriseId} " +
            "AND file_type = #{fileType} AND status = 'ACTIVE' AND deleted = false " +
            "ORDER BY created_at DESC")
    List<EnterpriseFile> findByEnterpriseIdAndType(@Param("enterpriseId") Long enterpriseId,
                                                    @Param("fileType") String fileType);

    /**
     * 根据企业ID查询所有有效文件
     */
    @Select("SELECT * FROM enterprise_files WHERE enterprise_id = #{enterpriseId} " +
            "AND status = 'ACTIVE' AND deleted = false ORDER BY created_at DESC")
    List<EnterpriseFile> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    /**
     * 根据MD5值查询文件
     */
    @Select("SELECT * FROM enterprise_files WHERE md5 = #{md5} " +
            "AND enterprise_id = #{enterpriseId} AND status = 'ACTIVE' AND deleted = false")
    EnterpriseFile findByMd5(@Param("md5") String md5, @Param("enterpriseId") Long enterpriseId);

    /**
     * 查询待审核的企业文件
     */
    @Select("SELECT f.*, e.name as enterprise_name FROM enterprise_files f " +
            "JOIN enterprise e ON f.enterprise_id = e.id " +
            "WHERE f.review_status = 'PENDING' AND f.status = 'ACTIVE' AND f.deleted = false " +
            "ORDER BY f.created_at ASC")
    List<EnterpriseFile> findPendingReviewFiles();

    /**
     * 查询企业已审核通过的文件
     */
    @Select("SELECT * FROM enterprise_files WHERE enterprise_id = #{enterpriseId} " +
            "AND review_status = 'APPROVED' AND status = 'ACTIVE' AND deleted = false " +
            "ORDER BY created_at DESC")
    List<EnterpriseFile> findApprovedFilesByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    /**
     * 获取企业文件数量统计
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN file_type = 'BUSINESS_LICENSE' THEN 1 END) as business_license_count, " +
            "COUNT(CASE WHEN file_type = 'TAX_REGISTRATION' THEN 1 END) as tax_registration_count, " +
            "COUNT(CASE WHEN file_type = 'LEGAL_PERSON_ID' THEN 1 END) as legal_person_id_count, " +
            "COUNT(*) as total_files " +
            "FROM enterprise_files " +
            "WHERE enterprise_id = #{enterpriseId} AND status = 'ACTIVE' AND deleted = false")
    FileStatistics getFileStatistics(@Param("enterpriseId") Long enterpriseId);

    /**
     * 文件统计信息
     */
    interface FileStatistics {
        Integer getBusinessLicenseCount();
        Integer getTaxRegistrationCount();
        Integer getLegalPersonIdCount();
        Integer getTotalFiles();
    }
}