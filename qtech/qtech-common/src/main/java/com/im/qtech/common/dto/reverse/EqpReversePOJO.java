package com.im.qtech.common.dto.reverse;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备反控POJO
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/13
 */
@Data
public class EqpReversePOJO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String simId;
    private String source;
    private String module;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime chkDt;
    private Integer code;
    private Boolean passed;
    @Enumerated(EnumType.STRING)
    private LabelEum label;
    private String reason;
    private String description;
}

