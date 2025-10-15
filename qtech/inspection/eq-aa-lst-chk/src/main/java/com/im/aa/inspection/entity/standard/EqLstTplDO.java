package com.im.aa.inspection.entity.standard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.im.qtech.common.dto.param.EqLstPOJO;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/21 08:45:18
 */
@Data
@Entity
@Table(name = "eqp_aa_lst_tpl", schema = "biz")
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(exclude = {"tplInfo"})
public class EqLstTplDO extends EqLstPOJO {

    private static final long serialVersionUID = 2L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 一对一关系映射 - 添加 insertable=false, updatable=false
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module", referencedColumnName = "module", insertable = false, updatable = false)
    @JsonIgnore  // 完全忽略此字段的序列化
    private EqLstTplInfoDO tplInfo;

    private boolean deleted = false;
    private Integer version = 0;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
}



