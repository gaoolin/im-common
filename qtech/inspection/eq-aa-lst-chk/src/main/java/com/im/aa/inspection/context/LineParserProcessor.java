package com.im.aa.inspection.context;

import com.im.aa.inspection.entity.param.EqLstParsed;
import org.im.exception.constants.ErrorCode;
import org.im.exception.type.common.BusinessException;

import java.util.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/08 14:21:32
 */
public class LineParserProcessor {
    private final EqLstParsed qtechEqLstParsed = new EqLstParsed();
    private final Map<String, String> listMap = new HashMap<>();
    private final Map<String, String> listItemMap = new HashMap<>();

    public static List<Command> processStringList(String[] stringList) {
        List<Command> commandList = new ArrayList<>();

        for (String line : stringList) {
            if (line.startsWith("LIST")) {
                String[] parts = line.split("\\s+");
                int num = Integer.parseInt(parts[1]);
                String command = parts[2];
                String subsystem = parts[3];
                String enable = parts[parts.length - 1]; // Get the last part
                Command currentCommand = new Command(num, command, subsystem, enable);
                commandList.add(currentCommand);
            }
        }

        return commandList;
    }

    public static void main(String[] args) {
        String s = "LIST    1   Init    Tester  5   Continue    Enable  \n" + "LIST    2   ClampOnOff  Motion  Continue    Stop    Enable  \n" + "LIST    3   ReInit  Tester  Continue    -2  Enable  \n" + "LIST    4   PRToBond    Motion  Continue    -2  Enable  \n" + "LIST    5   SID Tester  Continue    Continue    Enable  \n" + "LIST    6   AA1 ActiveAlign 8   Continue    Enable  \n" + "LIST    7   AA2 ActiveAlign Continue    Stop    Enable  \n" + "LIST    8   LP_ON_OC    Motion  Continue    Stop    Enable  \n" + "LIST    9   OpticCenter CenterAlign Continue    Stop    Enable  \n" + "LIST    10  LP_ON_Blemish   Motion  Continue    Stop    Enable  \n" + "LIST    11  Blemish Tester  Continue    Stop    Enable  \n" + "LIST    12  LP_OFF  Motion  Continue    Stop    Enable  \n" + "LIST    13  RecordPosition  Motion  Continue    Stop    Enable  \n" + "LIST    14  Dispense    Motion  Continue    Stop    Enable  \n" + "LIST    15  EpoxyInspection Auto Motion 17  Continue    Enable  \n" + "LIST    16  EpoxyInspection Motion  Continue    Stop    Enable  \n" + "LIST    17  BackToPosition  Motion  Continue    Stop    Enable  \n" + "LIST    18  UVON    Sync Motion Continue    Stop    Enable  \n" + "LIST    19  Reinit2 Tester  Continue    Continue    Enable  \n" + "LIST    20  UVOFF   Sync End    Continue    Stop    Enable  \n" + "LIST    21  Gripper Open    Motion  Continue    Stop    Enable  \n" + "LIST    22  OC_Check    Tester  Continue    Stop    Enable  \n" + "PRE 0\n" + "PRE2    0\n" + "POST    51  Tester  \n" + "POST2   52  Motion  \n";

        String[] lines = s.split("\n");
        List<Command> commands = processStringList(lines);

        // 输出命令列表
        for (Command command : commands) {
            System.out.println("EqLstCommand: " + command.num);
            System.out.println("  EqLstCommand: " + command.command);
            System.out.println("  Subsystem: " + command.subsystem);
            System.out.println("  Enable: " + command.enable);
            System.out.println();
        }
    }

