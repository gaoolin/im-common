package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

/**
 * <p>
 * 具体的解析逻辑和MTF_CHECK一样，QCP显示用于多焦段机型检测，目前只有谷歌机型用到
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/03/11 10:43:08
 */

/**
 * MTF离轴检查处理器
 */
public final class MtfOffAxisCheckHandler extends CommandHandler<EqLstCommand> implements AutoRegisteredHandler<EqLstCommand> {
    // 饿汉式单例
    public static final MtfOffAxisCheckHandler INSTANCE = new MtfOffAxisCheckHandler();

    // 组合MtfCheckHandler实例
    public final MtfCheckHandler mtfCheckHandler;

    public MtfOffAxisCheckHandler() {
        // 显式调用父类构造函数
        super(EqLstCommand.class);

        // 使用MtfCheckHandler的单例实例
        this.mtfCheckHandler = MtfCheckHandler.getInstance();
    }

    /**
     * 获取单例实例
     *
     * @return MtfOffAxisCheckHandler单例
     */
    public static MtfOffAxisCheckHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public EqLstCommand handle(String[] parts, String parentCmd) {
        return mtfCheckHandler.handle(parts, parentCmd);
    }

    @Override
    public CommandHandler<EqLstCommand> createInstance() {
        return getInstance();
    }
}
