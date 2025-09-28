package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.parser.item.ItemDelayParser;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

/**
 * <p>
 * 这项目前没有管控要求，是光源板移动到产品上方 亮度14 延时500意思
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/09/20 11:52:53
 */
public final class LpOcHandler extends CommandHandler<EqLstCommand> {
    // 饿汉式单例
    private static final LpOcHandler INSTANCE = new LpOcHandler();

    private LpOcHandler() {
        super(EqLstCommand.class);
    }

    /**
     * 获取单例实例
     *
     * @return LpOcHandler单例
     */
    public static LpOcHandler getInstance() {
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
        return ItemDelayParser.apply(parts, parentCmd);
    }
}
