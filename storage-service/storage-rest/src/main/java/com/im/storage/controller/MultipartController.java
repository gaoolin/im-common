package com.im.storage.controller;

import com.im.storage.CephS3StorageService;
import com.im.storage.dto.MultipartInitRequest;
import com.im.storage.dto.MultipartUploadCompleteRequest;
import com.im.storage.dto.MultipartUploadPartRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static javafx.scene.input.KeyCode.R;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

@RestController
@RequestMapping("/api/storage/multipart")
@RequiredArgsConstructor
public class MultipartController {

    private final CephS3StorageService multipartService; // 来自 provider

    @PostMapping("/init")
    public R<MultipartInitResult> init(@RequestBody MultipartInitRequest req) {
        return R.ok(multipartService.initUpload(req));
    }

    @PutMapping("/upload")
    public R<?> uploadPart(MultipartUploadPartRequest req, @RequestPart("file") MultipartFile file) {
        multipartService.uploadPart(req, file);
        return R.ok();
    }

    @PostMapping("/complete")
    public R<?> complete(@RequestBody MultipartUploadCompleteRequest req) {
        multipartService.completeUpload(req);
        return R.ok();
    }
}
