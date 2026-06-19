package com.anzo.insurance.common.storage;

import com.anzo.insurance.common.config.MinioProperties;
import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * MinIO 存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String upload(MultipartFile file, String bucketName, String objectName) {
        try (InputStream inputStream = file.getInputStream()) {
            ensureBucketExists(bucketName);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return buildFileUrl(bucketName, objectName);
        } catch (ErrorResponseException e) {
            log.error("上传文件到 MinIO 失败: bucket={}, object={}, error={}", bucketName, objectName, e.getMessage(), e);
            String errorCode = e.errorResponse() == null ? null : e.errorResponse().code();
            if ("InvalidAccessKeyId".equals(errorCode)
                    || "SignatureDoesNotMatch".equals(errorCode)
                    || "AccessDenied".equals(errorCode)) {
                throw new BusinessException(ErrorCode.FILE_OPERATION_ERROR.getCode(), "MinIO 认证失败，请检查 access-key/secret-key 配置");
            }
            throw new BusinessException(ErrorCode.FILE_OPERATION_ERROR.getCode(), "上传文件到 MinIO 失败");
        } catch (Exception e) {
            log.error("上传文件到 MinIO 失败: bucket={}, object={}, error={}", bucketName, objectName, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_OPERATION_ERROR.getCode(), "上传文件到 MinIO 失败");
        }
    }

    public byte[] downloadBytes(String bucketName, String objectName) {
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("从 MinIO 下载文件失败: bucket={}, object={}, error={}", bucketName, objectName, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_OPERATION_ERROR.getCode(), "从 MinIO 下载文件失败");
        }
    }

    public InputStream getObject(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取 MinIO 文件流失败: bucket={}, object={}, error={}", bucketName, objectName, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_OPERATION_ERROR.getCode(), "获取 MinIO 文件流失败");
        }
    }

    public String buildFileUrl(String bucketName, String objectName) {
        String endpoint = minioProperties.getEndpoint();
        String normalized = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return normalized + "/" + bucketName + "/" + objectName;
    }

    private void ensureBucketExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        }
    }
}
