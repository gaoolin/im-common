package com.im.qtech.service.msg.disruptor.eq;

import com.lmax.disruptor.EventFactory;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/29 11:18:58
 */


public class EqpNetworkStatusEventFactory implements EventFactory<EqpNetworkStatusEvent> {
    /**
     * @return
     */
    @Override
    public EqpNetworkStatusEvent newInstance() {
        return new EqpNetworkStatusEvent();
    }
}
