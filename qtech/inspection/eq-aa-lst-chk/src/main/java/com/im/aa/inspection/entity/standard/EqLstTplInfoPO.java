package com.im.aa.inspection.entity.standard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.im.qtech.common.dto.standard.EqLstTplInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/07/22 14:20:43
 */
@Data
@Entity
@Table(name = "imbiz.eq_lst_tpl_info")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class EqLstTplInfoPO extends EqLstTplInfo {
    private static final long serialVersionUID = 2L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 通过 module 作为 OneToOne 外键
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "module",                // 当前表字段
            referencedColumnName = "module" // 主表字段
    )
    private EqLstTplDO tplDO;

    @JsonIgnore
    private String provider;
    @JsonIgnore
    private String belongTo;
    @JsonIgnore
    private String createBy;
    @JsonIgnore
    private String createTime;
    @JsonIgnore
    private String updateBy;
    @JsonIgnore
    private String updateTime;
    @JsonIgnore
    private String remark;
}


