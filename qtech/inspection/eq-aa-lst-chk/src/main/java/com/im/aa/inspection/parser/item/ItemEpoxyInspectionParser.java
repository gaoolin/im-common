package com.im.aa.inspection.parser.item;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * EpoxyInspection命令解析器
 * 处理形如以下格式的字符
 * ITEM	17	EpoxyInspection  30
 * <p>
 * 数据库中字段：
 * epoxy_inspection_interval
 * <p>
 * 实例中属性：
 * epoxyInspectionInterval
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/10/08 10:52:49
 */
public final class ItemEpoxyInspectionParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemEpoxyInspectionParser.class);

    public static EqLstCommand apply(String[] parts, String parentCommand) {
        String command = parts[2];
        if (StringUtils.equalsIgnoreCase("EpoxyInspection", command)) {
            try {
                Integer num = Integer.parseInt(parts[1]);
                String val = parts[3];
                logger.debug(">>>>> {}-ItemEpoxyInspectionParser: command: {}, val: {}", parentCommand, command, val);

                BoundsLoader boundsLoader = BoundsLoader.single(val);
                EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);

                // eqLstCommand.setParentCommand(parentCommand);     // 父命令存储前缀命令
                eqLstCommand.setCurrentCommand("EpoxyInspection"); // 当前命令存储命令类型
                eqLstCommand.setSubCommand("Interval");           // 子命令存储子系统
                eqLstCommand.setSequenceNumber(num);              // 序号存储命令序号

                return eqLstCommand;
            } catch (NumberFormatException e) {
                logger.error(">>>>> {}-ItemEpoxyInspectionParser: Invalid number format inspection parts[1]: {}", parentCommand, parts[1], e);
                return null;
            }
        }
        return null;
    }
}