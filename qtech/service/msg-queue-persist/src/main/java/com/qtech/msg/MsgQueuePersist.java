package com.qtech.msg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * DataSourceAutoConfiguration —— Spring Boot 自带的数据源自动配置
 * <p>
 * DruidDataSourceAutoConfigure —— druid 的自动配置
 * <p>
 * 项目已经有：
 * <p>
 * 自定义数据源
 * <p>
 * 自定义动态路由
 * <p>
 * 自定义 DruidDataSourceBuilder
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/04/10 14:42:47
 */

@SpringBootApplication
@Import({com.im.qtech.data.dto.reverse.EqpReversePOJO.class, com.im.qtech.data.dto.param.EqLstPOJO.class, com.im.qtech.data.dto.param.WbOlpRawData.class})
@ComponentScan(basePackages = {"com.qtech.msg", "com.qtech.msg.config.rabbit"})
public class MsgQueuePersist {
    public static void main(String[] args) {
        SpringApplication.run(MsgQueuePersist.class, args);
        System.out.println(">>>>> MsgQueuePersist started successfully.");
    }
}
