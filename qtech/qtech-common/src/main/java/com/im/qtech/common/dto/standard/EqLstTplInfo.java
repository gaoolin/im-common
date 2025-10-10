package com.im.qtech.common.dto.standard;

import lombok.Data;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/25
 */

@Data
@MappedSuperclass
public class EqLstTplInfo implements Serializable {
    private static final long serialVersionUID = 529L;

    private String module;
    private Integer listParams;
    private Integer itemParams;
    private Integer status;

    // 重写equals和hashCode方法，用于判断对象的对应属性是否相等
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EqLstTplInfo that = (EqLstTplInfo) o;
        return Objects.equals(module, that.module) &&
                Objects.equals(listParams, that.listParams) &&
                Objects.equals(itemParams, that.itemParams) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, listParams, itemParams, status);
    }
}