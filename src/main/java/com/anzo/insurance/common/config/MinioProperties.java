package com.anzo.insurance.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 配置属性
 */
@Data
@ConfigurationProperties(prefix = "app.file.storage.minio")
public class MinioProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private Bucket bucket = new Bucket();

    @Data
    public static class Bucket {
        private String enterprise = "enterprise-files";
        private String insurance = "insurance-files";
        private String claim = "claim-files";
    }
}
