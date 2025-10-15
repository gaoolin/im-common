package com.im.aa.inspection.entity.reverse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.im.qtech.common.dto.reverse.EqpReversePOJO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.im.common.dt.Chronos;

import javax.persistence.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/06/21 08:30:15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "eqp_reverse_control_latest", schema = "biz") // 需要指定实际表名
public class EqpReverseDO extends EqpReversePOJO {
    private static final long serialVersionUID = 2L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Transient
    private Long version;

    @JsonIgnore
    @Transient
    public String getFormattedChkDt() {
        if (getChkDt() == null) {  // 使用 getter 访问父类属性
            return "";
        }
        return Chronos.format(getChkDt(), "MM-dd HH:mm:ss");
    }
}
