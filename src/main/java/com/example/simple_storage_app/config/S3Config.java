package com.example.simple_storage_app.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Slf4j
@Configuration
public class S3Config {

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.endpoint}")
    private String endpoint;

    @Value("${app.s3.region}")
    private String region;

    @Value("${app.s3.access-key}")
    private String accessKey;

    @Value("${app.s3.secret-key}")
    private String secretKey;

    @Value("${app.s3.path-style-access:false}")
    private boolean pathStyleAccess;

    @PostConstruct
    public void logConfig() {
        log.info("S3 Configuration on Startup:");
        log.info("  Bucket: {}", bucket);
        log.info("  Region: {}", region);
        log.info("  Endpoint: {}", (endpoint == null || endpoint.isBlank()) ? "AWS Default" : endpoint);
        log.info("  Access Key: {}", (accessKey == null || accessKey.isBlank()) ? "Not Provided" : accessKey);
        log.info("  Secret Key: {}", (secretKey == null || secretKey.isBlank()) ? "Not Provided" : "********");
        log.info("  Path style access: {}", pathStyleAccess);
    }

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .forcePathStyle(pathStyleAccess);

        // Only override endpoint if specifically provided (e.g. for MinIO or custom
        // proxy)
        // AWS SDK v2 handles standard S3 endpoints better without an override.
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        }

        return builder.build();
    }
}
