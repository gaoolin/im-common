package com.im.inspection.controller.ocr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.im.inspection.service.ocr.IOCRLabelService;
import com.im.inspection.util.http.HttpUtils;
import com.im.inspection.util.response.ApiR;
import io.swagger.v3.oas.annotations.Operation;
import org.im.common.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.im.qtech.data.constant.QtechImBizConstant.OCR_HTTP_URL_DEV;

/**
 * OCR标签识别控制器
 * 提供OCR识别相关的API接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2023/07/31 17:00:23
 */
@RestController
@RequestMapping("/im/ocr")
public class OcrLabelController {
    private static final Logger logger = LoggerFactory.getLogger(OcrLabelController.class);
    private final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();
    private final IOCRLabelService ocrLabelService;

    public OcrLabelController(IOCRLabelService ocrLabelService) {
        this.ocrLabelService = ocrLabelService;
    }

    /**
     * 获取OCR识别结果并上传文件
     *
     * @param request 请求体，包含fileName和contents字段
     * @return OCR识别结果响应
     */
    @Operation(summary = "获取OCR识别结果")
    @PostMapping(value = "/label", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiR<String> getOcrResult(@RequestBody Map<String, String> request) {
        // 输入参数校验
        if (request == null) {
            return ApiR.badRequest("请求参数不能为空");
        }

        String fileName = request.get("fileName");
        String contents = request.get("contents");

        if (fileName == null || fileName.isEmpty()) {
            return ApiR.badRequest("fileName参数不能为空");
        }

        if (contents == null || contents.isEmpty()) {
            return ApiR.badRequest("contents参数不能为空");
        }

        try {
            // 上传文件到S3并获取响应
            String s3Response = ocrLabelService.S3Obj("qtech-20230717", fileName, contents);
            logger.info("S3上传响应: {}", s3Response);

            // 解析S3上传响应
            ApiR<String> s3ApiResponse = objectMapper.readValue(s3Response, new TypeReference<ApiR<String>>() {});

            if (s3ApiResponse.getCode() != 200) {
                return s3ApiResponse;
            }

            // 构建OCR请求参数
            Map<String, String> ocrRequestParams = buildOcrRequestParams(fileName, s3ApiResponse.getData());

            // 调用OCR服务
            return callOcrService(ocrRequestParams);

        } catch (JsonProcessingException e) {
            logger.error("JSON解析失败: {}", e.getMessage(), e);
            return ApiR.badRequest("响应数据解析失败");
        } catch (Exception e) {
            logger.error("处理OCR请求时发生错误: {}", e.getMessage(), e);
            return ApiR.badRequest("处理请求时发生错误");
        }
    }

    /**
     * 构建OCR服务请求参数
     */
    private Map<String, String> buildOcrRequestParams(String originalFileName, String s3ResponseData) {
        Map<String, String> params = new HashMap<>();

        if (s3ResponseData != null) {
            try {
                Map<String, String> fileNameResponse = objectMapper.readValue(
                    s3ResponseData,
                    new TypeReference<HashMap<String, String>>() {}
                );

                String newFileName = Optional.ofNullable(fileNameResponse.get("newFileName")).orElse("");
                String originalFileNameFromResponse = Optional.ofNullable(fileNameResponse.get("originalFileName")).orElse("");

                params.put("file_name", newFileName);
                params.put("new_file_name", newFileName);
                params.put("original_file_name", originalFileNameFromResponse);
            } catch (Exception e) {
                logger.error("解析文件名响应失败: {}", e.getMessage());
                params.put("file_name", originalFileName);
            }
        } else {
            params.put("file_name", originalFileName);
        }

        return params;
    }

    /**
     * 调用OCR服务
     */
    private ApiR<String> callOcrService(Map<String, String> requestParams) {
        try {
            String requestBody = objectMapper.writeValueAsString(requestParams);
            String ocrResponse = HttpUtils.post(OCR_HTTP_URL_DEV, requestBody);

            logger.info("OCR请求URL: {}, 请求参数: {}, 响应: {}", OCR_HTTP_URL_DEV, requestBody, ocrResponse);

            JsonNode jsonResponse = objectMapper.readTree(ocrResponse);
            int ocrCode = jsonResponse.get("code").asInt();

            if (ocrCode != 200) {
                return new ApiR<>(ocrCode, jsonResponse.get("msg").asText());
            }

            JsonNode dataNode = jsonResponse.get("data");
            if (dataNode == null) {
                return ApiR.badRequest("OCR服务返回结果异常");
            }

            // 移除时间戳字段
            if (dataNode instanceof ObjectNode) {
                ((ObjectNode) dataNode).remove("ms");
            }

            String resultData = objectMapper.writeValueAsString(dataNode);
            return ApiR.success("success", resultData);

        } catch (JsonProcessingException e) {
            logger.error("OCR服务响应解析失败: {}", e.getMessage(), e);
            return ApiR.badRequest("OCR服务返回结果异常");
        } catch (Exception e) {
            logger.error("请求OCR服务时发生错误: {}", e.getMessage(), e);
            return ApiR.badRequest("请求OCR服务时发生错误");
        }
    }
}
