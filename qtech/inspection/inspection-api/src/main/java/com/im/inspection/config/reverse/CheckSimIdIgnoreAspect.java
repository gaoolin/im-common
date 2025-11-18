package com.im.inspection.config.reverse;

import com.im.inspection.util.response.MesR;
import com.im.inspection.util.response.ResponseCode;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

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
        // 获取参数并校验合法性
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0 || !(args[0] instanceof String)) {
            logger.warn(">>>>> Invalid arguments received");
            return new MesR<String>().setCode(ResponseCode.SUCCESS.getCode()).setMsg("Invalid request parameters").setData(null);
        }

        String encodedSimId = (String) args[0];
        String simId;
        try {
            simId = URLDecoder.decode(encodedSimId, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            logger.warn(">>>>> Failed to decode simId: {}", encodedSimId, e);
            return new MesR<String>().setCode(ResponseCode.SUCCESS.getCode()).setMsg("Invalid simId encoding").setData(null);
        }

        // 打印原始请求路径
        logger.info(">>>>> Received request for path: {}", simId);

        try {
            Boolean exists = redisTemplate.hasKey(REDIS_KEY_PREFIX_EQP_REVERSE_IGNORE_SIM + simId);
            if (Boolean.TRUE.equals(exists)) {
                logger.warn(">>>>> simId is ignored: {}", simId);
                return new MesR<String>().setCode(ResponseCode.SUCCESS.getCode()).setMsg("Equipment reverse ignored").setData(null);
            } else {
                return joinPoint.proceed();
            }
        } catch (ConstraintViolationException ex) {
            Set<?> violations = ex.getConstraintViolations();
            String message = "Validation failed";
            if (!violations.isEmpty()) {
                ConstraintViolation<?> violation = (ConstraintViolation<?>) violations.iterator().next();
                message = violation.getMessage();
            }
            logger.warn("ConstraintViolationException caught in aspect: {}", message, ex);
            return new MesR<String>().setCode(ResponseCode.SUCCESS.getCode()).setMsg(message).setData(null);
        } catch (Throwable t) {
            logger.error("Unexpected error occurred in aspect", t);
            return new MesR<String>().setCode(ResponseCode.SUCCESS.getCode()).setMsg("Internal server error").setData(null);
        }
    }
}
