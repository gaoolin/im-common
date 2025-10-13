package com.im.aa.inspection.parser.item;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析AA相关参数的解析器
 * <p>
 * 处理形如以下格式的字符
 * ITEM	8	ROI		CC	100.00	58.00	30.00	1	1
 * ITEM	8	ROI		UL	100.00	35.00	20.00	1	2
 * ITEM	8	ROI		UR	100.00	35.00	20.00	1	3
 * ITEM	8	ROI		LL	100.00	35.00	20.00	1	4
 * ITEM	8	ROI		LR	100.00	35.00	20.00	1	5
 * <p>
 * 用到此解析器的List 命令包括：
 * AA1
 * AA2
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/10/08 10:40:42
 */
public final class ItemAaParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemAaParser.class);
    private static final Pattern PATTERN = Pattern.compile("([A-Za-z]+)");
    private static final String COMMAND_ROI = "ROI";
    private static final String COMMAND_MTF_OFF_AXIS_CHECK = "MTF_OFF_AXIS_CHECK";
    private static final String COMMAND_TARGET = "TARGET";
    private static final String COMMAND_CC_TO_CORNER_LIMIT = "CC_TO_CORNER_LIMIT";
    private static final String COMMAND_CC_TO_CORNER_LIMIT_MIN = "CC_TO_CORNER_LIMIT_MIN";
    private static final String COMMAND_CORNER_SCORE_DIFFERENCE_REJECT_VALUE = "CORNER_SCORE_DIFFERENCE_REJECT_VALUE";
    private static final String COMMAND_Z_REF = "Z_REF";
    private static final String COMMAND_SRCH_STEP = "SRCH_STEP";
    private static final String COMMAND_GOLDEN_GLUE_THICKNESS_MIN = "GOLDENGLUETHICKNESSMIN";
    private static final String COMMAND_GOLDEN_GLUE_THICKNESS_MAX = "GOLDENGLUETHICKNESSMAX";

    public static EqLstCommand apply(String[] parts, String parentCommand) {
        if (parts == null || parts.length < 4) {
            logWarn(parentCommand, "Invalid input array");
            return null;
        }

        String command = parts[2];
        int num;
        try {
            num = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            logError(parentCommand, "Invalid number format: " + parts[1]);
            return null;
        }

        switch (StringUtils.upperCase(command)) {
            case COMMAND_ROI:
                return parseRoi(parts, parentCommand, num);
            case COMMAND_MTF_OFF_AXIS_CHECK:
                return parseMtfOffAxisCheck(parts, parentCommand, num);
            case COMMAND_TARGET:
                return parseTarget(parts, parentCommand, num);
            case COMMAND_CC_TO_CORNER_LIMIT:
                return parseCcToCornerLimit(parts, parentCommand, num);
            case COMMAND_CC_TO_CORNER_LIMIT_MIN:
                return parseCcToCornerLimitMin(parts, parentCommand, num);
            case COMMAND_CORNER_SCORE_DIFFERENCE_REJECT_VALUE:
                return parseCornerScoreDifferenceRejectValue(parts, parentCommand, num);
            case COMMAND_Z_REF:
                return parseZRef(parts, parentCommand, num);
            case COMMAND_SRCH_STEP:
                return parseSrchStep(parts, parentCommand, num);
            case COMMAND_GOLDEN_GLUE_THICKNESS_MIN:
                return parseGoldenGlueThicknessMin(parts, parentCommand, num);
            case COMMAND_GOLDEN_GLUE_THICKNESS_MAX:
                return parseGoldenGlueThicknessMax(parts, parentCommand, num);
            default:
                // logWarn(parentCommand, "Unsupported command: " + command);
                return null;
        }
    }

    private static EqLstCommand parseRoi(String[] parts, String parentCommand, int num) {
        if (parts.length < 6) {
            logWarn(parentCommand, "ROI: Invalid number of parts inspection the command: " + parts.length);
            return null;
        }

        String subCommand = null;
        Matcher matcher = PATTERN.matcher(parts[3]);

        if (matcher.find()) {
            subCommand = matcher.group(1);
            if (isValidSubSystem(subCommand)) {
                String val = parts[5];
                logDebug(parentCommand, "ROI: subCommand: " + subCommand + ", " + val);

                // 使用新的AaListCommand创建命令
                BoundsLoader boundsLoader = BoundsLoader.single(val);
                EqLstCommand cmd = EqLstCommand.of(boundsLoader);
                cmd.setParentCommand(parentCommand);
                cmd.setCurrentCommand(COMMAND_ROI);
                cmd.setSubCommand(subCommand);
                cmd.setSequenceNumber(num);
                return cmd;
            } else {
                logWarn(parentCommand, "Invalid subsystem: " + subCommand);
                return null;
            }
        } else {
            // logWarn(parentCommand, "AaHandler string does not match the expected pattern");
            return null;
        }
    }

    private static EqLstCommand parseMtfOffAxisCheck(String[] parts, String parentCommand, int num) {
        String val = parts[3];
        logDebug(parentCommand, "MTF_OFF_AXIS_CHECK: " + val);

        BoundsLoader boundsLoader = BoundsLoader.single(val);
        EqLstCommand cmd = EqLstCommand.of(boundsLoader);
        cmd.setParentCommand(parentCommand);
        cmd.setCurrentCommand(COMMAND_MTF_OFF_AXIS_CHECK);
        cmd.setSequenceNumber(num);
        return cmd;
    }

    private static EqLstCommand parseTarget(String[] parts, String parentCommand, int num) {
        String value = parts[3];
        logDebug(parentCommand, COMMAND_TARGET + ": value: " + value);

        BoundsLoader boundsLoader = BoundsLoader.single(value);
        EqLstCommand cmd = EqLstCommand.of(boundsLoader);
        cmd.setParentCommand(parentCommand);
        cmd.setCurrentCommand(COMMAND_TARGET);
        cmd.setSequenceNumber(num);
        return cmd;
    }

    private static EqLstCommand parseCcToCornerLimit(String[] parts, String parentCommand, int num) {
        String value = parts[3];
        logDebug(parentCommand, COMMAND_CC_TO_CORNER_LIMIT + ": value: " + value);

        BoundsLoader boundsLoader = BoundsLoader.single(value);
        EqLstCommand cmd = EqLstCommand.of(boundsLoader);
        cmd.setParentCommand(parentCommand);
        cmd.setCurrentCommand(COMMAND_CC_TO_CORNER_LIMIT);
        cmd.setSequenceNumber(num);
        return cmd;
    }

    private static EqLstCommand parseCcToCornerLimitMin(String[] parts, String parentCommand, int num) {
        String value = parts[3];
        logDebug(parentCommand, COMMAND_CC_TO_CORNER_LIMIT_MIN + ": value: " + value);

        BoundsLoader boundsLoader = BoundsLoader.single(value);
        EqLstCommand cmd = EqLstCommand.of(boundsLoader);
        cmd.setParentCommand(parentCommand);
        cmd.setCurrentCommand(COMMAND_CC_TO_CORNER_LIMIT_MIN);
        cmd.setSequenceNumber(num);
        return cmd;
    }

    private static EqLstCommand parseCornerScoreDifferenceRejectValue(String[] parts, String parentCommand, int num) {
        String value = parts[3];
        logDebug(parentCommand, COMMAND_CORNER_SCORE_DIFFERENCE_REJECT_VALUE + ": value: " + value);

        BoundsLoader boundsLoader = BoundsLoader.single(value);
        EqLstCommand cmd = EqLstCommand.of(boundsLoader);
        cmd.setParentCommand(parentCommand);
        cmd.setCurrentCommand(COMMAND_CORNER_SCORE_DIFFERENCE_REJECT_VALUE);
        cmd.setSequenceNumber(num);
        return cmd;
    }

    private static EqLstCommand parseZRef(String[] parts, String parentCommand, int num) {
        String value = parts[3];
        logDebug(parentCommand, COMMAND_Z_REF + ": value: " + value);

        BoundsLoader boundsLoader = BoundsLoader.single(value);
        EqLstCommand cmd = EqLstCommand.of(boundsLoader);
        cmd.setParentCommand(parentCommand);
        cmd.setCurrentCommand(COMMAND_Z_REF);
        cmd.setSequenceNumber(num);
        return cmd;
    }

    private static EqLstCommand parseSrchStep(String[] parts, String parentCommand, int num) {
        String value = parts[3];
        logDebug(parentCommand, COMMAND_SRCH_STEP + ": value: " + value);

        BoundsLoader boundsLoader = BoundsLoader.single(value);
        EqLstCommand cmd = EqLstCommand.of(boundsLoader);
        cmd.setParentCommand(parentCommand);
        cmd.setCurrentCommand(COMMAND_SRCH_STEP);
        cmd.setSequenceNumber(num);
        return cmd;
    }

    private static EqLstCommand parseGoldenGlueThicknessMin(String[] parts, String parentCommand, int num) {
        String value = parts[3];
        logDebug(parentCommand, COMMAND_GOLDEN_GLUE_THICKNESS_MIN + ": value: " + value);

        BoundsLoader boundsLoader = BoundsLoader.single(value);
        EqLstCommand cmd = EqLstCommand.of(boundsLoader);
        cmd.setParentCommand(parentCommand);
        cmd.setCurrentCommand(COMMAND_GOLDEN_GLUE_THICKNESS_MIN);
        cmd.setSequenceNumber(num);
        return cmd;
    }

    private static EqLstCommand parseGoldenGlueThicknessMax(String[] parts, String parentCommand, int num) {
        String value = parts[3];
        logDebug(parentCommand, COMMAND_GOLDEN_GLUE_THICKNESS_MAX + ": value: " + value);

        BoundsLoader boundsLoader = BoundsLoader.single(value);
        EqLstCommand cmd = EqLstCommand.of(boundsLoader);
        cmd.setParentCommand(parentCommand);
        cmd.setCurrentCommand(COMMAND_GOLDEN_GLUE_THICKNESS_MAX);
        cmd.setSequenceNumber(num);
        return cmd;
    }

    private static boolean isValidSubSystem(String subCommand) {
        return "CC".equals(subCommand) || "UL".equals(subCommand) || "UR".equals(subCommand) || "LL".equals(subCommand) || "LR".equals(subCommand);
    }

    private static void logError(String parentCommand, String message) {
        logger.error(">>>>> {}-ItemAaParser: {}", parentCommand, message);
    }

    private static void logWarn(String parentCommand, String message) {
        logger.warn(">>>>> {}-ItemAaParser: {}", parentCommand, message);
    }

    private static void logInfo(String parentCommand, String message) {
        logger.info(">>>>> {}-ItemAaParser: {}", parentCommand, message);
    }

    private static void logDebug(String parentCommand, String message) {
        logger.debug(">>>>> {}-ItemAaParser: {}", parentCommand, message);
    }
}
