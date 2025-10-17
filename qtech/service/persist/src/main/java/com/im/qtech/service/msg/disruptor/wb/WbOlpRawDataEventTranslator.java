package com.im.qtech.service.msg.disruptor.wb;


import com.im.qtech.common.dto.param.WbOlpRawData;
import com.lmax.disruptor.EventTranslatorOneArg;

/**
 * 如果使用的是 Disruptor 的 publishEvent 带有转换器的 API，还可以定义
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/24 10:14:53
 */
public class WbOlpRawDataEventTranslator implements EventTranslatorOneArg<WbOlpRawDataEvent, WbOlpRawData> {
    @Override
    public void translateTo(WbOlpRawDataEvent event, long sequence, WbOlpRawData data) {
        event.setData(data);
    }
}