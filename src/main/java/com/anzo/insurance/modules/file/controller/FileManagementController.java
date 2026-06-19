package com.anzo.insurance.modules.file.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.file.dto.FileResponseDTO;
import com.anzo.insurance.modules.file.dto.FileUploadDTO;
import com.anzo.insurance.modules.file.service.FileManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 文件管理控制器
 */
@RestController
@RequestMapping("/enterprise/files")
@Tag(name = "文件管理", description = "企业文件上传、下载和管理接口")
@Slf4j
@RequiredArgsConstructor
@Validated
public class FileManagementController {

    private final FileManagementService fileManagementService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<FileResponseDTO> uploadFile(@Valid @ModelAttribute FileUploadDTO uploadDTO) {
        log.info("收到文件上传请求: fileType={}, size={}", 
                uploadDTO.getFileType(), uploadDTO.getFile().getSize());
        
        FileResponseDTO fileResponse = fileManagementService.uploadFile(uploadDTO);
        return ApiResponse.success(fileResponse);
    }

    @PostMapping("/upload-simple")
    @Operation(summary = "简化版文件上传")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<FileResponseDTO> uploadFileSimple(
            @RequestParam("file") MultipartFile file,
            @RequestParam @NotBlank(message = "文件类型不能为空") String fileType,
            @RequestParam(required = false) String description) {
        
        log.info("收到简化版文件上传请求: fileType={}, fileName={}, size={}", 
                fileType, file.getOriginalFilename(), file.getSize());
        
        FileResponseDTO fileResponse = fileManagementService.uploadFile(file, fileType, description);
        return ApiResponse.success(fileResponse);
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "获取文件信息")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<FileResponseDTO> getFile(@PathVariable Long fileId) {
        FileResponseDTO fileResponse = fileManagementService.getFileById(fileId);
        return ApiResponse.success(fileResponse);
    }

    @GetMapping({"", "/list"})
    @Operation(summary = "获取企业文件列表")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<List<FileResponseDTO>> getEnterpriseFiles(
            @RequestParam(required = false) String fileType) {
        
        // 从token中获取企业ID（服务层处理）
        List<FileResponseDTO> files;
        if (fileType != null) {
            files = fileManagementService.getFilesByEnterpriseIdAndType(null, fileType);
        } else {
            files = fileManagementService.getFilesByEnterpriseId(null);
        }
        
        return ApiResponse.success(files);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询文件列表")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<Page<FileResponseDTO>> getFilesPage(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String reviewStatus) {
        
        Page<FileResponseDTO> page = fileManagementService.getFilesPage(
                pageable, null, fileType, reviewStatus);
        return ApiResponse.success(page);
    }

    @GetMapping("/{fileId}/download")
    @Operation(summary = "下载文件")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        FileResponseDTO fileInfo = fileManagementService.getFileById(fileId);
        byte[] fileBytes = fileManagementService.downloadFileBytes(fileId);
        
        ByteArrayResource resource = new ByteArrayResource(fileBytes);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                String.format("attachment; filename=\"%s\"", fileInfo.getOriginalName()));
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileBytes.length));
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/{fileId}/url")
    @Operation(summary = "获取文件访问URL")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<String> getFileUrl(@PathVariable Long fileId) {
        String fileUrl = fileManagementService.getFileUrl(fileId);
        return ApiResponse.success(fileUrl);
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> deleteFile(@PathVariable Long fileId) {
        fileManagementService.deleteFile(fileId);
        return ApiResponse.success();
    }

    @PutMapping("/{fileId}/review")
    @Operation(summary = "审核文件")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> reviewFile(
            @PathVariable Long fileId,
            @RequestParam @NotBlank(message = "审核结果不能为空") String reviewResult,
            @RequestParam(required = false) String remark) {
        
        log.info("文件审核: fileId={}, result={}, remark={}", fileId, reviewResult, remark);
        fileManagementService.reviewFile(fileId, reviewResult, remark);
        return ApiResponse.success();
    }

    @PutMapping("/batch-review")
    @Operation(summary = "批量审核文件")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> batchReviewFiles(
            @RequestParam List<Long> fileIds,
            @RequestParam @NotBlank(message = "审核结果不能为空") String reviewResult,
            @RequestParam(required = false) String remark) {
        
        log.info("批量文件审核: fileCount={}, result={}", fileIds.size(), reviewResult);
        fileManagementService.batchReviewFiles(fileIds, reviewResult, remark);
        return ApiResponse.success();
    }

    @GetMapping("/pending-review")
    @Operation(summary = "获取待审核文件列表")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<List<FileResponseDTO>> getPendingReviewFiles() {
        List<FileResponseDTO> files = fileManagementService.getPendingReviewFiles();
        return ApiResponse.success(files);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取文件统计信息")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<FileManagementService.FileStatistics> getFileStatistics() {
        FileManagementService.FileStatistics stats = fileManagementService.getFileStatistics(null);
        return ApiResponse.success(stats);
    }

    @PutMapping("/{fileId}/description")
    @Operation(summary = "更新文件描述")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<Void> updateFileDescription(
            @PathVariable Long fileId,
            @RequestParam String description) {
        
        fileManagementService.updateFileDescription(fileId, description);
        return ApiResponse.success();
    }

    @GetMapping("/check-exists")
    @Operation(summary = "检查文件是否存在")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<Boolean> checkFileExists(
            @RequestParam String md5,
            @RequestParam(required = false) Long enterpriseId) {
        
        boolean exists = fileManagementService.checkFileExists(md5, enterpriseId);
        return ApiResponse.success(exists);
    }

    @GetMapping("/types")
    @Operation(summary = "获取支持的文件类型")
    public ApiResponse<List<FileTypeInfo>> getSupportedFileTypes() {
        List<FileTypeInfo> fileTypes = List.of(
                new FileTypeInfo("BUSINESS_LICENSE", "营业执照", 
                        List.of("image/jpeg", "image/png", "image/webp", "application/pdf"), 
                        10 * 1024 * 1024L),
                new FileTypeInfo("TAX_REGISTRATION", "税务登记证", 
                        List.of("image/jpeg", "image/png", "image/webp", "application/pdf"), 
                        10 * 1024 * 1024L),
                new FileTypeInfo("LEGAL_PERSON_ID", "法人身份证", 
                        List.of("image/jpeg", "image/png", "image/webp"), 
                        5 * 1024 * 1024L),
                new FileTypeInfo("OTHER", "其他文件", 
                        List.of("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp",
                                "application/pdf", 
                                "application/msword", 
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), 
                        10 * 1024 * 1024L)
        );
        
        return ApiResponse.success(fileTypes);
    }

    /**
     * 文件类型信息
     */
    public static class FileTypeInfo {
        private final String type;
        private final String description;
        private final List<String> allowedMimeTypes;
        private final Long maxSize;

        public FileTypeInfo(String type, String description, List<String> allowedMimeTypes, Long maxSize) {
            this.type = type;
            this.description = description;
            this.allowedMimeTypes = allowedMimeTypes;
            this.maxSize = maxSize;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getAllowedMimeTypes() {
            return allowedMimeTypes;
        }

        public Long getMaxSize() {
            return maxSize;
        }

        public String getFormattedMaxSize() {
            if (maxSize < 1024) {
                return maxSize + " B";
            } else if (maxSize < 1024 * 1024) {
                return String.format("%.2f KB", maxSize / 1024.0);
            } else {
                return String.format("%.2f MB", maxSize / (1024.0 * 1024.0));
            }
        }
    }
}
