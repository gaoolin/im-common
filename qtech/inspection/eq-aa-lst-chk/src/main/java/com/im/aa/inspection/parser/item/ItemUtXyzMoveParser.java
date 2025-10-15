package com.im.aa.inspection.parser.item;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UT：Utility（公用设施）, 在某些行业中，UT 可能指的是 公用设施，例如电力、气体、水等。这通常与设备的运作环境有关，指的是支持设备运行的基础设施。
 * 处理形如以下格式的字符
 * ITEM	14	UTXYZMove #X #Y  -10
 * <p>
 * 用到此解析器的List 命令包括：
 * RecordPosition
 * <p>
 * 数据库中对应字段：
 * record_position_ut_xyz_move
 * <p>
 * 实例中的属性：
 * recordPositionUtXyzMove
 * 上述属性名作废，变更为：
 * utXyzMoveVal
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/10/08 11:02:29
 */
public final class ItemUtXyzMoveParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemUtXyzMoveParser.class);

    public static EqLstCommand apply(String[] parts, String parentCommand) {
        try {
            Integer num = Integer.parseInt(parts[1]);
            String command = parts[2];
            if (StringUtils.equalsIgnoreCase("UTXYZMove", command)) {
                String val = parts[5];
                logger.debug(">>>>> {}-ItemUtXyzMoveParser: UtXyzMove: {}", parentCommand, val);

                BoundsLoader boundsLoader = BoundsLoader.single(val);
                EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);

                eqLstCommand.setParentCommand(parentCommand);
                eqLstCommand.setCurrentCommand("UtXyzMoveVal");
                eqLstCommand.setSequenceNumber(num);

                return eqLstCommand;
            }
            return null;
        } catch (NumberFormatException e) {
            logger.error(">>>>> {}-ItemUtXyzMoveParser: Invalid number format inspection parts[1]: {}", parentCommand, parts[1], e);
            return null;
        }
    }
}
