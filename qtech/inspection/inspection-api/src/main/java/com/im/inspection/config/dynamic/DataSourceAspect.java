package com.im.inspection.config.dynamic;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 处理DataSourceSwitch注解，实现动态切换数据源
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/08 10:19:03
 */
@Aspect
@Component
public class DataSourceAspect implements Ordered {

    @Pointcut("@annotation(com.im.inspection.config.dynamic.DataSourceSwitch)")
    public void dataSourcePointCut() {
    }

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();

        DataSourceSwitch dataSourceSwitch = method.getAnnotation(DataSourceSwitch.class);
        if (dataSourceSwitch == null) {
            // Fallback to target class if annotation is not found on method (e.g., for proxy issues)
            dataSourceSwitch = point.getTarget().getClass().getAnnotation(DataSourceSwitch.class);
        }

        if (dataSourceSwitch != null) {
            DynamicDataSource.setDataSource(dataSourceSwitch.name());
        }

        try {
            return point.proceed();
        } finally {
            DynamicDataSource.clearDataSource();
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
