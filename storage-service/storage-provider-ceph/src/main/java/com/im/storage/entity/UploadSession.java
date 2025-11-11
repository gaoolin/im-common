package com.im.storage.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */


@Entity
@Table(name = "storage_upload_session", indexes = {
        @Index(name = "idx_uploadid", columnList = "upload_id")
})
@Data
public class UploadSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantId;
    private String bucket;
    @Column(name = "object_key", length = 1024)
    private String objectKey;

    @Column(name = "upload_id", length = 256)
    private String uploadId;

    private String contentType;
    private Long totalSize;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    private UploadStatus status;
}