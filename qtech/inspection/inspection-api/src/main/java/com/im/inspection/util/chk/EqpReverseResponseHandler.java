package com.im.inspection.util.chk;

import com.im.inspection.util.response.MesR;
import com.im.inspection.util.response.ResponseCode;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.im.inspection.util.chk.IoTReverseMsgBuilder.buildResponseMessage;

/**
 * 设备反向控制响应处理器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 */
public class EqpReverseResponseHandler {

    private static final String NO_DATA_MESSAGE = "No data found";
    private static final String ALWAYS_NULL_MESSAGE = "Control Model: ALWAYS_NULL, always return Ok.";
    private static final String DEFAULT_NOT_WORKING_TIME_MESSAGE = "Control Model: DEFAULT. not in working time, return Ok.";
    // 添加新的常量
    private static final String OUTDATED_DATA_MESSAGE = "反控超过一天未更新";

    /**
     * 检查数据是否过期（超过一天）
     *
     * @param pojo 设备反向控制信息对象
     * @return 如果数据有效返回true，否则返回false
     */
    public static boolean isDataUpToDate(EqpReversePOJO pojo) {
        if (pojo == null || pojo.getChkDt() == null) {
            return false;
        }

        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return !pojo.getChkDt().isBefore(oneDayAgo);
    }

    /**
     * 处理设备反向控制信息的响应
     *
     * @param pojo 设备反向控制POJO对象
     * @return 响应结果
     */
    public static MesR<String> handleResponse(EqpReversePOJO pojo) {
        // 首先检查数据是否过期
        if (!isDataUpToDate(pojo)) {
            return createOutdatedDataResponse();
        }

        return handleEqpSource(pojo);
    }

    /**
     * 创建数据过期的响应
     *
     * @return 数据过期响应对象
     */
    public static MesR<String> createOutdatedDataResponse() {
        return new MesR<String>()
                .setCode(ResponseCode.SUCCESS.getCode()) // code 200
                .setMsg(OUTDATED_DATA_MESSAGE)
                .setData(null);
    }

    /**
     * 处理 aa-list 数据源的响应逻辑
     *
     * @param pojo 设备反向控制信息对象
     * @return 处理后的响应对象
     */
    public static MesR<String> handleEqpSource(EqpReversePOJO pojo) {
        ControlMode mode = CtrlModeFlag.getControlMode(pojo.getSource());

        switch (mode) {
            case ALWAYS_NULL:
                return createSuccessResponse(ALWAYS_NULL_MESSAGE);
            case DEFAULT:
                return handleDefaultMode(pojo);
            case ALWAYS_RETURN:
            default:
                return convertToResponse(pojo);
        }
    }

    /**
     * 处理默认控制模式
     *
     * @param pojo 设备反向控制信息对象
     * @return 处理后的响应对象
     */
    public static MesR<String> handleDefaultMode(EqpReversePOJO pojo) {
        LocalDateTime now = LocalDateTime.now();
        if (isWithinWorkingHours(now)) {
            return convertToResponse(pojo);
        } else {
            return createSuccessResponse(DEFAULT_NOT_WORKING_TIME_MESSAGE);
        }
    }

    /**
     * 将设备反向控制对象转换为统一响应格式
     *
     * @param pojo 设备反向控制对象
     * @return 统一响应对象
     */
    public static MesR<String> convertToResponse(EqpReversePOJO pojo) {
        if (pojo == null) {
            return createSuccessResponse(NO_DATA_MESSAGE);
        }

        int code = pojo.getCode();
        String message = buildResponseMessage(pojo);

        if (code == 0) {
            return createSuccessResponse(message);
        } else {
            return createFoundResponse(message);
        }
    }

    /**
     * 创建成功状态的响应
     *
     * @param msg 响应消息
     * @return 成功响应对象
     */
    public static MesR<String> createSuccessResponse(String msg) {
        return new MesR<String>()
                .setCode(ResponseCode.SUCCESS.getCode())
                .setMsg(msg)
                .setData(null);
    }

    /**
     * 创建找到数据状态的响应
     *
     * @param msg 响应消息
     * @return 找到数据响应对象
     */
    public static MesR<String> createFoundResponse(String msg) {
        return new MesR<String>()
                .setCode(ResponseCode.FOUND.getCode())
                .setMsg(msg)
                .setData(null);
    }

    /**
     * 判断当前时间是否在工作时间内
     * 工作时间为周一至周五 8:30-17:00
     *
     * @param dateTime 当前日期时间
     * @return 是否在工作时间内
     */
    public static boolean isWithinWorkingHours(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.toLocalDate().getDayOfWeek();
        LocalTime timeNow = dateTime.toLocalTime();

        boolean isWeekday = dayOfWeek.getValue() >= DayOfWeek.MONDAY.getValue()
                && dayOfWeek.getValue() <= DayOfWeek.FRIDAY.getValue();
        boolean isWorkingHour = timeNow.isAfter(LocalTime.of(8, 30))
                && timeNow.isBefore(LocalTime.of(17, 0));

        return isWeekday && isWorkingHour;
    }
}
