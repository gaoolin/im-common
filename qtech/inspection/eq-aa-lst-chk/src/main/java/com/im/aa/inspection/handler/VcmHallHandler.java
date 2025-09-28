package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.parser.item.ItemVcmHallParser;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

/**
 * <p>
 * OIS机型VCM通电检查
 * Item 中的 HallAF、HallX、HallY 检查
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/03/27 08:41:11
 */
public final class VcmHallHandler extends CommandHandler<EqLstCommand> {
    // 饿汉式单例
    private static final VcmHallHandler INSTANCE = new VcmHallHandler();

    private VcmHallHandler() {
        super(EqLstCommand.class);
    }

    /**
     * 获取单例实例
     *
     * @return VcmHallHandler单例
     */
    public static VcmHallHandler getInstance() {
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
        return ItemVcmHallParser.apply(parts, parentCmd);
    }
}
