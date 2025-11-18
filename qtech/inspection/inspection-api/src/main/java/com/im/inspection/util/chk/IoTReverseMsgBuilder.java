package com.im.inspection.util.chk;

import com.im.qtech.data.dto.reverse.EqpReversePOJO;

import java.time.format.DateTimeFormatter;

import static com.im.qtech.data.constant.QtechImBizConstant.EQ_REVERSE_CTRL_INFO_RESPONSE_MSG_LENGTH;

/**
 * 反控机台信息显示内容
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/14 10:32:45
 */
public class IoTReverseMsgBuilder {
    private static final String PREFIX_WB_OLP = "ECheck";
    private static final String PREFIX_AA_LIST = "Parameter Monitoring(LIST)";
    private static final String NG_MSG = " NG";
    private static final String SEPARATOR = ": ";

    /**
     * 构建响应消息。
     *
     * @param pojo EqReverseInfo对象
     * @return 合成的消息
     */
    public static String buildResponseMessage(EqpReversePOJO pojo) {
        if (pojo == null) {
            return "unknown check source.";
        }

        String description = truncateDescription(pojo.getDescription());
        String formattedTime = formatChkDt(pojo);
        String prefix = getPrefixBySource(pojo.getSource());
        String suffix = pojo.getCode() == 0 ? "" : NG_MSG;

        if (prefix == null) {
            return "unknown check source.";
        }

        return prefix + suffix + SEPARATOR + formattedTime + " " + description;
    }

    private static String getPrefixBySource(String source) {
        if ("wb-olp".equals(source)) {
            return PREFIX_WB_OLP;
        } else if ("aa-list".equals(source)) {
            return PREFIX_AA_LIST;
        } else {
            return null;
        }
    }

    private static String formatChkDt(EqpReversePOJO pojo) {
        if (pojo.getChkDt() == null) {
            return "";
        }
        return pojo.getChkDt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * 限制description字段的内容长度。
     *
     * @param description 原始描述
     * @return 限制长度后的描述
     */
    private static String truncateDescription(String description) {
        if (description == null || description.isEmpty()) {
            return "";
        }
        if (description.length() <= EQ_REVERSE_CTRL_INFO_RESPONSE_MSG_LENGTH) {
            return description;
        }
        return description.substring(0, EQ_REVERSE_CTRL_INFO_RESPONSE_MSG_LENGTH) + "...";
    }
}
