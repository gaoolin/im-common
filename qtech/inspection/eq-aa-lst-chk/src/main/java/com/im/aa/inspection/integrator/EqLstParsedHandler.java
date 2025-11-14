package com.im.aa.inspection.integrator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.im.aa.inspection.comparator.MtfChkCmdItemsConverterV2;
import com.im.aa.inspection.constant.CommandHandlerMapper;
import com.im.aa.inspection.entity.param.EqLstParsed;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.handler.AutoRegisteredHandler;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.im.common.json.JsonMapperProvider;
import org.im.semiconductor.common.dispatcher.CommandHandlerDispatcher;
import org.im.semiconductor.common.handler.cmd.CommandHandler;
import org.im.semiconductor.common.handler.msg.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.im.qtech.data.constant.EqLstInspectionConstants.CONTROL_LIST_SET;
import static com.im.qtech.data.constant.QtechImBizConstant.*;

/**
 * List、Item 级联解析
 * <p>
 * 线程安全性说明：
 * - 该类设计为无状态的工具类，所有实例变量均为线程安全或不共享
 * - 使用ThreadLocal确保线程间数据隔离
 * - 使用ConcurrentHashMap确保集合操作的线程安全
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/27
 */
public class EqLstParsedHandler extends MessageHandler<EqLstParsed> implements AutoRegisteredHandler<EqLstParsed> {
    /**
     * 饿汉式单例实例
     */
    public static final EqLstParsedHandler INSTANCE = new EqLstParsedHandler();
    private static final Logger logger = LoggerFactory.getLogger(EqLstParsedHandler.class);
    // 使用ThreadLocal确保每个线程有独立的HashMap实例
    private static final ThreadLocal<HashMap<Integer, String>> listItemMapper = ThreadLocal.withInitial(HashMap::new);
    // 使用ThreadLocal确保每个线程有独立的EqLstParsed实例
    private static final ThreadLocal<EqLstParsed> threadLocalEqLstMessage = ThreadLocal.withInitial(EqLstParsed::new);
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();
    // 使用单例模式获取HandlerDispatcher（线程安全）
    private final CommandHandlerDispatcher commandHandlerDispatcher = CommandHandlerDispatcher.getInstance();
    // 使用单例模式获取CommandHandlerMapper（线程安全）
    private final CommandHandlerMapper commandHandlerMapper = CommandHandlerMapper.getInstance();

    /**
     * 私有构造函数，防止外部直接实例化
     */
    public EqLstParsedHandler() {
        super(EqLstParsed.class);
    }

    /**
     * 获取单例实例
     *
     * @return ListIntegrator单例实例
     */
    public static EqLstParsedHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 获取当前线程的listItemMapper并清空
     *
     * @return 清空后的HashMap
     */
    private static HashMap<Integer, String> getListItemMapper() {
        HashMap<Integer, String> map = listItemMapper.get();
        map.clear();
        return map;
    }

    /**
     * 获取当前线程的EqLstParsed并重置
     *
     * @return 重置后的AaListParamParsed实例
     */
    private EqLstParsed getEqLstParsed() {
        EqLstParsed eqLstParsed = threadLocalEqLstMessage.get();
        eqLstParsed.reset();
        return eqLstParsed;
    }

    /**
     * 清理当前线程的ThreadLocal资源，防止内存泄漏
     */
    private void cleanupThreadLocal() {
        HashMap<Integer, String> mapper = listItemMapper.get();
        if (mapper != null) {
            mapper.clear();
        }
        listItemMapper.remove();
        threadLocalEqLstMessage.remove();
    }

    public EqLstCommand parseRowStartWithList(String[] parts) {
        try {
            // 获取处理器
            CommandHandler<EqLstCommand> handler = commandHandlerDispatcher.getCommandHandler("ListHandler");
            // 使用处理器处理命令
            return handler != null ? handler.handle(parts) : null;
        } catch (RuntimeException e) {
            // 处理未找到处理器的情况
            logger.warn(">>>>> 未找到List处理器: {}", e.getMessage());
        }
        return null;
    }

    public EqLstCommand parseRowStartWithItem(String[] parts, String handlerName, String command) {
        if (handlerName != null) {
            try {
                // 获取处理器
                CommandHandler<EqLstCommand> handler = commandHandlerDispatcher.getCommandHandler(handlerName + "Handler");
                // 使用处理器处理命令
                return handler != null ? handler.handle(parts, command) : null;
            } catch (RuntimeException e) {
                // 处理未找到处理器的情况
                logger.warn(">>>>> 未找到Item处理器:{}\n{}", handlerName, e.getMessage());
                return null;
            }
        } else {
            logger.warn(">>>>> listItemMapper is empty");
            return null;
        }
    }

