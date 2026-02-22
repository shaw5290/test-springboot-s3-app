package com.example.simple_storage_app.repository;

import com.example.simple_storage_app.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
}
