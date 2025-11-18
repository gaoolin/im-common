package com.im.inspection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Hello world!
 */
// @Import(com.im.qtech.data.dto.reverse.EqpReversePOJO.class)
@SpringBootApplication
@Import(com.im.qtech.data.dto.reverse.EqpReversePOJO.class)
@ComponentScan(basePackages = {"com.im.inspection", "com.im.qtech.data.dto.reverse"})
public class InspectionApi {
    public static void main(String[] args) {
        SpringApplication.run(InspectionApi.class, args);
        System.out.println("Hello World!");
    }
}
