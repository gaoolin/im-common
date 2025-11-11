package com.im.storage;

import com.im.storage.v1.StorageServiceV1;
import com.im.storage.v1.model.UploadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private StorageServiceV1 service;

    @PostMapping("/upload")
    public Object upload() {
        UploadRequest req = new UploadRequest();
        req.setBucket("default");
        req.setKey("hello.txt");
        req.setBytes("Hello".getBytes());
        return service.upload(req);
    }
}