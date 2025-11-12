package com.im.storage.controller.v2;

import com.im.storage.model.BaseResponse;
import com.im.storage.model.dto.ObjectInfo;
import com.im.storage.model.dto.ObjectMetadata;
import com.im.storage.service.OssService;
import com.im.storage.service.storage.StorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

/**
 * V2版本存储服务控制器
 * 提供增强功能的RESTful API接口，支持多存储类型
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
@RestController
@RequestMapping("/api/v2/storage")
public class StorageControllerV2 {

    @Autowired
    private OssService ossService;

    /**
     * 上传对象 - V2版本（支持指定存储类型）
     */
    @PostMapping("/objects")
    public ResponseEntity<BaseResponse<String>> putObject(
            @RequestParam String bucketName,
            @RequestParam String objectKey,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String storageType) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    BaseResponse.error(400, "File is empty or not provided")
            );
        }

        InputStream content = null;
        try {
            content = file.getInputStream();
            String contentType = file.getContentType();

            if (storageType != null && !storageType.isEmpty()) {
                ossService.putObject(bucketName, objectKey, content, contentType, StorageType.fromString(storageType));
            } else {
                ossService.putObject(bucketName, objectKey, content, contentType);
            }

            return ResponseEntity.ok(BaseResponse.success("Object uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    BaseResponse.error(500, "Failed to upload object: " + e.getMessage())
            );
        } finally {
            // 确保输入流被正确关闭
            if (content != null) {
                try {
                    content.close();
                } catch (Exception ignored) {
                }
            }
        }
    }


    /**
     * 下载对象 - V2版本（支持指定存储类型）
     */
    @GetMapping("/objects/{bucketName}/{objectKey}")
    public void getObject(@PathVariable String bucketName,
                          @PathVariable String objectKey,
                          @RequestParam(required = false) String storageType,
                          HttpServletResponse response) {
        try {
            InputStream inputStream;
            if (storageType != null && !storageType.isEmpty()) {
                inputStream = ossService.getObject(bucketName, objectKey, StorageType.fromString(storageType));
            } else {
                inputStream = ossService.getObject(bucketName, objectKey);
            }

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + objectKey + "\"");

            // 兼容 Java 8 的写法
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }

            response.getOutputStream().flush();
            inputStream.close();
        } catch (Exception e) {
            try {
                response.setStatus(500);
                response.getWriter().write("Error downloading file: " + e.getMessage());
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 检查存储桶是否存在 - V2版本
     */
    @GetMapping("/buckets/{bucketName}/exists")
    public ResponseEntity<BaseResponse<Boolean>> bucketExists(@PathVariable String bucketName,
                                                              @RequestParam(required = false) String storageType) {
        try {
            boolean exists;
            if (storageType != null && !storageType.isEmpty()) {
                exists = ossService.bucketExists(bucketName, StorageType.fromString(storageType));
            } else {
                exists = ossService.bucketExists(bucketName);
            }
            return ResponseEntity.ok(BaseResponse.success(exists));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(BaseResponse.error(500, "Failed to check bucket existence: " + e.getMessage()));
        }
    }

    /**
     * 创建存储桶 - V2版本
     */
    @PostMapping("/buckets/{bucketName}")
    public ResponseEntity<BaseResponse<String>> createBucket(@PathVariable String bucketName,
                                                             @RequestParam(required = false) String storageType) {
        try {
            if (storageType != null && !storageType.isEmpty()) {
                ossService.createBucket(bucketName, StorageType.fromString(storageType));
            } else {
                ossService.createBucket(bucketName);
            }
            return ResponseEntity.ok(BaseResponse.success("Bucket created successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(BaseResponse.error(500, "Failed to create bucket: " + e.getMessage()));
        }
    }

    /**
     * 删除存储桶 - V2版本
     */
    @DeleteMapping("/buckets/{bucketName}")
    public ResponseEntity<BaseResponse<String>> deleteBucket(@PathVariable String bucketName,
                                                             @RequestParam(required = false) String storageType) {
        try {
            if (storageType != null && !storageType.isEmpty()) {
                ossService.deleteBucket(bucketName, StorageType.fromString(storageType));
            } else {
                ossService.deleteBucket(bucketName);
            }
            return ResponseEntity.ok(BaseResponse.success("Bucket deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(BaseResponse.error(500, "Failed to delete bucket: " + e.getMessage()));
        }
    }

    /**
     * 删除对象 - V2版本（支持指定存储类型）
     */
    @DeleteMapping("/objects/{bucketName}/{objectKey}")
    public ResponseEntity<BaseResponse<String>> deleteObject(@PathVariable String bucketName,
                                                             @PathVariable String objectKey,
                                                             @RequestParam(required = false) String storageType) {
        try {
            if (storageType != null && !storageType.isEmpty()) {
                ossService.deleteObject(bucketName, objectKey, StorageType.fromString(storageType));
            } else {
                ossService.deleteObject(bucketName, objectKey);
            }
            return ResponseEntity.ok(BaseResponse.success("Object deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(BaseResponse.error(500, "Failed to delete object: " + e.getMessage()));
        }
    }

    /**
     * 列出对象 - V2版本（支持指定存储类型）
     */
    @GetMapping("/objects/{bucketName}")
    public ResponseEntity<BaseResponse<List<ObjectInfo>>> listObjects(@PathVariable String bucketName,
                                                                      @RequestParam(required = false) String prefix,
                                                                      @RequestParam(required = false) String storageType) {
        try {
            List<ObjectInfo> objects;
            if (storageType != null && !storageType.isEmpty()) {
                objects = ossService.listObjects(bucketName, prefix, StorageType.fromString(storageType));
            } else {
                objects = ossService.listObjects(bucketName, prefix);
            }

            return ResponseEntity.ok(BaseResponse.success(objects));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(BaseResponse.error(500, "Failed to list objects: " + e.getMessage()));
        }
    }

    /**
     * 获取对象元数据 - V2版本（支持指定存储类型）
     */
    @GetMapping("/objects/{bucketName}/{objectKey}/metadata")
    public ResponseEntity<BaseResponse<ObjectMetadata>> getObjectMetadata(@PathVariable String bucketName,
                                                                          @PathVariable String objectKey,
                                                                          @RequestParam(required = false) String storageType) {
        try {
            ObjectMetadata metadata;
            if (storageType != null && !storageType.isEmpty()) {
                metadata = ossService.getObjectMetadata(bucketName, objectKey, StorageType.fromString(storageType));
            } else {
                metadata = ossService.getObjectMetadata(bucketName, objectKey);
            }

            return ResponseEntity.ok(BaseResponse.success(metadata));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(BaseResponse.error(500, "Failed to get object metadata: " + e.getMessage()));
        }
    }

    /**
     * 生成预签名URL - V2版本
     */
    @GetMapping("/presigned-url")
    public ResponseEntity<BaseResponse<String>> generatePresignedUrl(
            @RequestParam String bucketName,
            @RequestParam String objectKey,
            @RequestParam String urlType, // UPLOAD 或 DOWNLOAD
            @RequestParam(required = false) String storageType,
            @RequestParam(defaultValue = "3600") long expirationSeconds) {
        try {
            Duration expiration = Duration.ofSeconds(expirationSeconds);
            String presignedUrl;

            if ("UPLOAD".equalsIgnoreCase(urlType)) {
                if (storageType != null && !storageType.isEmpty()) {
                    presignedUrl = ossService.generatePresignedUploadUrl(bucketName, objectKey, expiration, StorageType.fromString(storageType));
                } else {
                    presignedUrl = ossService.generatePresignedUploadUrl(bucketName, objectKey, expiration);
                }
            } else if ("DOWNLOAD".equalsIgnoreCase(urlType)) {
                if (storageType != null && !storageType.isEmpty()) {
                    presignedUrl = ossService.generatePresignedDownloadUrl(bucketName, objectKey, expiration, StorageType.fromString(storageType));
                } else {
                    presignedUrl = ossService.generatePresignedDownloadUrl(bucketName, objectKey, expiration);
                }
            } else {
                return ResponseEntity.badRequest().body(BaseResponse.error(400, "Invalid urlType. Must be UPLOAD or DOWNLOAD"));
            }

            return ResponseEntity.ok(BaseResponse.success(presignedUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(BaseResponse.error(500, "Failed to generate presigned URL: " + e.getMessage()));
        }
    }
}