package com.im.aa.inspection.entity.standard;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.im.qtech.data.dto.standard.EqLstTplInfoPOJO;
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
 * @date 2024/07/22 14:20:43
 */
@Data
@Entity
@Table(name = "EQP_AA_LST_TPL_INFO", schema = "IMBIZ")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@ToString(exclude = {"tpl"})
public class EqLstTplInfoDO extends EqLstTplInfoPOJO {

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
    @JoinColumn(name = "moduleId", referencedColumnName = "moduleId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"tplInfo", "hibernateLazyInitializer", "handler"}) // 忽略反向引用避免循环
    private EqLstTplDO tpl;
    private String provider;
    @Column(name = "belong_to")
    private String belongTo;
    @Column(name = "create_by")
    private String createBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time")
    private LocalDateTime createTime;
    @Column(name = "update_by")
    private String updateBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    private String remark;

    public EqLstTplInfoDO(Long id, String moduleId, Long listParams, Long itemParams, EqLstTplDO tpl, Integer status, LocalDateTime createTime, LocalDateTime updateTime) {
        super.setModuleId(moduleId);
        super.setListParams(listParams);
        super.setItemParams(itemParams);
        super.setStatus(status);
        this.setCreateTime(createTime);
        this.setUpdateTime(updateTime);
        this.id = id;
        this.tpl = tpl;
    }
}




