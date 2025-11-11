package com.im.storage.repository;

import com.im.storage.entity.UploadPartRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

public interface UploadPartRepository extends JpaRepository<UploadPartRecord, Long> {
    List<UploadPartRecord> findBySessionIdOrderByPartNumberAsc(Long sessionId);

    Optional<UploadPartRecord> findBySessionIdAndPartNumber(Long sessionId, Integer partNumber);

    void deleteBySessionId(Long sessionId);
}
