package com.im.storage.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.storage.model.BaseResponse;
import com.im.storage.service.OssService;
import com.im.storage.util.FileNameUtils;
import org.im.common.json.JsonMapperProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;

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

    private final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();
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

    /**
     * 上传对象（Base64格式）- V1版本
     * 用于接收Base64编码的文件内容并上传到存储系统
     */
    @PostMapping("/objects/base64")
    public ResponseEntity<BaseResponse<String>> putObjectBase64(
            @RequestParam String bucketName,
            @RequestParam String objectKey,
            @RequestBody String base64Content) {

        // 添加输入验证
        if (bucketName == null || bucketName.isEmpty()) {
            BaseResponse<String> errorResponse = new BaseResponse<>();
            errorResponse.setCode(400);
            errorResponse.setMessage("bucketName不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (objectKey == null || objectKey.isEmpty()) {
            BaseResponse<String> errorResponse = new BaseResponse<>();
            errorResponse.setCode(400);
            errorResponse.setMessage("objectKey不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // 检查对象是否已存在
            boolean objectExists = ossService.objectExists(bucketName, objectKey);

            // 处理文件重命名逻辑（如果对象已存在）
            Map<String, String> renameData = FileNameUtils.getFileNameWithPossibleRename(objectKey, objectExists);
            String actualObjectName = renameData != null ? renameData.get("newFileName") : objectKey;

            // 解码Base64内容
            byte[] decodedContent = Base64.getDecoder().decode(base64Content);

            // 上传对象 - 修正调用，添加contentType参数
            ossService.putObject(bucketName, actualObjectName,
                    new ByteArrayInputStream(decodedContent),
                    "application/octet-stream");

            // 构建响应
            BaseResponse<String> response = new BaseResponse<>();
            response.setCode(200);

            if (renameData != null) {
                // 如果对象被重命名，返回重命名信息
                response.setMessage("对象已重命名并上传成功");
                response.setData(objectMapper.writeValueAsString(renameData));
            } else {
                response.setMessage("对象上传成功");
                response.setData(null);
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            BaseResponse<String> errorResponse = new BaseResponse<>();
            errorResponse.setCode(400);
            errorResponse.setMessage("Base64内容格式错误: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            BaseResponse<String> errorResponse = new BaseResponse<>();
            errorResponse.setCode(500);
            errorResponse.setMessage("对象上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}