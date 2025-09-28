package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;

/**
 * <p>
 * 具体的解析逻辑和MTF_CHECK一样，QCP显示用于多焦段机型检测，目前只有谷歌机型用到
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/03/11 10:43:08
 */
public final class MtfOffAxisCheckHandler {
    // 饿汉式单例
    private static final MtfOffAxisCheckHandler INSTANCE = new MtfOffAxisCheckHandler();

    // 组合MtfCheckHandler实例
    private final MtfCheckHandler mtfCheckHandler;

    private MtfOffAxisCheckHandler() {
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

    /**
     * 处理命令
     *
     * @param parts     命令的部分
     * @param parentCmd 前缀命令（可选）
     * @return 处理结果
     */
    public EqLstCommand handle(String[] parts, String parentCmd) {
        return mtfCheckHandler.handle(parts, parentCmd);
    }
}
