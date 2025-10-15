package com.im.qtech.service.msg.disruptor.eq;

import com.lmax.disruptor.EventFactory;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/29 11:18:58
 */


public class EqpOnlineStatusEventFactory implements EventFactory<EqpOnlineStatusEvent> {
    /**
     * @return
     */
    @Override
    public EqpOnlineStatusEvent newInstance() {
        return new EqpOnlineStatusEvent();
    }
}
