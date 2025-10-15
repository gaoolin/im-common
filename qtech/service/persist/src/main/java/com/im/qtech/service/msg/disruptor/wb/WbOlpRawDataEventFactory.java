package com.im.qtech.service.msg.disruptor.wb;

import com.lmax.disruptor.EventFactory;

/**
 * Disruptor 事件工厂：用于创建事件实例。
 */
public class WbOlpRawDataEventFactory implements EventFactory<WbOlpRawDataEvent> {

    @Override
    public WbOlpRawDataEvent newInstance() {
        return new WbOlpRawDataEvent();
    }
}