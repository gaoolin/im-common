package com.im.qtech.service.msg.disruptor.wb;

import com.im.qtech.service.msg.entity.EqpReverseInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/24 15:29:05
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WbOlpChkEvent {
    /**
     * 实际业务数据
     */
    private EqpReverseInfo data;

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
