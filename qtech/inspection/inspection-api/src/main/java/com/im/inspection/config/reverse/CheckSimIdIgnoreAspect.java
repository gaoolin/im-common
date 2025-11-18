package com.im.inspection.config.reverse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static com.im.inspection.util.chk.EqpReverseResponseHandler.createSuccessResponse;
import static com.im.qtech.data.constant.QtechImBizConstant.REDIS_KEY_PREFIX_EQP_REVERSE_IGNORE_SIM;

/**
 * 定义SimIdCheckAspect切面，该切面会在控制器方法执行之前拦截请求，检查simId是否在Redis中存在。
 * <p>
 * SimIdCheckAspect 切面类中使用 @Around 注解来拦截getEqReverseInfo方法的执行。它首先检查simId是否存在于Redis中。
 * 如果存在，则直接返回一个成功响应R<String>，否则继续执行控制器的方法。
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/15 09:50:37
 */
@Aspect
@Component
public class CheckSimIdIgnoreAspect {
    private static final Logger logger = LoggerFactory.getLogger(CheckSimIdIgnoreAspect.class);

    @Autowired
    private RedisTemplate<String, Boolean> redisTemplate;

    @Pointcut("execution(* com.im.inspection.controller.chk.EqpReverseMesController.getEqReverseInfo(..))")
    public void checkSimIdPointcut() {
    }

    @Around(value = "checkSimIdPointcut()")
    public Object checkSimIdAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        // 参数基础校验
        if (args == null || args.length == 0 || !(args[0] instanceof String)) {
            logger.warn(">>>>> Invalid arguments received");
            return createSuccessResponse("Invalid request parameters");
        }

        String encodedSimId = (String) args[0];

        // 校验 simId 是否为空或过长
        if (encodedSimId.isEmpty() || encodedSimId.length() > 100) {
            logger.warn(">>>>> Invalid simId length or empty value");
            return createSuccessResponse("Invalid simId format");
        }

        String simId;
        try {
            simId = URLDecoder.decode(encodedSimId, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            logger.warn(">>>>> Failed to decode simId: {}", encodedSimId, e);
            return createSuccessResponse("Invalid simId encoding");
        }

        // 记录原始请求路径（注意不要泄露敏感信息）
        logger.info("Received request for simId");

        try {
            Boolean exists = redisTemplate.hasKey(REDIS_KEY_PREFIX_EQP_REVERSE_IGNORE_SIM + sanitizeSimId(simId));
            if (Boolean.TRUE.equals(exists)) {
                logger.info(">>>>> simId is ignored: {}", simId);
                return createSuccessResponse("Equipment reverse ignored");
            } else {
                return joinPoint.proceed();
            }
        } catch (ConstraintViolationException ex) {
            Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
            String message = "Validation failed";
            if (!violations.isEmpty()) {
                ConstraintViolation<?> violation = violations.iterator().next();
                message = violation.getMessage();
            }
            logger.warn(">>>>> ConstraintViolationException caught in aspect: {}", message, ex);
            return createSuccessResponse(message);
        } catch (Throwable t) {
            logger.error(">>>>> Unexpected error occurred in aspect", t);
            return createSuccessResponse("Internal server error");
        }
    }


    /**
     * 清洗 simId 字符串防止 Redis key 注入
     */
    private String sanitizeSimId(String simId) {
        if (simId == null) return "";
        return simId.replaceAll("[\r\n\t]", "_"); // 替换掉可能引起问题的控制字符
    }
}
