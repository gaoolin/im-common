package com.im.storage.repository;

import com.im.storage.entity.UploadSession;
import com.im.storage.entity.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

public interface UploadSessionRepository extends JpaRepository<UploadSession, Long> {
    Optional<UploadSession> findByUploadId(String uploadId);

    List<UploadSession> findByStatusAndExpiresAtBefore(UploadStatus status, Instant t);
}