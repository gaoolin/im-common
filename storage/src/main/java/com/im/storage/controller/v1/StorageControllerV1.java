package com.im.storage.controller.v1;

import com.im.storage.model.BaseResponse;
import com.im.storage.model.dto.GetObjectRequest;
import com.im.storage.model.dto.PutObjectRequest;
import com.im.storage.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */

/**
 * V1版本存储服务控制器
 * 提供向后兼容的RESTful API接口
 */
@RestController
@RequestMapping("/api/v1/storage")
public class StorageControllerV1 {

    @Autowired
    private OssService ossService;

    /**
     * 上传对象 - V1版本
     */
    @PostMapping("/objects")
    public ResponseEntity<BaseResponse<String>> putObject(
            @RequestParam String bucketName,
            @RequestParam String objectKey,
            @RequestParam MultipartFile file) {
        try {
            InputStream content = file.getInputStream();
            String contentType = file.getContentType();

            ossService.putObject(bucketName, objectKey, content, contentType);

            return ResponseEntity.ok(BaseResponse.success("Object uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                BaseResponse.error(500, "Failed to upload object: " + e.getMessage())
            );
        }
    }

    /**
     * 下载对象 - V1版本
     */
    @GetMapping("/objects/{bucketName}/{objectKey}")
    public void getObject(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            HttpServletResponse response) {
        try {
            InputStream inputStream = ossService.getObject(bucketName, objectKey);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + objectKey + "\"");

            inputStream.transferTo(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            response.setStatus(500);
        }
    }

    /**
     * 删除对象 - V1版本
     */
    @DeleteMapping("/objects/{bucketName}/{objectKey}")
    public ResponseEntity<BaseResponse<String>> deleteObject(
            @PathVariable String bucketName,
            @PathVariable String objectKey) {
        try {
            ossService.deleteObject(bucketName, objectKey);
            return ResponseEntity.ok(BaseResponse.success("Object deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                BaseResponse.error(500, "Failed to delete object: " + e.getMessage())
            );
        }
    }
}