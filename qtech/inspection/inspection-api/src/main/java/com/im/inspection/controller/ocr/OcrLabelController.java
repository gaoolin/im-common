package com.im.inspection.controller.ocr; // 注意包名修正

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.im.inspection.service.ocr.IOCRLabelService;
import com.im.inspection.util.http.HttpUtils;
import com.im.inspection.util.response.ApiR;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.im.qtech.data.constant.QtechImBizConstant.OCR_HTTP_URL;

/**
 * OCR API
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2023/07/31 17:00:23
 */

@RestController
@RequestMapping("/im/ocr")
public class OcrLabelController {
    private static final Logger logger = LoggerFactory.getLogger(OcrLabelController.class);
    private final ObjectMapper objectMapper = new ObjectMapper(); // 使用单例模式
    private final IOCRLabelService IOCRLabelService;

    public OcrLabelController(IOCRLabelService IOCRLabelService) {
        this.IOCRLabelService = IOCRLabelService;
    }

    /**
     * WebDataBinder是Spring MVC框架中的一个重要组件，主要用于处理HTTP请求中的参数，并将其绑定到控制器方法的参数或模型对象上。以下是WebDataBinder的主要作用和过程：
     * 主要作用：
     * 数据绑定：将HTTP请求中的参数值绑定到Java对象的属性上。
     * 类型转换：将请求中的字符串参数转换为相应的Java类型。
     * 格式化：根据指定的格式模式解析日期等类型。
     * 验证：执行参数的验证逻辑，并收集验证错误。
     * 工作过程：
     * 创建WebDataBinder：当一个请求到达时，Spring MVC会为每个需要绑定的对象创建一个WebDataBinder实例。
     * 初始化WebDataBinder：
     * 调用@InitBinder注解的方法，允许开发者自定义数据绑定的行为，如注册自定义的PropertyEditor或TypeConverter。
     * 可以覆盖默认的类型转换器、格式化器或验证器。
     * 绑定数据：
     * 根据请求参数的名称和值，WebDataBinder会查找匹配的属性并尝试进行绑定。
     * 如果存在类型不匹配，则会尝试使用注册的类型转换器进行转换。
     * 验证数据：
     * 绑定完成后，会调用验证器（如果有的话）对绑定后的对象进行验证。
     * 验证失败的信息会被收集到BindingResult对象中。
     * 错误处理：
     * 如果有验证错误，可以通过BindingResult对象获取错误信息，并决定如何处理这些错误。
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {

    }

    /**
     * 获取 OCR 信息并上传文件
     *
     * @param request 请求体，包含文件信息
     * @return OCR API 返回的响应
     */
    @Operation(summary = "get ocr result")
    @RequestMapping(value = "/label", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiR<String> getOcrResult(@RequestBody Map<String, String> request) throws JsonProcessingException { // 更改请求体类型
        // 从请求体中提取信息
        String bucketName = "qtech-20230717";
        String fileName = request.get("fileName");
        String base64Contents = request.get("contents");

        String response = IOCRLabelService.S3Obj(bucketName, fileName, base64Contents);
        logger.info(">>>>> S3Obj: " + response);

        ApiR<String> ApiR = objectMapper.readValue(response, new TypeReference<ApiR<String>>() {
        }); // 使用Jackson解析
        int responseCode = ApiR.getCode();
        String data = ApiR.getData();

        // 检查响应内容
        if (responseCode == 200) {
            Map<String, String> map = new HashMap<>();
            if (data != null) {
                // 使用TypeReference确保类型安全
                HashMap<String, String> fileNameResponse = null;
                try {
                    fileNameResponse = objectMapper.readValue(data, new TypeReference<HashMap<String, String>>() {
                    });
                } catch (Exception e) {
                    logger.error("Error parsing response: " + e.getMessage());
                }
                String newFileName = null;
                if (fileNameResponse != null) {
                    newFileName = fileNameResponse.getOrDefault("newFileName", "");
                }
                String originalFileName = null;
                if (fileNameResponse != null) {
                    originalFileName = fileNameResponse.getOrDefault("originalFileName", "");
                }
                map.put("file_name", newFileName);
                map.put("new_file_name", newFileName);
                map.put("original_file_name", originalFileName);
            } else {
                map.put("file_name", fileName);
            }
            try {
                String ocrResponse = HttpUtils.post(OCR_HTTP_URL, objectMapper.writeValueAsString(map)); // 使用Jackson序列化
                logger.info(">>>>> HttpConnectUtils: " + OCR_HTTP_URL);
                logger.info(">>>>> cocrRequest: {}, \nocrResponse: {}", OCR_HTTP_URL, ocrResponse);
                JsonNode jsonNode = null;
                try {
                    jsonNode = objectMapper.readTree(ocrResponse);
                    int ocrCode = jsonNode.get("code").asInt();
                    if (ocrCode == 200) {
                        JsonNode dataNode = jsonNode.get("data");
                        if (dataNode != null) {
                            // 检查 dataNode 是否为 ObjectNode
                            if (dataNode instanceof ObjectNode) {
                                ((ObjectNode) dataNode).remove("ms");
                            }
                            // 将 dataNode 转换成字符串
                            String dataStr = objectMapper.writeValueAsString(dataNode);
                            return com.im.inspection.util.response.ApiR.success("success", dataStr); // 返回字符串
                        } else {
                            return com.im.inspection.util.response.ApiR.badRequest("ocr服务返回结果异常");
                        }
                    } else {
                        return new ApiR<>(ocrCode, jsonNode.get("msg").asText());
                    }
                } catch (JsonProcessingException e) {
                    return com.im.inspection.util.response.ApiR.badRequest("ocr服务返回结果异常");
                }
            } catch (Exception e) {
                return com.im.inspection.util.response.ApiR.badRequest("请求ocr服务时发生错误");
            }
        }
        return ApiR;
    }
}
