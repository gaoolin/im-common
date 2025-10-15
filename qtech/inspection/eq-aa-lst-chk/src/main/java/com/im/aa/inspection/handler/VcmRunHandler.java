package com.im.aa.inspection.handler;

import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.parser.item.ItemCcParser;
import org.im.semiconductor.common.handler.cmd.CommandHandler;

/**
 * <p>
 * VcmRun 好像在采集上来的数据中并没有找到参数
 * VcmRun:相同的list名称
 * 1. Vcm_Hall
 * 2. Vcm_Hall2
 * 3. Vcm_Check(除外，它的参数值是 Cc值)
 * 4. Vcm_Check_650
 * 5. Vcm_MoveAF
 * 6. MoveAF_Z_Check
 * 7. VCMPowerOffCheck
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/28 11:19:56
 */
public final class VcmRunHandler extends CommandHandler<EqLstCommand> implements AutoRegisteredHandler<EqLstCommand> {
    // 饿汉式单例
    public static final VcmRunHandler INSTANCE = new VcmRunHandler();

    public VcmRunHandler() {
        super(EqLstCommand.class);
    }

    /**
     * 获取单例实例
     *
     * @return VcmRunHandler单例
     */
    public static VcmRunHandler getInstance() {
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
        return ItemCcParser.apply(parts, parentCmd);
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
