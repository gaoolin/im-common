package com.im.inspection.util.chk;

import com.im.inspection.util.response.MesR;
import com.im.inspection.util.response.ResponseCode;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.im.inspection.util.chk.MesResponseConvertor.doConvert;

/**
 * 控制模式逻辑处理器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/15 13:52:19
 */

public class CtrlModeResponseHandler {
    // 根据 EqReverseInfo 的 source 属性和控制模式，返回不同的响应
    public static MesR<String> handleResponse(EqpReversePOJO pojo) {
        String source = pojo.getSource();

        // 根据不同模块应用不同的控制模式
        ControlMode mode = CtrlModeFlag.getControlMode(source);

        switch (mode) {
            case ALWAYS_NULL:
                return new MesR<String>().setCode(ResponseCode.SUCCESS.getCode())
                        .setMsg("Control Model: ALWAYS_NULL for module " + source + ", always return Ok.")
                        .setData(null);
            case DEFAULT:
                LocalDateTime now = LocalDateTime.now();
                if (isWithinWorkingHours(now)) {
                    return doConvert(pojo);
                } else {
                    return new MesR<String>().setCode(ResponseCode.SUCCESS.getCode())
                            .setMsg("Control Model: DEFAULT for module " + source + ". not in working time, return Ok.")
                            .setData(null);
                }
            case ALWAYS_RETURN:
            default:
                return doConvert(pojo);
        }
    }

    /**
     * 判断当前时间是否在工作时间内。
     *
     * @param dateTime 当前日期时间
     * @return 是否在工作时间内
     */
    private static boolean isWithinWorkingHours(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.toLocalDate().getDayOfWeek();
        LocalTime timeNow = dateTime.toLocalTime();
        return (DayOfWeek.MONDAY.getValue() <= dayOfWeek.getValue() && dayOfWeek.getValue() <= DayOfWeek.FRIDAY.getValue())
                && timeNow.isAfter(LocalTime.of(8, 30)) && timeNow.isBefore(LocalTime.of(17, 0));
    }
}
