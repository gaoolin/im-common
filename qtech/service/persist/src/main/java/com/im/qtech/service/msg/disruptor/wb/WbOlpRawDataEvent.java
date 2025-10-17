package com.im.qtech.service.msg.disruptor.wb;

import com.im.qtech.common.dto.param.WbOlpRawData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Disruptor 中的事件对象，封装原始采集数据。
 * 实现 Disruptor 的事件处理器，进行批量数据持久化
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/24 10:07:54
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WbOlpRawDataEvent {

    /**
     * 实际业务数据
     */
    private WbOlpRawData data;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("data", data)
                .toString();
    }

    /**
     * 清除引用，便于 GC。
     */
    public void clear() {
        this.data = null;
    }
}