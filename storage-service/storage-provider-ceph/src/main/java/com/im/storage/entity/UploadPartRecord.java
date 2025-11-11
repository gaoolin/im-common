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
@Table(name = "storage_upload_part", uniqueConstraints = {
        @UniqueConstraint(name = "uk_session_part", columnNames = {"session_id", "part_number"})
})
@Data
public class UploadPartRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "part_number")
    private Integer partNumber;

    @Column(name = "etag", length = 128)
    private String eTag;

    private Long size;

    private Instant uploadedAt;
}