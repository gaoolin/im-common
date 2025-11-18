package com.im.inspection.entity.database;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/01/03 14:33:16
 */

@Data
public class ImSparkJobInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String jobName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")  // 将LocalDateTime对象格式化为字符串
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 用于指定从表单或API接收日期时间参数时的格式
    private LocalDateTime jobDt;
    private Integer statusCode;
    private String remark;
}