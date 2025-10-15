package com.im.aa.inspection.parser.item;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VCM Hall参数解析器
 * <p>
 * ITEM	25	COMMAND		VCM_check
 * ITEM	25	COMPARE_MAX
 * ITEM	25	COMPARE_MIN
 * ITEM	25	CONNECTION_OPTION		1
 * ITEM	25	SEPERATE_STATION_MAX_MIN_SETTING		0
 * ITEM	25	RESULT		Result	Check	100.00	1.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare	0.00	0.00	0.00
 * ITEM	25	RESULT		Test	Check	100.00	1.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare	0.00	0.00	0.00
 * ITEM	25	RESULT		HallX	Check	524.00	500.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare	0.00	0.00	0.00
 * ITEM	25	RESULT		HallY	Check	524.00	500.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare	0.00	0.00	0.00
 * ITEM	25	RESULT		HallAF	Check	120.00	100.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare	0.00	0.00	0.00
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/03/27 08:49:32
 */
public final class ItemVcmHallParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemVcmHallParser.class);

    public static EqLstCommand apply(String[] parts, String prefixCmd) {
        if (parts.length < 7) {
            return null;
        }

        String command = parts[3];
        try {
            if (StringUtils.equalsIgnoreCase("HallAF", command)) {
                Integer num = Integer.parseInt(parts[1]);
                String max = parts[5];
                String min = parts[6];
                BoundsLoader boundsLoader = BoundsLoader.of(min, max);
                logger.debug(">>>>> {}-ItemVcmHallParser: {}, Max: {}, Min: {}", prefixCmd, command, max, min);

                EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);
                eqLstCommand.setParentCommand(prefixCmd);
                eqLstCommand.setCurrentCommand(command);
                eqLstCommand.setSequenceNumber(num);

                return eqLstCommand;
            } else if (StringUtils.equalsIgnoreCase("HallX", command)) {
                Integer num = Integer.parseInt(parts[1]);
                String max = parts[5];
                String min = parts[6];
                BoundsLoader boundsLoader = BoundsLoader.of(min, max);
                logger.debug(">>>>> {}-ItemVcmHallParser: {}, Max: {}, Min: {}", prefixCmd, command, max, min);

                EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);
                eqLstCommand.setParentCommand(prefixCmd);
                eqLstCommand.setCurrentCommand(command);
                eqLstCommand.setSequenceNumber(num);

                return eqLstCommand;
            } else if (StringUtils.equalsIgnoreCase("HallY", command)) {
                Integer num = Integer.parseInt(parts[1]);
                String max = parts[5];
                String min = parts[6];
                BoundsLoader boundsLoader = BoundsLoader.of(min, max);
                logger.debug(">>>>> {}-ItemVcmHallParser: {}, Max: {}, Min: {}", prefixCmd, command, max, min);

                EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);
                eqLstCommand.setParentCommand(prefixCmd);
                eqLstCommand.setCurrentCommand(command);
                eqLstCommand.setSequenceNumber(num);

                return eqLstCommand;
            } else {
                logger.info(">>>>> {}-ItemVcmHallParser: {}", prefixCmd, command);
                return null;
            }
        } catch (NumberFormatException e) {
            logger.error(">>>>> {}-ItemVcmHallParser: Invalid number format inspection parts: {}", prefixCmd, parts[1], e);
            return null;
        }
    }
}
