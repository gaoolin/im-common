package com.qtech.msg.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/24 15:37:01
 */


public class WbOlpChkEventFactory implements EventFactory<WbOlpChkEvent> {
    /**
     * @return
     */
    @Override
    public WbOlpChkEvent newInstance() {
        return new WbOlpChkEvent();
    }
}
