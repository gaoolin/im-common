package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.parser.item.ItemCcParser;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

/**
 * <p>
 * 处理List为Save_MTF的命令
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/09/26 14:38:02
 */
public final class SaveMtfHandler extends CommandHandler<EqLstCommand> implements AutoRegisteredHandler<EqLstCommand> {
    // 饿汉式单例
    public static final SaveMtfHandler INSTANCE = new SaveMtfHandler();

    public SaveMtfHandler() {
        super(EqLstCommand.class);
    }

    /**
     * 获取单例实例
     *
     * @return SaveMtfHandler单例
     */
    public static SaveMtfHandler getInstance() {
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
        return ItemCcParser.apply(parts, parentCmd);
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
