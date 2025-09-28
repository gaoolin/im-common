package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.parser.list.ListParser;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

/**
 * <p>
 * List解析项
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/05/28 11:25:27
 */
public final class ListHandler extends CommandHandler<EqLstCommand> {
    // 饿汉式单例
    private static final ListHandler INSTANCE = new ListHandler();

    private ListHandler() {
        super(EqLstCommand.class);
    }

    /**
     * 获取单例实例
     *
     * @return ListHandler单例
     */
    public static ListHandler getInstance() {
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
        return ListParser.apply(parts, parentCmd);
    }
}
