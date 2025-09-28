package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.parser.item.ItemAaParser;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

/**
 * <p>
 * AA Item
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/05/28 10:57:30
 */
public final class AaHandler extends CommandHandler<EqLstCommand> {
    // 饿汉式单例
    private static final AaHandler INSTANCE = new AaHandler();

    private AaHandler() {
        super(EqLstCommand.class);
    }

    /**
     * 获取单例实例
     *
     * @return AaHandler单例
     */
    public static AaHandler getInstance() {
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
        return ItemAaParser.apply(parts, parentCmd);
    }
}
