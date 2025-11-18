package com.im.inspection.controller.chk;

import com.im.inspection.annotation.QtechValidSimId;
import com.im.inspection.service.chk.IEqpReverseMesService;
import com.im.inspection.util.chk.EqpReverseResponseHandler;
import com.im.inspection.util.response.MesR;
import com.im.inspection.util.response.ResponseCode;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.im.qtech.data.constant.QtechImBizConstant.REDIS_KEY_PREFIX_EQP_REVERSE_INFO;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/13 17:24:23
 */
@RestController
@RequestMapping("/im/chk")
@Validated
@Tag(name = "智能制造参数点检反控接口")
public class EqpReverseMesController {

    private static final Logger logger = LoggerFactory.getLogger(EqpReverseMesController.class);
    private static final String NO_DATA_FOUND_MSG = "No data found";
    private static final String DATABASE_QUERY_ERROR_MSG = "Database query error";

    private final IEqpReverseMesService service;
    private final RedisTemplate<String, EqpReversePOJO> redisTemplate;

    @Autowired
    public EqpReverseMesController(IEqpReverseMesService service, RedisTemplate<String, EqpReversePOJO> redisTemplate) {
        this.service = service;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取设备反向控制信息
     *
     * @param simId   盒子号
     * @param request HTTP请求对象
     * @return 设备反向控制信息响应
     */
    @GetMapping("/{simId}")
    @Operation(summary = "获取设备反向控制信息")
    public MesR<String> getEqReverseInfo(@Parameter(name = "盒子号", description = "例如：86xxxx", required = true) @PathVariable @QtechValidSimId String simId, HttpServletRequest request) {

        // Step 1: Try to fetch from Redis
        EqpReversePOJO infoFromRedis = fetchFromRedis(simId, request);
        if (infoFromRedis != null) {
            return EqpReverseResponseHandler.handleResponse(infoFromRedis);
        }

        // Step 2: Fallback to database
        return fetchAndHandleFromDatabase(simId, request);
    }

    /**
     * 从Redis中获取设备反向控制信息
     *
     * @param simId   盒子号
     * @param request HTTP请求对象
     * @return 设备反向控制信息，获取失败则返回null
     */
    private EqpReversePOJO fetchFromRedis(String simId, HttpServletRequest request) {
        try {
            return redisTemplate.opsForValue().get(REDIS_KEY_PREFIX_EQP_REVERSE_INFO + simId);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving data from Redis for SIM ID {}, Request URI: {}", simId, request.getRequestURI(), e);
            return null;
        }
    }

    /**
     * 从数据库获取并处理设备反向控制信息
     *
     * @param simId   盒子号
     * @param request HTTP请求对象
     * @return 处理后的响应对象
     */
    private MesR<String> fetchAndHandleFromDatabase(String simId, HttpServletRequest request) {
        EqpReversePOJO pojo;
        try {
            pojo = fetchFromDatabase(simId);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving data from database for SIM ID {}, Request URI: {}", simId, request.getRequestURI(), e);
            return buildQueryErrorResponse();
        }

        if (pojo == null) {
            logger.warn("No data found for SIM ID {}, Request URI: {}", simId, request.getRequestURI());
            return buildNotFoundResponse();
        }

        return EqpReverseResponseHandler.handleResponse(pojo);
    }

    /**
     * 从数据库获取设备反向控制信息
     *
     * @param simId 盒子号
     * @return 设备反向控制信息
     */
    private EqpReversePOJO fetchFromDatabase(String simId) {
        return service.getOneBySimId(simId);
    }

    /**
     * 构建未找到数据的响应
     *
     * @return 未找到数据响应对象
     */
    private MesR<String> buildNotFoundResponse() {
        return new MesR<String>().setCode(ResponseCode.SUCCESS.getCode()).setMsg(NO_DATA_FOUND_MSG).setData(null);
    }

    /**
     * 构建数据库查询错误的响应
     *
     * @return 数据库查询错误响应对象
     */
    private MesR<String> buildQueryErrorResponse() {
        return new MesR<String>().setCode(ResponseCode.SUCCESS.getCode()).setMsg(DATABASE_QUERY_ERROR_MSG).setData(null);
    }
}
