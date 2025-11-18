package com.im.inspection.service.ocr.impl;

import com.im.inspection.service.ocr.IOCRLabelService;
import com.im.inspection.util.response.ApiR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static com.im.qtech.data.constant.QtechImBizConstant.CEPH_HTTP_URL;

/**
 * OCR服务层
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2023/07/31 17:04:14
 */
@Service
public class IOCRLabelServiceImpl implements IOCRLabelService {

    private static final Logger logger = LoggerFactory.getLogger(IOCRLabelServiceImpl.class);

    private final RestTemplate restTemplate;

    public IOCRLabelServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String S3Obj(String bucketName, String fileName, String contents) {
        // 拼接请求 URL，包括请求参数
        String url = String.format(CEPH_HTTP_URL, bucketName, fileName);
        logger.info(">>>>> Request URL: {}", url);
        // 创建 HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(contents, headers);

        try {
            // 发送 HTTP POST 请求
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            // 获取响应体并返回
            String body = responseEntity.getBody();
            if (Objects.nonNull(body)) {
                return body;
            }
        } catch (Exception e) {
            // 如果出现异常，返回服务器内部错误响应
            logger.error("Error occurred while sending HTTP request: ", e);
        }
        return ApiR.badRequest("Error occurred while sending HTTP request").toString();
    }
}

