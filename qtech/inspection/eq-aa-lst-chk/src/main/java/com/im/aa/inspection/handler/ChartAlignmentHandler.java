package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.parser.item.ItemXyResParser;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

/**
 * <p>
 * ChartAlignment
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/05/28 11:20:49
 */
public final class ChartAlignmentHandler extends CommandHandler<EqLstCommand> {
    // 饿汉式单例
    private static final ChartAlignmentHandler INSTANCE = new ChartAlignmentHandler();

    private ChartAlignmentHandler() {
        super(EqLstCommand.class);
    }

    /**
     * 获取单例实例
     *
     * @return ChartAlignmentHandler单例
     */
    public static ChartAlignmentHandler getInstance() {
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
        return ItemXyResParser.apply(parts, parentCmd);
    }
}
