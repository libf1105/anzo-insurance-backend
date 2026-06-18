package com.anzo.insurance.modules.file.service;

import com.anzo.insurance.modules.file.dto.FileResponseDTO;
import com.anzo.insurance.modules.file.dto.FileUploadDTO;
import com.anzo.insurance.modules.file.entity.EnterpriseFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文件管理服务接口
 */
public interface FileManagementService {

    /**
     * 上传文件
     */
    FileResponseDTO uploadFile(FileUploadDTO uploadDTO);

    /**
     * 上传文件（简化方法）
     */
    FileResponseDTO uploadFile(MultipartFile file, String fileType, String description);

    /**
     * 根据ID获取文件
     */
    FileResponseDTO getFileById(String fileId);

    /**
     * 根据文件ID获取文件实体
     */
    EnterpriseFile getFileEntityById(String fileId);

    /**
     * 根据企业ID获取文件列表
     */
    List<FileResponseDTO> getFilesByEnterpriseId(String enterpriseId);

    /**
     * 根据企业ID和文件类型获取文件列表
     */
    List<FileResponseDTO> getFilesByEnterpriseIdAndType(String enterpriseId, String fileType);

    /**
     * 分页查询文件列表
     */
    Page<FileResponseDTO> getFilesPage(Pageable pageable, String enterpriseId, String fileType, String reviewStatus);

    /**
     * 删除文件（逻辑删除）
     */
    void deleteFile(String fileId);

    /**
     * 下载文件
     */
    InputStream downloadFile(String fileId);

    /**
     * 获取文件下载流
     */
    byte[] downloadFileBytes(String fileId);

    /**
     * 审核文件
     */
    void reviewFile(String fileId, String reviewResult, String remark);

    /**
     * 批量审核文件
     */
    void batchReviewFiles(List<String> fileIds, String reviewResult, String remark);

    /**
     * 获取企业文件统计
     */
    FileStatistics getFileStatistics(String enterpriseId);

    /**
     * 获取待审核文件列表
     */
    List<FileResponseDTO> getPendingReviewFiles();

    /**
     * 检查文件是否存在（根据MD5）
     */
    boolean checkFileExists(String md5, String enterpriseId);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String fileId);

    /**
     * 更新文件描述
     */
    void updateFileDescription(String fileId, String description);

    /**
     * 增加下载次数
     */
    void incrementDownloadCount(String fileId);

    /**
     * 文件统计信息接口
     */
    interface FileStatistics {
        Integer getBusinessLicenseCount();
        Integer getTaxRegistrationCount();
        Integer getLegalPersonIdCount();
        Integer getTotalFiles();
        Integer getPendingReviewCount();
        Integer getApprovedCount();
        Integer getRejectedCount();
    }
}