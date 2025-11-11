package com.im.storage;

import com.im.storage.entity.UploadPartRecord;
import com.im.storage.entity.UploadSession;
import com.im.storage.entity.UploadStatus;
import com.im.storage.repository.UploadPartRepository;
import com.im.storage.repository.UploadSessionRepository;
import com.im.storage.v1.StorageServiceV1;
import com.im.storage.v1.model.FileInfo;
import com.im.storage.v1.model.StorageException;
import com.im.storage.v1.model.UploadRequest;
import com.im.storage.v1.model.UploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ceph S3 implementation of StorageServiceV1 with multipart and resume support.
 * downloadStream 返回 InputStream，调用方必须关闭流。
 * uploadPart 的幂等通过 DB 唯一约束（sessionId + partNumber）保证；你应在数据库层加唯一索引（上面 DDL 已设置）。
 * 对 S3 的网络异常建议加重试/退避策略（这里示例采用简单尝试次数或在调用方做重试）。
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */
@Slf4j
@Service
public class CephS3StorageService implements StorageServiceV1 {

    private static final long MIN_PART_SIZE = 5L * 1024 * 1024;
    private final S3Client s3;
    private final S3Presigner presigner;
    private final UploadSessionRepository sessionRepo;
    private final UploadPartRepository partRepo;
    private final int maxRetries = 3;

    public CephS3StorageService(S3Client s3,
                                S3Presigner presigner,
                                UploadSessionRepository sessionRepo,
                                UploadPartRepository partRepo) {
        this.s3 = s3;
        this.presigner = presigner;
        this.sessionRepo = sessionRepo;
        this.partRepo = partRepo;
    }

    // =========== Simple upload (from bytes) ===========
    @Override
    public UploadResponse upload(UploadRequest request) {
        int attempt = 0;
        while (true) {
            try {
                PutObjectRequest.Builder pb = PutObjectRequest.builder()
                        .bucket(request.getBucket())
                        .key(request.getKey());
                if (request.getContentType() != null) pb.contentType(request.getContentType());
                if (request.getMetadata() != null && !request.getMetadata().isEmpty()) pb.metadata(request.getMetadata());

                PutObjectResponse resp = s3.putObject(pb.build(), RequestBody.fromBytes(request.getBytes()));

                UploadResponse out = new UploadResponse();
                out.setBucket(request.getBucket());
                out.setKey(request.getKey());
                out.setSize(request.getSize());
                out.setEtag(resp.eTag());
                return out;
            } catch (S3Exception e) {
                attempt++;
                log.warn("PutObject attempt {} failed: {}", attempt, e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage());
                if (attempt >= maxRetries) throw new StorageException("PutObject failed after retries", e);
            } catch (Exception e) {
                throw new StorageException("PutObject failed", e);
            }
        }
    }

