package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.parser.item.ItemLensFieldOfViewTestParser;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

/**
 * <p>
 * mtfCheck命令只可能作为单独的命令，不能作为其他命令的子命令
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/28 11:18:51
 */
public class MtfCheckHandler extends CommandHandler<EqLstCommand> implements AutoRegisteredHandler<EqLstCommand> {
    // 饿汉式单例
    public static final MtfCheckHandler INSTANCE = new MtfCheckHandler();

    public MtfCheckHandler() {
        super(EqLstCommand.class);
    }

    /**
     * 获取单例实例
     *
     * @return MtfCheckHandler单例
     */
    public static MtfCheckHandler getInstance() {
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
        return ItemLensFieldOfViewTestParser.apply(parts, parentCmd);
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
