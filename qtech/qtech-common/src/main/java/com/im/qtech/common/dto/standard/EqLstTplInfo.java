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
    private Long listParams;
    private Long itemParams;
    private Integer status;

    /**
     * 重写equals和hashCode方法，用于判断对象的对应属性是否相等
     * 重写 equals/hashCode，使用业务字段，而非数据库主键
     * 注意：如果你想用在 Set / Map 做去重，业务字段足够，避免使用懒加载对象 template
     */
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