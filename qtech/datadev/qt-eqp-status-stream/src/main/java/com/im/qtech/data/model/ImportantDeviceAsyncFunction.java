package com.im.qtech.data.model;

import com.im.qtech.data.async.BaseAsyncFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.im.qtech.data.util.DeviceStatusHandler.addLastUpdated;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/07/16 14:59:38
 */

public class ImportantDeviceAsyncFunction extends BaseAsyncFunction<EqNetworkStatus, EqNetworkStatus> {
    private static final Logger logger = LoggerFactory.getLogger(ImportantDeviceAsyncFunction.class);

    public ImportantDeviceAsyncFunction() {
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
     */
    @Override
    protected EqNetworkStatus asyncInvokeInternal(EqNetworkStatus input) {
        try {
            // 这里可以做 DB/HTTP 等外部异步请求
            addLastUpdated(input);
            logger.debug("处理设备 {} 完成，更新时间: {}", input.getDeviceId(), input.getLastUpdated());
            return input;
        } catch (Exception e) {
            logger.error("处理设备 {} 时发生错误: {}", input.getDeviceId(), e.getMessage(), e);
            // 发生错误时仍返回原始数据
            return input;
        }
    }

    @Override
    protected Class<EqNetworkStatus> getOutputClass() {
        return EqNetworkStatus.class;
    }
}