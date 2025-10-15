package com.im.aa.inspection.parser.item;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RecordPosition命令解析器
 * 处理形如以下格式的字符
 * ITEM	14	GetPositionWithFinalOffset
 * <p>
 * 数据库中对应字段：
 * record_position_name
 * <p>
 * 实例中的属性：
 * recordPositionName
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/01/15 13:19:27
 */
public final class ItemRecordPositionParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemRecordPositionParser.class);

    public static EqLstCommand apply(String[] parts, String parentCommand) {
        try {
            int num = Integer.parseInt(parts[1]);
            String val = parts[2];
            logger.debug(">>>>> {}-ItemRecordPositionParser: {}", parentCommand, val);

            BoundsLoader boundsLoader = BoundsLoader.single(val);
            EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);

            // EqLstCommand.setParentCommand(parentCommand);
            eqLstCommand.setCurrentCommand("recordPositionName");
            eqLstCommand.setSequenceNumber(num);

            return eqLstCommand;
        } catch (NumberFormatException e) {
            logger.error(">>>>> {}-ItemRecordPositionParser: Invalid number format inspection parts[1]: {}", parentCommand, parts[1], e);
            return null;
        }
    }
}
