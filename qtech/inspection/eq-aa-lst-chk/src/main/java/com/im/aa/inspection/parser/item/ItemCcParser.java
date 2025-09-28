package com.im.aa.inspection.parser.item;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * desc   :  CC处理器
 * 处理形如以下格式的字符
 * ITEM	18	RESULT		[CC]	Check	30.00	0.00	Log	100.00	100.00	100.00	100.00	30.00	30.00	50.00	1.00	NoCompare	0.00	0.00	0.00
 * <p>
 * 用到此解析器的List 命令包括：
 * VCM_Check
 * mtf_check
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/10/08 09:37:58
 */
public final class ItemCcParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemCcParser.class);
    // 匹配一个形如 [非空字符串] 的字符串，并且该字符串中必须包含至少一个英文字母
    private static final Pattern PATTERN = Pattern.compile("\\[([a-zA-Z].*|.*[a-zA-Z].*)\\]");

    public static EqLstCommand apply(String[] parts, String parentCommand) {
        if (parts.length < 7) {
            logger.warn(">>>>> ItemCcParser: Input array length is insufficient. Expected at least 7 elements, but got {}", parts.length);
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
                    subCommand = matcher.group(1);
                } else {
                    logger.warn(">>>>> ItemCcParser: String does not match the expected pattern: {}", parts[3]);
                    return null;
                }
                String val = parts[6];
                logger.debug(">>>>> {}-ItemCcParser: Command: {}, subCommand: {}, val: {}", parentCommand, command, subCommand, val);

                BoundsLoader boundsLoader = BoundsLoader.single(val);

                EqLstCommand eqLstCommand = EqLstCommand.of(boundsLoader);

                eqLstCommand.setParentCommand(parentCommand);
                eqLstCommand.setCurrentCommand(command);
                eqLstCommand.setSubCommand(subCommand);
                eqLstCommand.setSequenceNumber(num);

                return eqLstCommand;
            } catch (NumberFormatException e) {
                logger.error(">>>>> ItemCcParser: Invalid number format inspection parts[1]: {}", parts[1], e);
                return null;
            }
        }
        return null;
    }
}
