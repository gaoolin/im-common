package com.im.aa.inspection.entity.standard;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.im.aa.inspection.entity.param.EqLstSet;
import lombok.Data;
import lombok.ToString;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/05/21 08:45:18
 */

@Data
@ToString(callSuper = true)
@TableName(value = "IMBIZ.IM_AA_LIST_PARAMS_STD_MODEL")
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略未知属性
public class EqLstTplDO extends EqLstSet {
    private static final long serialVersionUID = 2L;

    private Long id;

    @Override
    public void reset() {
        this.id = null;
        super.reset();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
