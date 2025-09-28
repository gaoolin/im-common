package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.parser.item.ItemXyOffsetParser;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

/**
 * <p>
 * 处理List名为OC_Check Save_OC 的命令
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/09/26 14:12:35
 */
public final class SaveOcHandler extends CommandHandler<EqLstCommand> {
    // 饿汉式单例
    private static final SaveOcHandler INSTANCE = new SaveOcHandler();

    private SaveOcHandler() {
        super(EqLstCommand.class);
    }

    /**
     * 获取单例实例
     *
     * @return SaveOcHandler单例
     */
    public static SaveOcHandler getInstance() {
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
        return ItemXyOffsetParser.apply(parts, parentCmd);
    }
}
