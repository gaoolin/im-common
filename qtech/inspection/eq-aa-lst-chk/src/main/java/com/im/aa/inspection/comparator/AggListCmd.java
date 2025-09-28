package com.im.aa.inspection.comparator;

import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MTF检查命令聚合工具类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/09/24 17:06:10
 */
public class AggListCmd {
    private static final String AGG_MTF_CHECK_ITEMS_RESULT_SUFFIX = "F";

    public static List<EqLstCommand> aggregateMtfCheckCommands(List<EqLstCommand> commands) {
        // 过滤符合条件的命令
        List<EqLstCommand> filteredCommands = commands.stream().filter(AggListCmd::isValidCommand).collect(Collectors.toList());

        // 获取剩余未通过过滤的命令
        List<EqLstCommand> remainingCommands = new ArrayList<>(commands);
        remainingCommands.removeAll(filteredCommands); // 从原始列表中移除过滤后的命令

        // 按前缀分组
        Map<String, List<EqLstCommand>> groupedCommands = filteredCommands.stream().collect(Collectors.groupingBy(EqLstCommand::getParentCommand));

        List<EqLstCommand> resultCommands = new ArrayList<>();
        for (Map.Entry<String, List<EqLstCommand>> entry : groupedCommands.entrySet()) {
            List<EqLstCommand> sameGroup = entry.getValue();

            ArrayList<EqLstCommand> tempAgg = new ArrayList<>();

            // 根据subSystem进行排序
            sameGroup.sort(Comparator.comparingInt(cmd -> {
                try {
                    return Integer.parseInt(cmd.getSubCommand());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }));

            int commandCounter = 1; // 每组初始化计数器

            for (int i = 0; i < sameGroup.size(); ) {
                EqLstCommand currentCommand = sameGroup.get(i);
                if (currentCommand.getBoundsLoader() == null || currentCommand.getBoundsLoader().getSingleValue() == null || currentCommand.getSubCommand() == null) {
                    i++;
                    continue; // 跳过无效命令
                }

                String currentVal = currentCommand.getBoundsLoader().getSingleValue();
                List<EqLstCommand> aggregatedGroup = new ArrayList<>();
                aggregatedGroup.add(currentCommand);

                int subSystemIndex = Integer.parseInt(currentCommand.getSubCommand());
                i++;

                while (i < sameGroup.size()) {
                    EqLstCommand nextCommand = sameGroup.get(i);
                    if (nextCommand.getBoundsLoader() != null && nextCommand.getBoundsLoader().getSingleValue() != null && nextCommand.getSubCommand() != null && nextCommand.getBoundsLoader().getSingleValue().equals(currentVal) && Integer.parseInt(nextCommand.getSubCommand()) == subSystemIndex + 1) {
                        aggregatedGroup.add(nextCommand);
                        subSystemIndex++;
                        i++;
                    } else {
                        break;
                    }
                }

                // 检查是否所有subSystem在9到12之间
                boolean allSubsystemsInRange = aggregatedGroup.stream().allMatch(cmd -> {
                    try {
                        int subSysValue = Integer.parseInt(cmd.getSubCommand());
                        return subSysValue >= 9 && subSysValue <= 12;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });

                // 创建新的AaListCommand并添加到聚合列表
                EqLstCommand aggregatedCommand;
                if (allSubsystemsInRange) {
                    aggregatedCommand = createAggregatedCommand(aggregatedGroup, "c"); // subSystem设置为"c"
                } else {
                    aggregatedCommand = createAggregatedCommand(aggregatedGroup, String.valueOf(commandCounter)); // 使用计数器
                    commandCounter++; // 增加计数器
                }
                tempAgg.add(aggregatedCommand);
            }
            // 合并subSystem为1和2的对象
            mergeSubsystems(tempAgg);
            resultCommands.addAll(tempAgg);
        }

        // 将剩余未通过过滤的命令添加到结果中
        resultCommands.addAll(remainingCommands);

        return resultCommands;
    }

    private static void mergeSubsystems(List<EqLstCommand> tempAgg) {
        List<EqLstCommand> subsystemsOneAndTwo = tempAgg.stream().filter(cmd -> cmd.getSubCommand() != null && (cmd.getSubCommand().equals("1") || cmd.getSubCommand().equals("2"))).collect(Collectors.toList());

        if (subsystemsOneAndTwo.size() == 2) {
            EqLstCommand command1 = subsystemsOneAndTwo.get(0);
            EqLstCommand command2 = subsystemsOneAndTwo.get(1);

            if (command1.getBoundsLoader() != null && command2.getBoundsLoader() != null && command1.getBoundsLoader().getSingleValue() != null && command2.getBoundsLoader().getSingleValue() != null && command1.getBoundsLoader().getSingleValue().equals(command2.getBoundsLoader().getSingleValue())) {
                // 合并逻辑
                tempAgg.remove(command1);
                tempAgg.remove(command2);

                // 使用新的AaListCommand构建方式
                BoundsLoader boundsLoader = BoundsLoader.single(command1.getBoundsLoader().getSingleValue());
                EqLstCommand mergedCommand = new EqLstCommand(boundsLoader);

                // 利用AbstractCommandStructure的属性存储命令信息
                mergedCommand.setParentCommand(command1.getParentCommand());     // 父命令存储前缀命令
                mergedCommand.setCurrentCommand(AGG_MTF_CHECK_ITEMS_RESULT_SUFFIX); // 当前命令存储命令类型
                mergedCommand.setSubCommand("1");           // 子命令设置为"1"
                mergedCommand.setSequenceNumber(command1.getSequenceNumber() + command2.getSequenceNumber()); // 序号存储命令序号

                tempAgg.add(mergedCommand);

                // 其他对象的value减1
                for (EqLstCommand command : tempAgg) {
                    if (!"c".equals(command.getSubCommand())) {
                        try {
                            int subCommandValue = Integer.parseInt(command.getSubCommand());
                            if (subCommandValue > 1) {
                                command.setSubCommand(String.valueOf(subCommandValue - 1));
                            }
                        } catch (NumberFormatException e) {
                            // 忽略无法解析的subCommand
                        }
                    }
                }
            }
        }
    }

    private static EqLstCommand createAggregatedCommand(List<EqLstCommand> commands, String subSystemValue) {
        EqLstCommand firstCommand = commands.get(0);

        // 使用新的AaListCommand构建方式
        BoundsLoader boundsLoader = BoundsLoader.single(firstCommand.getBoundsLoader().getSingleValue());
        EqLstCommand aggregatedCommand = new EqLstCommand(boundsLoader);

        // 利用AbstractCommandStructure的属性存储命令信息
        aggregatedCommand.setParentCommand(firstCommand.getParentCommand());     // 父命令存储前缀命令
        aggregatedCommand.setCurrentCommand(AGG_MTF_CHECK_ITEMS_RESULT_SUFFIX);   // 当前命令存储命令类型
        aggregatedCommand.setSubCommand(subSystemValue);                         // 子命令设置为传入的subSystem值
        aggregatedCommand.setSequenceNumber(commands.size());                    // 序号存储命令数量

        return aggregatedCommand;
    }

    private static boolean isValidCommand(EqLstCommand command) {
        if (command == null || command.getParentCommand() == null) {
            return false;
        }
        boolean containsAny = StringUtils.containsAny(command.getSubCommand(), "CC", "UL", "UR", "LL", "LR");
        return command.getParentCommand().startsWith("MTF_Check") || (command.getParentCommand().startsWith("AA") && Objects.equals(command.getCurrentCommand(), "ROI") && !containsAny);
    }
}
