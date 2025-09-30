package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.parser.item.ItemUtXyzMoveParser;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

/**
 * <p>
 * 上抬下拉
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/01/15 15:02:52
 */
public final class UtXyzMoveHandler extends CommandHandler<EqLstCommand> implements AutoRegisteredHandler<EqLstCommand> {
    // 饿汉式单例
    public static final UtXyzMoveHandler INSTANCE = new UtXyzMoveHandler();

    public UtXyzMoveHandler() {
        super(EqLstCommand.class);
    }

    /**
     * 获取单例实例
     *
     * @return UtXyzMoveHandler单例
     */
    public static UtXyzMoveHandler getInstance() {
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
        return ItemUtXyzMoveParser.apply(parts, parentCmd);
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
