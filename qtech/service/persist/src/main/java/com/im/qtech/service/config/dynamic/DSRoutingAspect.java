package com.im.qtech.service.config.dynamic;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据源切面
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/15
 */

@Aspect
@Component
@Order(1)
public class DSRoutingAspect {

    @Pointcut("@annotation(com.im.qtech.service.config.dynamic.DS) || @within(com.im.qtech.service.config.dynamic.DS)")
    public void dsPointCut() {
    }

    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        DS annotation = method.getAnnotation(DS.class);
        if (annotation == null) {
            annotation = point.getTarget().getClass().getAnnotation(DS.class);
        }

        if (annotation != null) DSContextHolder.set(annotation.value());

        try {
            return point.proceed();
        } finally {
            if (annotation != null) DSContextHolder.clear();
        }
    }
}