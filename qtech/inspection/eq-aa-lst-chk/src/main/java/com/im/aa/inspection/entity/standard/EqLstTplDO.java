package com.im.aa.inspection.entity.standard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.im.qtech.common.dto.param.EqLstPOJO;
import lombok.Data;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/05/21 08:45:18
 */
@Data
@Entity
@Table(name = "biz.eqp_aa_lst_tpl", schema = "imbiz")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EqLstTplDO extends EqLstPOJO {
    private static final long serialVersionUID = 2L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId                     // ✅ 标识业务主键
    @Column(name = "module", insertable = false, updatable = false)
    private String module;

    private boolean deleted;
    private Integer version;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;

    @OneToOne(mappedBy = "tplDO", fetch = FetchType.LAZY, optional = true)
    private EqLstTplInfoPO tplInfo;

    // ✅ 如果 Lombok 生成的 setter 与父类冲突，手动覆盖
    @Override
    public EqLstTplDO setModule(String module) {
        super.setModule(module);
        return this;
    }
}