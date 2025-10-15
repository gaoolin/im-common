package com.im.aa.inspection.parser.item;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XY分辨率参数解析器
 * <p>
 * 处理形如以下格式的字符
 * ITEM	7	X_RES		5.00	-5.00	1
 * ITEM	7	Y_RES		5.00	-5.00	1
 * <p>
 * 用到此解析器的List 命令包括：
 * AaHandler, ChartAlignment1, ChartAlignment2
 * 以下List中包含次参数，但未要求解析和管控
 * AA1, AA2, AA3,
 * <p>
 * 数据库中对应字段：
 * chart_alignment_x_res_min, chart_alignment_x_res_max
 * <p>
 * 实例中的属性：
 * chartAlignmentXResMin, chartAlignmentXResMax
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/10/08 10:48:37
 */
public final class ItemXyResParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemXyResParser.class);

    public static EqLstCommand apply(String[] parts, String parentCommand) {
        String command = StringUtils.upperCase(parts[2]);
        if ("X_RES".equals(command) || "Y_RES".equals(command)) {
            try {
                Integer num = Integer.parseInt(parts[1]);
                String max = parts[3];
                String min = parts[4];
                BoundsLoader chartAlignmentBoundsLoader = BoundsLoader.of(min, max);
                logger.debug(">>>>> {}-ItemXyResParser: Command: {}, min: {}, max: {}", parentCommand, command, min, max);

                EqLstCommand eqLstCommand = EqLstCommand.of(chartAlignmentBoundsLoader);
                eqLstCommand.setParentCommand(parentCommand);     // 父命令存储前缀命令
                eqLstCommand.setCurrentCommand(command);          // 当前命令存储命令类型
                eqLstCommand.setSequenceNumber(num);              // 序号存储命令序号

                return eqLstCommand;
            } catch (NumberFormatException e) {
                logger.error(">>>>> {}-ItemXyResParser: Invalid number format inspection parts: {}", parentCommand, parts[1], e);
                return null;
            }
        }
        return null;
    }
}
