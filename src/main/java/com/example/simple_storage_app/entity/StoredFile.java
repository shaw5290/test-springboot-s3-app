package com.example.simple_storage_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "stored_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false, unique = true)
    private String s3Key;

    private String contentType;

    private long size;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onPersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