    public EqLstParsed doAaParamsParse(String msg) {
/*        StringTokenizer tokenizer = new StringTokenizer(msg, " \t\r\n");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if ("LIST".equals(token)) {
                parseListStart(tokenizer, qtechEqLstParsed, token, listItemMap);
            } else if ("ITEM".equals(token)) {
                parseItemStart(tokenizer, qtechEqLstParsed, token);
            }
        }*/
        String[] lines = msg.split("\n");
        List<Command> commands = processStringList(lines);

        commands.forEach(command -> {
            // qtechEqLstParsed.fillWithData(command);
        });

        System.out.println(qtechEqLstParsed);
        return qtechEqLstParsed;
    }

    public void parseListStart(StringTokenizer tokenizer, EqLstParsed qtechEqLstParsed, String token, Map<String, String> listItemMap) {

        String num = tokenizer.nextToken(); // Skip the number (1)
        String action = tokenizer.nextToken(); // Get the action (Destroy_Start)
        String tester = tokenizer.nextToken(); // Get the action (Destroy_Start)
        String ctu = tokenizer.nextToken(); // Get the action (Destroy_Start)
        String fail = tokenizer.nextToken(); // Get the action (Destroy_Start)
        String status = tokenizer.nextToken(); // Get the status (Enable)
        // break; // Assuming only one such LIST entry per input

        HashMap<String, String> eqLstMap = new HashMap<>();

        String listName = token + "_" + num + "_" + action;
        eqLstMap.put(listName, status);
        listItemMap.put(action, num);

        // qtechEqLstParsed.fillWithData(aaParamsMap);

        System.out.println(listItemMap);
        System.out.println(qtechEqLstParsed);
    }

    public void parseItemStart(StringTokenizer tokenizer, EqLstParsed qtechEqLstParsed, String token) {

        String num = tokenizer.nextToken();

        if (listItemMap.isEmpty()) {
            throw new BusinessException(ErrorCode.BIZ_DATA_NOT_FOUND, "List action is empty");
            // break;
        } else {
            listItemMap.forEach((key, value) -> {
                if (value.equals(num)) {
                    String param = tokenizer.nextToken();
                    String desc = tokenizer.nextToken();
                    String s = tokenizer.nextToken();
                    String itemName = key + "_" + tokenizer.nextToken();
                    String itemValue = tokenizer.nextToken();
                    HashMap<String, String> aaParamsMap = new HashMap<>();
                    aaParamsMap.put(itemName, itemValue);
                    // qtechEqLstParsed.fillWithData(aaParamsMap);
                }
            });
        }
/*        if ("3".equals(num)) {
            String param = tokenizer.nextToken();
            if ("ROI".equals(param)) {
                String desc = tokenizer.nextToken();
                String celling = tokenizer.nextToken();
                String actual = tokenizer.nextToken();
                String floor = tokenizer.nextToken();
                String itemName = token + "_" + num + "_" + param + "_" + desc;
                HashMap<String, String> aaParamsMap = new HashMap<>();
                aaParamsMap.put(itemName, actual);
                aaParams.fillWithData(aaParamsMap);
            } else if ("TARGET".equals(param)) {
                String desc = tokenizer.nextToken();
                String itemName = token + "_" + num + "_" + param;
                HashMap<String, String> aaParamsMap = new HashMap<>();
                aaParamsMap.put(itemName, desc);
                aaParams.fillWithData(aaParamsMap);
            } else if ("CC_TO_CORNER_LIMIT".equals(param)) {
                String desc = tokenizer.nextToken();
                String itemName = token + "_" + num + "_" + param;
                HashMap<String, String> aaParamsMap = new HashMap<>();
                aaParamsMap.put(itemName, desc);
                aaParams.fillWithData(aaParamsMap);
            }
        } else if ("4".equals(num)) {
            String param = tokenizer.nextToken();
            if ("".equals(param)) {
                String desc = tokenizer.nextToken();
            }
        }*/
    }

    public static class Command {
        int num;
        String command;
        String subsystem;
        String enable;

        public Command(int num, String command, String subsystem, String enable) {
            this.num = num;
            this.command = command;
            this.subsystem = subsystem;
            this.enable = enable;
        }

        // Getters and setters if needed
    }
}
