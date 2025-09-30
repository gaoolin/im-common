package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.parser.item.ItemRecordPositionParser;
import com.im.aa.inspection.parser.item.ItemUtXyzMoveParser;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

import java.util.Arrays;

/**
 * <p>
 * ZOffset，vcm马达上台下拉，固定值
 *
 * <p>
 * 此命令在台虹和古城意义不同：
 * 在台虹厂区，仅仅是记录位置的命令，不包含上抬下拉
 * 在古城厂区，包含上抬下拉和记录位置两项参数
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/05/28 11:23:36
 */
public final class RecordPositionHandler extends CommandHandler<EqLstCommand> implements AutoRegisteredHandler<EqLstCommand> {
    // 饿汉式单例
    public static final RecordPositionHandler INSTANCE = new RecordPositionHandler();

    public RecordPositionHandler() {
        super(EqLstCommand.class);
    }

    /**
     * 获取单例实例
     *
     * @return RecordPositionHandler单例
     */
    public static RecordPositionHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 处理命令
     *
     * @param parts     命令的部分
     * @param parentCmd 前缀命令（可选）
     * @return 处理结果
     */
    @Override
    public EqLstCommand handle(String[] parts, String parentCmd) {
        try {
            if (parts.length == 3) {
                return ItemRecordPositionParser.apply(parts, parentCmd);
            } else {
                return ItemUtXyzMoveParser.apply(parts, parentCmd);
            }
        } catch (Exception e) {
            logger.error(">>>>> {} handle error for parts: {}, parentCmd: {}. Error: {}",
                    this.getClass().getName(), Arrays.toString(parts), parentCmd, e.getMessage(), e);
        }
        return null;
    }

    /**
     * 创建Handler实例
     *
     * @return Handler实例
     */
    @Override
    public CommandHandler<EqLstCommand> createInstance() {
        return getInstance();
    }
}
