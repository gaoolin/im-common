package com.im.aa.inspection.entity.reverse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/06/21 08:30:15
 */

@Data
@EqualsAndHashCode
@ToString
public class EqReverseCtrlInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String simId;
    private String source;
    private String prodType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime chkDt;

    private Integer code;
    private String description;

    @JsonIgnore // 指示Jackson序列化器忽略此字段
    transient private int version;

    @JsonIgnore
    public String getFormattedChkDt() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+8"));
        return sdf.format(chkDt);
    }
}