    // =========== Download methods ===========
    @Override
    public byte[] downloadBytes(String bucket, String key) {
        try (ResponseInputStream<GetObjectResponse> resp = s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = resp.read(buf)) != -1) baos.write(buf, 0, read);
            return baos.toByteArray();
        } catch (S3Exception e) {
            throw new StorageException("Object not found: " + bucket + "/" + key, e);
        } catch (Exception e) {
            throw new StorageException("Failed to download object", e);
        }
    }

    @Override
    public InputStream downloadStream(String bucket, String key) {
        try {
            return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (S3Exception e) {
            throw new StorageException("Object not found: " + bucket + "/" + key, e);
        } catch (Exception e) {
            throw new StorageException("Failed to get download stream", e);
        }
    }

    @Override
    public void downloadTo(String bucket, String key, OutputStream os) {
        try (ResponseInputStream<GetObjectResponse> resp = s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build())) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = resp.read(buf)) != -1) os.write(buf, 0, read);
            os.flush();
        } catch (S3Exception e) {
            throw new StorageException("Object not found: " + bucket + "/" + key, e);
        } catch (Exception e) {
            throw new StorageException("Failed to stream object", e);
        }
    }

    @Override
    public void downloadToFile(String bucket, String key, Path path) {
        try {
            s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build(), path);
        } catch (S3Exception e) {
            throw new StorageException("Object not found: " + bucket + "/" + key, e);
        } catch (Exception e) {
            throw new StorageException("Failed to download to file", e);
        }
    }

    // =========== Head / metadata ===========
    @Override
    public FileInfo getFileInfo(String bucket, String key) {
        try {
            HeadObjectResponse head = s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            FileInfo info = new FileInfo();
            info.setBucket(bucket);
            info.setKey(key);
            info.setSize(head.contentLength());
            info.setETag(head.eTag());
            info.setContentType(head.contentType());
            if (head.lastModified() != null) info.setLastModified(head.lastModified().toEpochMilli());
            return info;
        } catch (S3Exception e) {
            throw new StorageException("HeadObject failed for " + bucket + "/" + key, e);
        }
    }

    // =========== Delete ===========
    @Override
    public boolean delete(String bucket, String key) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (S3Exception e) {
            throw new StorageException("Delete failed for " + bucket + "/" + key, e);
        }
    }

    // =========== Presign ===========
    @Override
    public String getPresignedUrl(String bucket, String key, long expireSeconds) {
        return presignGet(bucket, key, Duration.ofSeconds(expireSeconds));
    }

    public String presignGet(String bucket, String key, Duration expires) {
        try {
            GetObjectRequest getReq = GetObjectRequest.builder().bucket(bucket).key(key).build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getReq)
                    .signatureDuration(expires)
                    .build();
            return presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            throw new StorageException("Failed to create presigned GET URL", e);
        }
    }

    public String presignPut(String bucket, String key, Duration expires, String contentType, Map<String, String> metadata) {
        try {
            PutObjectRequest.Builder putB = PutObjectRequest.builder().bucket(bucket).key(key);
            if (contentType != null) putB.contentType(contentType);
            if (metadata != null && !metadata.isEmpty()) putB.metadata(metadata);
            PutObjectRequest putReq = putB.build();

            PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                    .putObjectRequest(putReq)
                    .signatureDuration(expires)
                    .build();
            return presigner.presignPutObject(presignReq).url().toString();
        } catch (Exception e) {
            throw new StorageException("Failed to create presigned PUT URL", e);
        }
    }

    // =========== Multipart (init/upload/list/complete/abort) ===========
    @Transactional
    public UploadSession initMultipart(String tenantId, String bucket, String key, String contentType, Long totalSize, Duration expiry) {
        try {
            CreateMultipartUploadRequest.Builder builder = CreateMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key);
            if (contentType != null) builder.contentType(contentType);
            CreateMultipartUploadResponse resp = s3.createMultipartUpload(builder.build());
            String uploadId = resp.uploadId();

            UploadSession session = new UploadSession();
            session.setTenantId(tenantId);
            session.setBucket(bucket);
            session.setObjectKey(key);
            session.setUploadId(uploadId);
            session.setContentType(contentType);
            session.setTotalSize(totalSize);
            session.setCreatedAt(Instant.now());
            session.setUpdatedAt(Instant.now());
            session.setExpiresAt(Instant.now().plus(expiry != null ? expiry : Duration.ofHours(24)));
            session.setStatus(UploadStatus.INIT);
            return sessionRepo.save(session);
        } catch (S3Exception e) {
            throw new StorageException("Failed to init multipart upload", e);
        }
    }

    @Transactional
    public String uploadPart(String uploadId, int partNumber, InputStream data, long partSize) {
        UploadSession session = sessionRepo.findByUploadId(uploadId)
                .orElseThrow(() -> new StorageException("Upload session not found: " + uploadId));

        if (session.getStatus() != UploadStatus.INIT) {
            throw new StorageException("Upload session is not active: " + uploadId);
        }

        if (partSize < MIN_PART_SIZE) {
            // allow smaller only if it's the last part and totalSize known and matches
            Long total = session.getTotalSize();
            if (total == null) {
                throw new IllegalArgumentException("part size too small; must be >= " + MIN_PART_SIZE);
            } else {
                // don't strictly assert here; S3 requires min 5MB except last
            }
        }

        Optional<UploadPartRecord> existing = partRepo.findBySessionIdAndPartNumber(session.getId(), partNumber);
        if (existing.isPresent()) return existing.get().getETag();

        try {
            UploadPartRequest req = UploadPartRequest.builder()
                    .bucket(session.getBucket())
                    .key(session.getObjectKey())
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .contentLength(partSize)
                    .build();
            UploadPartResponse resp = s3.uploadPart(req, RequestBody.fromInputStream(data, partSize));
            String etag = resp.eTag();

            UploadPartRecord rec = new UploadPartRecord();
            rec.setSessionId(session.getId());
            rec.setPartNumber(partNumber);
            rec.setETag(etag);
            rec.setSize(partSize);
            rec.setUploadedAt(Instant.now());
            partRepo.save(rec);

            session.setUpdatedAt(Instant.now());
            sessionRepo.save(session);

            return etag;
        } catch (S3Exception e) {
            throw new StorageException("upload part failed", e);
        } catch (Exception e) {
            throw new StorageException("upload part failed", e);
        }
    }

    public List<UploadPartRecord> listParts(String uploadId) {
        UploadSession session = sessionRepo.findByUploadId(uploadId)
                .orElseThrow(() -> new StorageException("Upload session not found: " + uploadId));
        return partRepo.findBySessionIdOrderByPartNumberAsc(session.getId());
    }

    @Transactional
    public void completeMultipart(String uploadId) {
        UploadSession session = sessionRepo.findByUploadId(uploadId)
                .orElseThrow(() -> new StorageException("Upload session not found: " + uploadId));

        List<UploadPartRecord> parts = partRepo.findBySessionIdOrderByPartNumberAsc(session.getId());
        if (parts.isEmpty()) throw new StorageException("No parts uploaded for uploadId: " + uploadId);

        List<CompletedPart> completedParts = parts.stream()
                .sorted(Comparator.comparingInt(UploadPartRecord::getPartNumber))
                .map(p -> CompletedPart.builder().partNumber(p.getPartNumber()).eTag(p.getETag()).build())
                .collect(Collectors.toList());

        CompletedMultipartUpload completed = CompletedMultipartUpload.builder().parts(completedParts).build();
        CompleteMultipartUploadRequest req = CompleteMultipartUploadRequest.builder()
                .bucket(session.getBucket())
                .key(session.getObjectKey())
                .uploadId(uploadId)
                .multipartUpload(completed)
                .build();

        try {
            s3.completeMultipartUpload(req);
            session.setStatus(UploadStatus.COMPLETED);
            session.setUpdatedAt(Instant.now());
            sessionRepo.save(session);
            // optional: keep part records or delete them for cleanup
        } catch (S3Exception e) {
            throw new StorageException("complete multipart failed", e);
        }
    }

    @Transactional
    public void abortMultipart(String uploadId) {
        UploadSession session = sessionRepo.findByUploadId(uploadId)
                .orElseThrow(() -> new StorageException("Upload session not found: " + uploadId));

        try {
            s3.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                    .bucket(session.getBucket())
                    .key(session.getObjectKey())
                    .uploadId(uploadId)
                    .build());
        } catch (S3Exception e) {
            log.warn("abortMultipart S3 error (ignored): {}", e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage());
        } finally {
            partRepo.deleteBySessionId(session.getId());
            session.setStatus(UploadStatus.ABORTED);
            session.setUpdatedAt(Instant.now());
            sessionRepo.save(session);
        }
    }

    public List<Integer> getMissingParts(String uploadId, List<Integer> candidateParts) {
        UploadSession session = sessionRepo.findByUploadId(uploadId)
                .orElseThrow(() -> new StorageException("Upload session not found: " + uploadId));
        Set<Integer> uploaded = partRepo.findBySessionIdOrderByPartNumberAsc(session.getId())
                .stream().map(UploadPartRecord::getPartNumber).collect(Collectors.toSet());
        return candidateParts.stream().filter(p -> !uploaded.contains(p)).collect(Collectors.toList());
    }
}