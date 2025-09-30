package com.im.aa.inspection.entity.reverse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.im.common.dt.Chronos;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/06/21 08:30:15
 */

@Data
@EqualsAndHashCode
@ToString
public class EqpReverseRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String simId;
    private String source;
    private String module;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime chkDt;

    private Integer code;
    private Boolean passed;
    private String reason;
    private String description;

    @JsonIgnore // 指示Jackson序列化器忽略此字段
    transient private Long version;

    @JsonIgnore
    public String getFormattedChkDt() {
        if (chkDt == null) {
            return "";
        }
        // 使用自研框架的 chronos 实现时间格式化
        return Chronos.format(chkDt, "MM-dd HH:mm:ss");
    }
}
