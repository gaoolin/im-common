package com.im.storage.controller.v2;

import com.im.storage.model.BaseResponse;
import com.im.storage.model.dto.GetObjectRequest;
import com.im.storage.model.dto.ObjectInfo;
import com.im.storage.model.dto.ObjectMetadata;
import com.im.storage.model.dto.PutObjectRequest;
import com.im.storage.service.OssService;
import com.im.storage.service.storage.StorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */


/**
 * V2版本存储服务控制器
 * 提供增强功能的RESTful API接口，支持多存储类型
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
    public ResponseEntity<BaseResponse<ObjectMetadata>> putObject(
            @RequestParam String bucketName,
            @RequestParam String objectKey,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String storageType) {
        try {
            InputStream content = file.getInputStream();
            String contentType = file.getContentType();

            ObjectMetadata metadata;
            if (storageType != null && !storageType.isEmpty()) {
                metadata = ossService.putObject(bucketName, objectKey, content,
                                              contentType, StorageType.fromString(storageType));
            } else {
                metadata = ossService.putObject(bucketName, objectKey, content, contentType);
            }

            return ResponseEntity.ok(BaseResponse.success(metadata));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                BaseResponse.error(500, "Failed to upload object: " + e.getMessage())
            );
        }
    }

    /**
     * 下载对象 - V2版本（支持指定存储类型）
     */
    @GetMapping("/objects/{bucketName}/{objectKey}")
    public void getObject(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam(required = false) String storageType,
            HttpServletResponse response) {
        try {
            InputStream inputStream;
            if (storageType != null && !storageType.isEmpty()) {
                inputStream = ossService.getObject(bucketName, objectKey,
                                                 StorageType.fromString(storageType));
            } else {
                inputStream = ossService.getObject(bucketName, objectKey);
            }

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + objectKey + "\"");

            inputStream.transferTo(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            response.setStatus(500);
        }
    }

    /**
     * 删除对象 - V2版本（支持指定存储类型）
     */
    @DeleteMapping("/objects/{bucketName}/{objectKey}")
    public ResponseEntity<BaseResponse<String>> deleteObject(
            @PathVariable String bucketName,
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
            return ResponseEntity.status(500).body(
                BaseResponse.error(500, "Failed to delete object: " + e.getMessage())
            );
        }
    }

    /**
     * 列出对象 - V2版本（支持指定存储类型）
     */
    @GetMapping("/objects/{bucketName}")
    public ResponseEntity<BaseResponse<List<ObjectInfo>>> listObjects(
            @PathVariable String bucketName,
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
            return ResponseEntity.status(500).body(
                BaseResponse.error(500, "Failed to list objects: " + e.getMessage())
            );
        }
    }

    /**
     * 获取对象元数据 - V2版本（支持指定存储类型）
     */
    @GetMapping("/objects/{bucketName}/{objectKey}/metadata")
    public ResponseEntity<BaseResponse<ObjectMetadata>> getObjectMetadata(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam(required = false) String storageType) {
        try {
            ObjectMetadata metadata;
            if (storageType != null && !storageType.isEmpty()) {
                metadata = ossService.getObjectMetadata(bucketName, objectKey,
                                                      StorageType.fromString(storageType));
            } else {
                metadata = ossService.getObjectMetadata(bucketName, objectKey);
            }

            return ResponseEntity.ok(BaseResponse.success(metadata));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                BaseResponse.error(500, "Failed to get object metadata: " + e.getMessage())
            );
        }
    }
}