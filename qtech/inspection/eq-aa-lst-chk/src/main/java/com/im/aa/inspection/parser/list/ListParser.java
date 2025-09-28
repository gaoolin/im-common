package com.im.aa.inspection.parser.list;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/15
 */
public final class ListParser {
    private static final Logger logger = LoggerFactory.getLogger(ListParser.class);

    public static EqLstCommand apply(String[] parts, String parentCommand) {
        try {
            String command = parts[2];
            String enable = parts[parts.length - 1];
            logger.debug(">>>>> ListHandler: Command: {}, status: {}", command, enable);
            BoundsLoader boundsLoader = BoundsLoader.single(enable);
            EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);
            eqLstCommand.setCurrentCommand(command);
            return eqLstCommand;
        } catch (Exception e) {
            logger.error(">>>>> ListHandler: Error: {}", e.getMessage());
            return null;
        }
    }
}
