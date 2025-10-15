package com.im.qtech.service.msg.disruptor.eq;

import com.im.qtech.service.config.thread.TaskDispatcher;
import com.im.qtech.service.msg.entity.EqpOnlineStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/29 11:03:04
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EqpOnlineStatusEvent {
    private EqpOnlineStatus data;
    private String raw;
    private TaskDispatcher.TaskPriority priority;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("data", data)
                .append("raw", raw)
                .append("priority", priority)
                .toString();
    }

    public void clear() {
        this.data = null;
        this.raw = null;
        this.priority = null;
    }
}
