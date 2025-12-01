package com.im.qtech.data.util;

import com.im.qtech.data.model.EqNetworkStatus;

import java.util.Date;

/**
 * 工具类（无状态）
 *
 * @author :  gaozhilin
 * @email :  gaoolin@gmail.com
 * @date :  2025/08/01 13:23:12
 */
public class DeviceStatusHandler {
    public static EqNetworkStatus addLastUpdated(EqNetworkStatus record) {
        record.setLastUpdated(new Date());
        return record;
    }
}
