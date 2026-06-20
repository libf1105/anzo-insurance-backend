package com.anzo.insurance.modules.file.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.anzo.insurance.common.config.MinioProperties;
import com.anzo.insurance.common.storage.MinioStorageService;
import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.modules.auth.entity.Enterprise;
import com.anzo.insurance.modules.auth.entity.User;
import com.anzo.insurance.modules.auth.repository.EnterpriseRepository;
import com.anzo.insurance.modules.auth.repository.UserRepository;
import com.anzo.insurance.modules.file.dto.FileResponseDTO;
import com.anzo.insurance.modules.file.dto.FileUploadDTO;
import com.anzo.insurance.modules.file.entity.EnterpriseFile;
import com.anzo.insurance.modules.file.repository.EnterpriseFileRepository;
import com.anzo.insurance.modules.file.service.FileManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件管理服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileManagementServiceImpl implements FileManagementService {

    private final EnterpriseFileRepository fileRepository;
    private final UserRepository userRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final MinioStorageService minioStorageService;
    private final MinioProperties minioProperties;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp",
            "application/pdf", 
            "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileResponseDTO uploadFile(FileUploadDTO uploadDTO) {
        // 验证文件
        validateFile(uploadDTO.getFile());
        
        // 获取当前用户和企业信息
        String username = ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        Enterprise enterprise = enterpriseRepository.selectById(user.getEnterpriseId());
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }
        
        // 检查文件是否已存在（通过MD5）
        String md5;
        try {
            md5 = DigestUtil.md5Hex(uploadDTO.getFile().getBytes());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_OPERATION_ERROR.getCode(), "文件读取失败", e);
        }
        EnterpriseFile existingFile = fileRepository.findByMd5(md5, enterprise.getId());
        if (existingFile != null && existingFile.isActive()) {
            throw new BusinessException(ErrorCode.FILE_EXISTS.getCode(), "文件已存在");
        }
        
        // 生成文件名和路径
        String originalFilename = uploadDTO.getFile().getOriginalFilename();
        String fileExtension = FileUtil.extName(originalFilename);
        String fileName = IdUtil.fastSimpleUUID() + "." + fileExtension;
        String objectKey = buildObjectKey(enterprise.getId(), fileName);
        
        // 上传文件到 MinIO
        String fileUrl = minioStorageService.upload(
                uploadDTO.getFile(),
                minioProperties.getBucket().getEnterprise(),
                objectKey
        );
        
        // 保存文件信息到数据库
        EnterpriseFile file = new EnterpriseFile();
        file.setFileName(fileName);
        file.setOriginalName(originalFilename);
        file.setFileType(uploadDTO.getFileType());
        file.setFileSize(uploadDTO.getFile().getSize());
        file.setMimeType(uploadDTO.getFile().getContentType());
        file.setFilePath(objectKey);
        file.setFileUrl(fileUrl);
        file.setMd5(md5);
        file.setEnterpriseId(enterprise.getId());
        file.setUploadUserId(user.getId());
        file.setStatus(EnterpriseFile.Status.ACTIVE.name());
        file.setReviewStatus(EnterpriseFile.ReviewStatus.PENDING.name());
        file.setDescription(uploadDTO.getDescription());
        file.setDownloadCount(0);
        file.setMetadata(uploadDTO.getMetadata());
        
        fileRepository.insert(file);
        
        log.info("文件上传成功: fileId={}, fileName={}, enterpriseId={}", 
                file.getId(), originalFilename, enterprise.getId());
        
        return convertToResponseDTO(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileResponseDTO uploadFile(MultipartFile file, String fileType, String description) {
        FileUploadDTO uploadDTO = new FileUploadDTO();
        uploadDTO.setFile(file);
        uploadDTO.setFileType(fileType);
        uploadDTO.setDescription(description);
        return uploadFile(uploadDTO);
    }

    @Override
    public FileResponseDTO getFileById(Long fileId) {
        EnterpriseFile file = getFileEntityById(fileId);
        return convertToResponseDTO(file);
    }

    @Override
    public EnterpriseFile getFileEntityById(Long fileId) {
        EnterpriseFile file = fileRepository.selectById(fileId);
        if (file == null || file.getDeleted()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND.getCode(), "文件不存在");
        }
        
        // 检查权限
        checkFileAccessPermission(file);
        
        return file;
    }

    @Override
    public List<FileResponseDTO> getFilesByEnterpriseId(Long enterpriseId) {
        checkEnterpriseAccessPermission(enterpriseId);
        
        List<EnterpriseFile> files = fileRepository.findByEnterpriseId(enterpriseId);
        return files.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FileResponseDTO> getFilesByEnterpriseIdAndType(Long enterpriseId, String fileType) {
        checkEnterpriseAccessPermission(enterpriseId);
        
        List<EnterpriseFile> files = fileRepository.findByEnterpriseIdAndType(enterpriseId, fileType);
        return files.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<FileResponseDTO> getFilesPage(Pageable pageable, Long enterpriseId, String fileType, String reviewStatus) {
        checkEnterpriseAccessPermission(enterpriseId);
        
        // 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EnterpriseFile> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EnterpriseFile>()
                .eq(EnterpriseFile::getEnterpriseId, enterpriseId)
                .eq(StrUtil.isNotBlank(fileType), EnterpriseFile::getFileType, fileType)
                .eq(StrUtil.isNotBlank(reviewStatus), EnterpriseFile::getReviewStatus, reviewStatus)
                .eq(EnterpriseFile::getStatus, EnterpriseFile.Status.ACTIVE.name())
                .eq(EnterpriseFile::getDeleted, false)
                .orderByDesc(EnterpriseFile::getCreatedAt);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EnterpriseFile> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                        pageable.getPageNumber() + 1, pageable.getPageSize());
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EnterpriseFile> result = 
                fileRepository.selectPage(page, queryWrapper);
        
        List<FileResponseDTO> content = result.getRecords().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId) {
        EnterpriseFile file = getFileEntityById(fileId);
        
        // 检查删除权限
        checkFileDeletePermission(file);
        
        file.setDeleted(true);
        file.setUpdatedAt(LocalDateTime.now());
        fileRepository.updateById(file);
        
        log.info("文件已删除（逻辑删除）: fileId={}, fileName={}", fileId, file.getFileName());
    }

    @Override
    public InputStream downloadFile(Long fileId) {
        EnterpriseFile file = getFileEntityById(fileId);
        incrementDownloadCount(fileId);
        return minioStorageService.getObject(minioProperties.getBucket().getEnterprise(), file.getFilePath());
    }

    @Override
    public byte[] downloadFileBytes(Long fileId) {
        EnterpriseFile file = getFileEntityById(fileId);
        incrementDownloadCount(fileId);
        return minioStorageService.downloadBytes(minioProperties.getBucket().getEnterprise(), file.getFilePath());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewFile(Long fileId, String reviewResult, String remark) {
        EnterpriseFile file = getFileEntityById(fileId);
        
        // 检查审核权限
        checkReviewPermission();
        
        if (!EnterpriseFile.ReviewStatus.PENDING.name().equals(file.getReviewStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "文件已审核，无法重复审核");
        }
        
        String username = ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        file.setReviewBy(String.valueOf(user.getId()));
        file.setReviewAt(LocalDateTime.now().toString());
        file.setReviewRemark(remark);
        
        if ("APPROVED".equals(reviewResult)) {
            file.setReviewStatus(EnterpriseFile.ReviewStatus.APPROVED.name());
            log.info("文件审核通过: fileId={}, fileName={}, reviewer={}", 
                    fileId, file.getFileName(), username);
        } else if ("REJECTED".equals(reviewResult)) {
            file.setReviewStatus(EnterpriseFile.ReviewStatus.REJECTED.name());
            // 拒绝时可以更新文件状态为无效
            file.setStatus(EnterpriseFile.Status.INACTIVE.name());
            log.info("文件审核拒绝: fileId={}, fileName={}, reviewer={}, reason={}", 
                    fileId, file.getFileName(), username, remark);
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "无效的审核结果");
        }
        
        file.setUpdatedAt(LocalDateTime.now());
        fileRepository.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchReviewFiles(List<Long> fileIds, String reviewResult, String remark) {
        for (Long fileId : fileIds) {
            try {
                reviewFile(fileId, reviewResult, remark);
            } catch (Exception e) {
                log.error("批量审核文件失败: fileId={}, error={}", fileId, e.getMessage());
                // 继续处理其他文件
            }
        }
    }

    @Override
    public FileStatistics getFileStatistics(Long enterpriseId) {
        checkEnterpriseAccessPermission(enterpriseId);
        
        // 查询文件统计
        EnterpriseFileRepository.FileStatistics stats = fileRepository.getFileStatistics(enterpriseId);
        List<EnterpriseFile> pendingFiles = fileRepository.findPendingReviewFiles();
        List<EnterpriseFile> approvedFiles = fileRepository.findApprovedFilesByEnterpriseId(enterpriseId);
        
        return new FileStatistics() {
            @Override
            public Integer getBusinessLicenseCount() {
                return stats != null ? stats.getBusinessLicenseCount() : 0;
            }
            
            @Override
            public Integer getTaxRegistrationCount() {
                return stats != null ? stats.getTaxRegistrationCount() : 0;
            }
            
            @Override
            public Integer getLegalPersonIdCount() {
                return stats != null ? stats.getLegalPersonIdCount() : 0;
            }
            
            @Override
            public Integer getTotalFiles() {
                return stats != null ? stats.getTotalFiles() : 0;
            }
            
            @Override
            public Integer getPendingReviewCount() {
                return (int) pendingFiles.stream()
                        .filter(file -> enterpriseId.equals(file.getEnterpriseId()))
                        .count();
            }
            
            @Override
            public Integer getApprovedCount() {
                return approvedFiles.size();
            }
            
            @Override
            public Integer getRejectedCount() {
                // 计算拒绝的文件数量
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EnterpriseFile> queryWrapper = 
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EnterpriseFile>()
                        .eq(EnterpriseFile::getEnterpriseId, enterpriseId)
                        .eq(EnterpriseFile::getReviewStatus, EnterpriseFile.ReviewStatus.REJECTED.name())
                        .eq(EnterpriseFile::getDeleted, false);
                return fileRepository.selectCount(queryWrapper).intValue();
            }
        };
    }

    @Override
    public List<FileResponseDTO> getPendingReviewFiles() {
        // 只有管理员可以查看待审核文件
        checkReviewPermission();
        
        List<EnterpriseFile> files = fileRepository.findPendingReviewFiles();
        return files.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkFileExists(String md5, Long enterpriseId) {
        EnterpriseFile file = fileRepository.findByMd5(md5, enterpriseId);
        return file != null && file.isActive();
    }

    @Override
    public String getFileUrl(Long fileId) {
        EnterpriseFile file = getFileEntityById(fileId);
        return file.getFileUrl();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFileDescription(Long fileId, String description) {
        EnterpriseFile file = getFileEntityById(fileId);
        
        // 检查权限
        checkFileUpdatePermission(file);
        
        file.setDescription(description);
        file.setUpdatedAt(LocalDateTime.now());
        fileRepository.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementDownloadCount(Long fileId) {
        EnterpriseFile file = getFileEntityById(fileId);
        file.setDownloadCount(file.getDownloadCount() != null ? file.getDownloadCount() + 1 : 1);
        file.setUpdatedAt(LocalDateTime.now());
        fileRepository.updateById(file);
    }

    // ============ 私有方法 ============

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "文件不能为空");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE.getCode(), "文件大小不能超过10MB");
        }
        
        String mimeType = file.getContentType();
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED.getCode(), "不支持的文件类型");
        }
    }

    private String buildObjectKey(Long enterpriseId, String fileName) {
        return "enterprise/" + enterpriseId + "/" + fileName;
    }

    private FileResponseDTO convertToResponseDTO(EnterpriseFile file) {
        FileResponseDTO dto = new FileResponseDTO();
        BeanUtils.copyProperties(file, dto);
        
        // 设置描述性字段
        dto.setFileTypeDescription(file.getFileTypeDescription());
        dto.setStatusDescription(file.getStatusDescription());
        dto.setReviewStatusDescription(file.getReviewStatusDescription());
        dto.setFormattedFileSize(file.getFormattedFileSize());
        
        return dto;
    }

    private void checkFileAccessPermission(EnterpriseFile file) {
        String username = ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 管理员可以访问所有文件
        if (user.getRole().startsWith("SUPER_ADMIN") || user.getRole().startsWith("ADMIN")) {
            return;
        }
        
        // 普通用户只能访问自己企业的文件
        if (!file.getEnterpriseId().equals(user.getEnterpriseId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权访问该文件");
        }
    }

    private void checkEnterpriseAccessPermission(Long enterpriseId) {
        String username = ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 管理员可以访问所有企业
        if (user.getRole().startsWith("SUPER_ADMIN") || user.getRole().startsWith("ADMIN")) {
            return;
        }
        
        // 普通用户只能访问自己企业
        if (!enterpriseId.equals(user.getEnterpriseId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权访问该企业");
        }
    }

    private void checkFileDeletePermission(EnterpriseFile file) {
        String username = ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 管理员可以删除所有文件
        if (user.getRole().startsWith("SUPER_ADMIN") || user.getRole().startsWith("ADMIN")) {
            return;
        }
        
        // 普通用户只能删除自己上传的文件
        if (!file.getUploadUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权删除该文件");
        }
    }

    private void checkFileUpdatePermission(EnterpriseFile file) {
        String username = ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 管理员可以更新所有文件
        if (user.getRole().startsWith("SUPER_ADMIN") || user.getRole().startsWith("ADMIN")) {
            return;
        }
        
        // 普通用户只能更新自己上传的文件
        if (!file.getUploadUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权更新该文件");
        }
    }

    private void checkReviewPermission() {
        String username = ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 只有管理员可以审核文件
        if (!user.getRole().startsWith("SUPER_ADMIN") && !user.getRole().startsWith("ADMIN")) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权审核文件");
        }
    }
}
