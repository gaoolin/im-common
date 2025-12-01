package com.im.qtech.data.model;

import com.im.qtech.data.async.BaseAsyncFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.im.qtech.data.util.DeviceStatusHandler.addLastUpdated;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/01 13:20:05
 */
public class NormalDeviceAsyncFunction extends BaseAsyncFunction<EqNetworkStatus, EqNetworkStatus> {

    private static final Logger logger = LoggerFactory.getLogger(NormalDeviceAsyncFunction.class);

    public NormalDeviceAsyncFunction() {
        super(30000L);
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }

    /**
     * 具体异步处理实现，子类必须实现
     *
     * @param input 输入
     * @return 结果
     * @throws Exception 异常
     */
    @Override
    protected EqNetworkStatus asyncInvokeInternal(EqNetworkStatus input) throws Exception {
        try {
            // 实际的异步处理逻辑
            // 例如：数据库操作、外部API调用等
            addLastUpdated(input);
            logger.debug("处理设备 {} 完成，更新时间: {}", input.getDeviceId(), input.getLastUpdated());
            return input;
        } catch (Exception e) {
            logger.error("处理设备 {} 时发生错误: {}", input.getDeviceId(), e.getMessage(), e);
            // 发生错误时仍返回原始数据以避免丢失
            return input;
        }
    }

    @Override
    protected Class<EqNetworkStatus> getOutputClass() {
        return EqNetworkStatus.class;
    }
}
