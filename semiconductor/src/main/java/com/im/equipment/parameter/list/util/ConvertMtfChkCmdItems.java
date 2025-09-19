package com.im.equipment.parameter.list.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.equipment.parameter.list.entity.struct.BoundsLoader;
import com.im.equipment.parameter.list.entity.struct.EqLstCommand;
import com.im.json.JsonMapperProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.im.constants.QtechImBizConstant.AGG_MTF_CHECK_ITEMS_FILTER_PREFIX;
import static com.im.constants.QtechImBizConstant.AGG_MTF_CHECK_ITEMS_RESULT_SUFFIX;

/**
 * 用于将 MTF_CHECK 命令的 ITEMS 转换为 Json格式字符串
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/12/18 09:00:38
 */
public class ConvertMtfChkCmdItems {
    private static final Logger logger = LoggerFactory.getLogger(ConvertMtfChkCmdItems.class);
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();

    public static List<EqLstCommand> convert(List<EqLstCommand> commands) {
        // 一次遍历生成 filteredCommands 和 remainingCommands
        List<EqLstCommand> filteredCommands = new ArrayList<>();
        List<EqLstCommand> remainingCommands = new ArrayList<>();
        for (EqLstCommand command : commands) {
            if (isValidCommand(command)) {
                filteredCommands.add(command);
            } else {
                remainingCommands.add(command);
            }
        }

        if (filteredCommands.isEmpty()) return remainingCommands;

        // 按前缀分组并过滤无效命令
        Map<String, List<EqLstCommand>> groupedCommands = filteredCommands.stream()
                .filter(cmd -> cmd.getBoundsLoader() != null &&
                        cmd.getBoundsLoader().hasSingleValue() &&
                        cmd.getSubCommand() != null)
                .collect(Collectors.groupingBy(EqLstCommand::getParentCommand));

        List<EqLstCommand> resultCommands = new ArrayList<>();
        for (Map.Entry<String, List<EqLstCommand>> entry : groupedCommands.entrySet()) {
            List<EqLstCommand> sameGroupNotNull = entry.getValue();

            // 根据subSystem进行排序
            sameGroupNotNull.sort(Comparator.comparingInt(cmd -> {
                try {
                    return Integer.parseInt(cmd.getSubCommand());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }));

            // 构建 Map，subSystem 作为键，value 作为值
            Map<String, String> subSystemValueMap = sameGroupNotNull.stream()
                    .collect(Collectors.toMap(
                            EqLstCommand::getSubCommand,
                            imAaListCommand -> {
                                BoundsLoader boundsLoader = imAaListCommand.getBoundsLoader();
                                return boundsLoader.getSingleValue();
                            },
                            (existing, replacement) -> existing, // 处理键冲突的情况
                            () -> new TreeMap<>(Comparator.comparingInt(s -> {
                                try {
                                    return Integer.parseInt(s);
                                } catch (NumberFormatException e) {
                                    return 0;
                                }
                            })) // 使用 TreeMap 并提供自定义比较器
                    ));

            String sameGroupJsonStr = null;
            try {
                sameGroupJsonStr = objectMapper.writeValueAsString(subSystemValueMap);
            } catch (JsonProcessingException e) {
                logger.error(">>>>> JSON解析失败, msg: {}", sameGroupNotNull, e);
            }

            // 使用新的ImAaListCommand构建方式
            BoundsLoader boundsLoader = BoundsLoader.single(sameGroupJsonStr);
            EqLstCommand temp = new EqLstCommand(boundsLoader);

            // 利用AbstractCommandStructure的属性存储命令信息
            temp.setParentCommand(null);     // 父命令存储前缀命令
            temp.setCurrentCommand(StringUtils.joinWith("_", entry.getKey(), AGG_MTF_CHECK_ITEMS_RESULT_SUFFIX)); // 当前命令存储命令类型
            temp.setSubCommand(null);        // 子命令存储子系统

            resultCommands.add(temp);
        }
        // 将剩余未通过过滤的命令添加到结果中
        resultCommands.addAll(remainingCommands);

        return resultCommands;
    }

    private static boolean isValidCommand(EqLstCommand command) {
        if (command == null || command.getParentCommand() == null) {
            return false;
        }
        if (command.getSubCommand() == null) {
            return false;
        }
        return AGG_MTF_CHECK_ITEMS_FILTER_PREFIX.stream().anyMatch(command.getParentCommand()::startsWith);
    }
}
