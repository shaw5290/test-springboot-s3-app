package com.example.simple_storage_app.service;

import com.example.simple_storage_app.entity.StoredFile;
import com.example.simple_storage_app.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final S3Client s3Client;
    private final StoredFileRepository repository;

    @Value("${app.s3.bucket}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public List<StoredFile> listAll() {
        return repository.findAll();
    }

    @Transactional
    public StoredFile upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must be less than 5 MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unnamed";
        }
        String s3Key = UUID.randomUUID().toString() + "_" + sanitizeFilename(originalFilename);

        ensureBucketExists();

        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        StoredFile stored = StoredFile.builder()
                .originalFilename(originalFilename)
                .s3Key(s3Key)
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
        return repository.save(stored);
    }

    @Transactional
    public void delete(Long id) {
        StoredFile stored = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + id));

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(stored.getS3Key())
                    .build());
        } catch (S3Exception e) {
            log.warn("S3 delete failed for key {}: {}", stored.getS3Key(), e.getMessage());
        }
        repository.delete(stored);
    }

    private void ensureBucketExists() {
        try {
            log.debug("Checking if bucket exists: {}", bucketName);
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            log.info("Bucket {} does not exist, creating it...", bucketName);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            log.info("Successfully created bucket: {}", bucketName);
        } catch (S3Exception e) {
            log.error("Error checking/creating bucket '{}'. Status Code: {}, Error Message: {}, Request ID: {}",
                    bucketName, e.statusCode(), e.getMessage(), e.requestId());
            throw e;
        }
    }

    private static String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
