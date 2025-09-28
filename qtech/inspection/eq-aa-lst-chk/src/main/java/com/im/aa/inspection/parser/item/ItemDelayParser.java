package com.im.aa.inspection.parser.item;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Delay命令解析器
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/10/08 10:55:55
 */
public final class ItemDelayParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemDelayParser.class);

    public static EqLstCommand apply(String[] parts, String parentCommand) {
        String command = parts[2];
        if (StringUtils.equalsIgnoreCase("Delay", command)) {
            try {
                int num = Integer.parseInt(parts[1]);
                String val = parts[3];
                logger.debug(">>>>> {}-ItemDelayParser: Delay: {}", parentCommand, val);

                BoundsLoader boundsLoader = BoundsLoader.single(val);
                EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);

                eqLstCommand.setParentCommand(parentCommand);
                eqLstCommand.setCurrentCommand(command);
                eqLstCommand.setSequenceNumber(num);

                return eqLstCommand;
            } catch (NumberFormatException e) {
                logger.error(">>>>> {}-ItemDelayParser: Invalid number format inspection parts[1]: {}", parentCommand, parts[1], e);
                return null;
            }
        }
        // logger.error(">>>>> ItemDelayParser: Unsupported command: {}", command);
        return null;
    }
}
