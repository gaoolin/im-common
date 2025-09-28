package com.im.aa.inspection.parser.item;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XY偏移量参数解析器
 * <p>
 * 处理形如以下格式的字符
 * ITEM	24	RESULT		X_Offset	Check	90.00	-20.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare		0.00	0.00
 * ITEM	24	RESULT		Y_Offset	Check	60.00	-80.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare		0.00	0.00
 * <p>
 * 用到此解析器的List 命令包括：
 * Save_OC, Save_MTF, OC_Check
 * <p>
 * 数据库中对应字段：
 * xx_x_offset_min, xx_x_offset_max, xx_y_offset_min, xx_y_offset_max
 * <p>
 * 实例中的属性：
 * xxXOffsetMax, xxXOffsetMin
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/10/08 11:00:04
 */
public final class ItemXyOffsetParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemXyOffsetParser.class);

    public static EqLstCommand apply(String[] parts, String prefixCmd) {
        if (parts.length < 4) {
            return null;
        }

        String command = parts[3];
        try {
            if (StringUtils.equalsIgnoreCase("X_Offset", command)) {
                Integer num = Integer.parseInt(parts[1]);
                String max = parts[5];
                String min = parts[6];
                BoundsLoader boundsLoader = BoundsLoader.of(min, max);
                logger.debug(">>>>> {}-ItemXyOffsetParser: {}, Max: {}, Min: {}", prefixCmd, command, max, min);

                EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);
                eqLstCommand.setParentCommand(prefixCmd);
                eqLstCommand.setCurrentCommand(command);
                eqLstCommand.setSequenceNumber(num);

                return eqLstCommand;
            } else if (StringUtils.equalsIgnoreCase("Y_Offset", command)) {
                Integer num = Integer.parseInt(parts[1]);
                String max = parts[5];
                String min = parts[6];
                BoundsLoader boundsLoader = BoundsLoader.of(min, max);
                logger.debug(">>>>> {}-ItemXyOffsetParser: {}, Max: {}, Min: {}", prefixCmd, command, max, min);

                EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);
                eqLstCommand.setParentCommand(prefixCmd);
                eqLstCommand.setCurrentCommand(command);
                eqLstCommand.setSequenceNumber(num);

                return eqLstCommand;
            }
            return null;
        } catch (NumberFormatException e) {
            logger.error(">>>>> {}-ItemXyOffsetParser: Invalid number format inspection parts: {}", prefixCmd, parts[1], e);
            return null;
        }
    }
}
