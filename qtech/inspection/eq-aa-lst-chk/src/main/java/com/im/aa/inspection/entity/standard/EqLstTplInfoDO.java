package com.im.aa.inspection.entity.standard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.im.qtech.common.dto.standard.EqLstTplInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 检查结果模板信息实体
 * 修正版：取消继承，父类字段直接放入本类，添加@Column明确映射
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/07/22 14:20:43
 */

@Data
@Entity
@Table(name = "eqp_aa_lst_tpl_info", schema = "biz")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@ToString(exclude = {"tpl"})
public class EqLstTplInfoDO extends EqLstTplInfo {

    private static final long serialVersionUID = 2L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 一对一关系，支持级联删除
     * 需要显式告诉 Hibernate 关联字段是 module
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "module", referencedColumnName = "module", insertable = false, updatable = false)
    private EqLstTplDO tpl;

    private String provider;

    @Column(name = "belong_to")
    private String belongTo;

    @Column(name = "create_by")
    private String createBy;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_by")
    private String updateBy;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    private String remark;
}




