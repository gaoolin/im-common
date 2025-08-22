package com.qtech.im.semiconductor.equipment.qtech;

import com.qtech.im.semiconductor.equipment.parameter.command.AbstractDeviceParameter;
import com.qtech.im.semiconductor.equipment.parameter.command.ParameterType;
import com.qtech.im.semiconductor.equipment.parameter.comparator.BoundsLoader;
import org.apache.commons.lang3.StringUtils;
/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/22 11:33:45
 * desc   :
 */

/**
 * 设备命令参数实现类 - 继承自抽象基类
 */
public class ImAaListCommand extends AbstractDeviceParameter {
    private String integration;
    private Integer num;
    private String prefixCommand;
    private String command;
    private String subsystem;
    private BoundsLoader boundsLoader;

    public ImAaListCommand() {
        super();
    }

    public ImAaListCommand(String integration, Integer num, String prefixCommand, String command, String subsystem, BoundsLoader boundsLoader) {
        this.integration = integration;
        this.num = num;
        this.prefixCommand = prefixCommand;
        this.command = command;
        this.subsystem = subsystem;
        this.boundsLoader = boundsLoader;

        // 初始化父类属性
        initialize();
    }

    @Override
    public String getParameterName() {
        return getIntegration();
    }

    @Override
    public String getParameterDescription() {
        return "设备命令参数: " + getIntegration();
    }

    @Override
    public String getParameterUnit() {
        return "command";
    }

    @Override
    public ParameterType getParameterType() {
        return ParameterType.STRING;
    }

    @Override
    public Object getParameterValue() {
        return getIntegration();
    }

    @Override
    public void setParameterValue(Object parameterValue) {
        if (parameterValue instanceof String) {
            this.integration = (String) parameterValue;
        }
    }

    @Override
    public String getPath() {
        return getIntegration();
    }

    @Override
    public String getFullPath() {
        return getIntegration();
    }

    @Override
    public boolean isValid() {
        return StringUtils.isNotBlank(getIntegration());
    }

    // 保留原有的getIntegration方法
    public String getIntegration() {
        if (StringUtils.isBlank(integration)) {
            StringBuilder sb = new StringBuilder();
            boolean hasContent = false;

            if (StringUtils.isNotBlank(prefixCommand)) {
                sb.append(prefixCommand);
                hasContent = true;
            }
            if (StringUtils.isNotBlank(command)) {
                if (hasContent) sb.append("_");
                sb.append(command);
                hasContent = true;
            }
            if (StringUtils.isNotBlank(subsystem)) {
                if (hasContent) sb.append("_");
                sb.append(subsystem);
            }
            return sb.toString();
        }
        return integration;
    }

    // 其他getter和setter方法...
}
