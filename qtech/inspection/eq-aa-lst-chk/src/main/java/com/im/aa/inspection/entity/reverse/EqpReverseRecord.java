package com.im.aa.inspection.entity.reverse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.im.common.dt.Chronos;

import javax.persistence.*;
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
@Entity
@Table(name = "eqp_reverse_control_latest", schema = "biz") // 需要指定实际表名
public class EqpReverseRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String simId;
    private String source;
    private String module;

    @Column(name = "chk_dt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime chkDt;

    private Integer code;
    private Boolean passed;

    @Enumerated(EnumType.STRING)
    private LabelEum label;
    private String reason;
    private String description;

    @JsonIgnore
    @Transient
    private Long version;

    @JsonIgnore
    @Transient
    public String getFormattedChkDt() {
        if (chkDt == null) {
            return "";
        }
        return Chronos.format(chkDt, "MM-dd HH:mm:ss");
    }
}