    public EqLstParsed doFullParse(String msg) {
        try {
            EqLstParsed aaListParamsParsed = getEqLstParsed();
            HashMap<Integer, String> mapper = getListItemMapper();

            // 将每一行数据拆分并转换为 EqLstCommand 对象
            List<EqLstCommand> eqLst = Arrays.stream(msg.split("\n"))
                    .map(String::trim)  // 去除每行的空白字符
                    .filter(line -> !line.isEmpty())  // 跳过空行
                    .filter(line -> StringUtils.startsWith(line, "LIST") || StringUtils.startsWith(line, "ITEM"))
                    .map(line -> line.split("\\s+"))  // 按空格分割每一行
                    .map(parts -> {
                        String startWithStr = parts[0];
                        try {
                            if ("LIST".equals(startWithStr)) {
                                String listNmb = parts[1];
                                String command = parts[2];
                                if (CONTROL_LIST_SET.contains(command)) {
                                    mapper.put(Integer.parseInt(listNmb), command);
                                }
                                return parseRowStartWithList(parts);  // 解析 LIST 行
                            } else if ("ITEM".equals(startWithStr)) {
                                Integer key = Integer.parseInt(parts[1]);
                                String command = mapper.get(key);
                                if (command != null) {
                                    String handlerName = commandHandlerMapper.get(command);
                                    if (!StringUtils.isEmpty(handlerName)) {
                                        return parseRowStartWithItem(parts, handlerName, command);  // 解析 ITEM 行
                                    }
                                }
                            } else {
                                logger.warn(">>>>> Unsupported line: {}", Arrays.toString(parts));
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            logger.error(">>>>> ArrayIndexOutOfBoundsException: {}", e.getMessage(), e);
                        } catch (Exception e) {
                            logger.error(">>>>> Exception occurred while processing line: {}", Arrays.toString(parts), e);
                        }
                        return null;  // 如果处理失败返回 null
                    })
                    .filter(Objects::nonNull)  // 过滤掉 null 的结果
                    .collect(Collectors.toList());  // 收集到列表中

            // 聚合 EqLstCommand，并返回聚合后的列表， 聚合 MTF_CHECK 命令的解析结果
            List<EqLstCommand> aggregatedCommands = MtfChkCmdItemsConverterV2.convert(eqLst);
            // 将命令列表填充到 aaListParamsParsed 中
            aaListParamsParsed.fillWithData(aggregatedCommands);
            return aaListParamsParsed;
        } finally {
            // 确保清理ThreadLocal资源
            cleanupThreadLocal();
        }
    }

    @Override
    public <R> R handleByType(Class<R> clazz, String msg) throws DecoderException {
        if (clazz == EqLstParsed.class) {
            try {
                Map<String, Object> jsonObject = objectMapper.readValue(msg, TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class));

                String aaListParamHexStr = (String) jsonObject.get(EQP_LST_RAW_HEX_FILED);
                String aaListParamStr;
                try {
                    aaListParamStr = new String(Hex.decodeHex(aaListParamHexStr));
                } catch (DecoderException e) {
                    logger.error(">>>>> Hex解码异常，机型: {}", jsonObject.get("WoCode"));
                    throw e; // 抛出异常以便上层处理
                }
                EqLstParsed aaListParamsParsedObj = doFullParse(aaListParamStr);
                String simId = jsonObject.get(EQP_LST_RAW_SIMID_FILED).toString();
                aaListParamsParsedObj.setSimId(simId);
                String module = StringUtils.trim(jsonObject.get(EQP_LST_RAW_MODULE_FILED).toString().split("#")[0]);
                aaListParamsParsedObj.setModuleId(module);
                return clazz.cast(aaListParamsParsedObj);
            } catch (JsonProcessingException e) {
                logger.error(">>>>> JSON 解析异常", e);
                throw new RuntimeException("JSON解析失败", e); // 抛出异常以便上层处理
            }
        }
        throw new UnsupportedOperationException("Unsupported message handling for policy: " + clazz.getSimpleName());
    }

    @Override
    public <U> boolean supportsType(Class<U> clazz) {
        return clazz.equals(EqLstParsed.class);
    }

    /**
     * 处理消息
     *
     * @param msg 消息内容
     * @return 处理结果
     * @throws DecoderException 解码异常
     */
    @Override
    public EqLstParsed handle(String msg) throws DecoderException {
        return null;
    }

    /**
     * 创建Handler实例
     *
     * @return Handler实例
     */
    @Override
    public Object createInstance() {
        return getInstance();
    }
}
