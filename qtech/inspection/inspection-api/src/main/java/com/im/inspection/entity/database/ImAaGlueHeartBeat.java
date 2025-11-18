package com.im.inspection.entity.database;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/02/08 15:17:40
 */

@Data
public class ImAaGlueHeartBeat implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 心跳记录的唯一标识ID
     */
    private Long id;

    /**
     * 设备的唯一标识符
     */
    @NotNull(message = "设备标识不能为空")
    @Size(max = 100, message = "设备标识长度不能超过100个字符")
    private String deviceId;

    /**
     * 心跳记录的时间戳
     */
    @NotNull(message = "时间戳不能为空")
    private Instant timestamp;

    /**
     * 心跳状态码
     */
    @NotNull(message = "状态码不能为空")
    private Integer statusCode;

    /**
     * 心跳状态的描述信息
     */
    @Size(max = 255, message = "描述长度不能超过255个字符")
    private String description;
}
