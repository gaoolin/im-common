package com.im.aa.inspection.parser.item;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * mtfCheck的光学视场测试
 * <p>
 * 处理形如以下格式的字符
 * ITEM	12	RESULT		[1]	Check	100.00	28.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare		0.00	0.00
 * ITEM	12	RESULT		[2]	Check	100.00	28.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare		0.00	0.00
 * ITEM	12	RESULT		[3]	Check	100.00	28.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare		0.00	0.00
 * ITEM	12	RESULT		[4]	Check	100.00	28.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare		0.00	0.00
 * ITEM	12	RESULT		[5]	Check	100.00	28.00	Log	100.00	100.00	100.00	100.00	1.00	1.00	1.00	1.00	NoCompare		0.00	0.00
 * <p>
 * 用到此解析器的List 命令包括：
 * mtfCheck
 * <p>
 * 数据库中的字段：
 * mtfCheckF
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/11/28 09:40:17
 */
public final class ItemLensFieldOfViewTestParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemLensFieldOfViewTestParser.class);

    // 声明为静态常量，避免重复编译
    // 匹配数字
    private static final Pattern PATTERN = Pattern.compile("\\[(\\d+)\\]");

    public static EqLstCommand apply(String[] parts, String parentCommand) {
        if (parts.length < 7) {
            logger.warn(">>>>> ItemLensFieldOfViewTestParser: Input array length is insufficient. Expected at least 7 elements, but got {}", parts.length);
            logger.warn(">>>>> Invalid Input array: " + String.join(",", parts));
            return null;
        }

        String command = parts[2];
        if ("RESULT".equals(StringUtils.upperCase(command))) {
            try {
                Integer num = Integer.parseInt(parts[1]);

                String subCommand = null;
                Matcher matcher = PATTERN.matcher(parts[3]);
                if (matcher.find()) {
                    subCommand = matcher.group(1); // Return the first capturing group (the number)
                } else {
                    logger.error(">>>>> ItemLensFieldOfViewTestParser: String does not match the expected pattern: {}", parts[3]);
                    return null;
                }
                String val = parts[6];
                logger.debug(">>>>> {}-ItemLensFieldOfViewTestParser: Command: {}, subCommand: {}, val: {}", parentCommand, command, subCommand, val);

                BoundsLoader boundsLoader = BoundsLoader.single(val);
                EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);

                eqLstCommand.setParentCommand(parentCommand);
                eqLstCommand.setCurrentCommand(command);
                eqLstCommand.setSubCommand(subCommand);
                eqLstCommand.setSequenceNumber(num);

                return eqLstCommand;
            } catch (NumberFormatException e) {
                logger.error(">>>>> ItemLensFieldOfViewTestParser: Invalid number format inspection parts[1]: {}", parts[1], e);
                return null;
            }
        }
        return null;
    }
}
