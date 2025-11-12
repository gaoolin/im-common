package com.im.storage.controller.v1;

import com.im.storage.model.BaseResponse;
import com.im.storage.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * V1版本存储服务控制器
 * 提供向后兼容的RESTful API接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
@RestController
@RequestMapping("/api/v1")
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
        return null;
    }

    /**
     * 下载对象 - V1版本
     */
    @GetMapping("/objects/{bucketName}/{objectKey}")
    public void getObject(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            HttpServletResponse response) {
    }

    /**
     * 删除对象 - V1版本
     */
    @DeleteMapping("/objects/{bucketName}/{objectKey}")
    public ResponseEntity<BaseResponse<String>> deleteObject(
            @PathVariable String bucketName,
            @PathVariable String objectKey) {
        return null;
    }
}