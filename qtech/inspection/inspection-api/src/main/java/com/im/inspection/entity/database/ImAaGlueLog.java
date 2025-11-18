package com.im.inspection.entity.database;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/02/08 15:13:20
 */

@Data
public class ImAaGlueLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")  // 将LocalDateTime对象格式化为字符串
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 用于指定从表单或API接收日期时间参数时的格式
    private Instant logDate;
    @Size(max = 100, message = "设备标识长度不能超过100个字符")
    private String deviceId;
    private String message;
    @NotNull(message = "日志级别不能为空")
    private Integer logLevel;
}
