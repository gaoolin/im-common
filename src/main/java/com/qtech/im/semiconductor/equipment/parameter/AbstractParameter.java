package com.qtech.im.semiconductor.equipment.parameter;

import com.qtech.im.common.security.AccessLevel;
import com.qtech.im.semiconductor.equipment.parameter.mgr.ParameterRange;
import com.qtech.im.semiconductor.equipment.parameter.mgr.ParameterStatus;
import com.qtech.im.semiconductor.equipment.parameter.mgr.ParameterType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备参数抽象基类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/22 11:28:11
 */
public abstract class AbstractParameter implements ParameterInterface {
    private String deviceId;
    private String parameterName;
    private Object parameterValue;
    private ParameterType parameterType;
    private String parameterDescription;
    private String parameterUnit;
    private ParameterRange parameterRange;
    private ParameterStatus parameterStatus;
    private Map<String, Object> parameterConfig;
    private String uniqueId;
    private String version;
    private long createTime;
    private long updateTime;
    private boolean readOnly;
    private AccessLevel accessLevel;
    private List<String> tags;
    private Map<String, Object> metadata;
    private ParameterInterface parentParameter;
    private List<ParameterInterface> childParameters;

    public AbstractParameter() {
        this.parameterConfig = new HashMap<>();
        this.tags = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.childParameters = new ArrayList<>();
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
        this.accessLevel = AccessLevel.READ_WRITE;
        this.readOnly = false;
        this.version = "1.0";
    }

    // 实现接口方法
    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public Object getParameterValue() {
        return parameterValue;
    }

    @Override
    public void setParameterValue(Object parameterValue) {
        this.parameterValue = parameterValue;
        this.updateTime = System.currentTimeMillis();
    }

    @Override
    public ParameterType getParameterType() {
        return parameterType;
    }

    @Override
    public void setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
    }

    @Override
    public String getParameterDescription() {
        return parameterDescription;
    }

    @Override
    public void setParameterDescription(String parameterDescription) {
        this.parameterDescription = parameterDescription;
    }

    @Override
    public String getParameterUnit() {
        return parameterUnit;
    }

    @Override
    public void setParameterUnit(String parameterUnit) {
        this.parameterUnit = parameterUnit;
    }

    @Override
    public ParameterRange getParameterRange() {
        return parameterRange;
    }

    @Override
    public void setParameterRange(ParameterRange parameterRange) {
        this.parameterRange = parameterRange;
    }

    @Override
    public ParameterStatus getParameterStatus() {
        return parameterStatus;
    }

    @Override
    public void setParameterStatus(ParameterStatus parameterStatus) {
        this.parameterStatus = parameterStatus;
    }

    @Override
    public Map<String, Object> getParameterConfig() {
        return parameterConfig;
    }

    @Override
    public void setParameterConfig(Map<String, Object> parameterConfig) {
        this.parameterConfig = parameterConfig;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    @Override
    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    @Override
    public void removeTag(String tag) {
        tags.remove(tag);
    }

    @Override
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public ParameterInterface getParentParameter() {
        return parentParameter;
    }

    @Override
    public void setParentParameter(ParameterInterface parentParameter) {
        this.parentParameter = parentParameter;
    }

    @Override
    public List<ParameterInterface> getChildParameters() {
        return childParameters;
    }

    @Override
    public void addChildParameter(ParameterInterface childParameter) {
        if (!childParameters.contains(childParameter)) {
            childParameters.add(childParameter);
            childParameter.setParentParameter(this);
        }
    }

    @Override
    public void removeChildParameter(ParameterInterface childParameter) {
        childParameters.remove(childParameter);
        if (childParameter.getParentParameter() == this) {
            childParameter.setParentParameter(null);
        }
    }

    @Override
    public boolean hasChildParameter(ParameterInterface childParameter) {
        return childParameters.contains(childParameter);
    }

    @Override
    public String getPath() {
        return parameterName;
    }

    @Override
    public int getDepth() {
        if (parentParameter == null) {
            return 0;
        }
        return parentParameter.getDepth() + 1;
    }

    @Override
    public String getFullPath() {
        if (parentParameter == null) {
            return parameterName;
        }
        return parentParameter.getFullPath() + "." + parameterName;
    }

    @Override
    public Object getAttribute(String key) {
        return metadata.get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        metadata.put(key, value);
    }

    @Override
    public boolean isInitialized() {
        return parameterName != null && parameterType != null;
    }

    @Override
    public void initialize() {
        if (parameterName == null) {
            parameterName = "unknown";
        }
        if (parameterType == null) {
            parameterType = ParameterType.STRING;
        }
    }

    @Override
    public ParameterInterface clone() {
        try {
            AbstractParameter cloned = (AbstractParameter) super.clone();
            cloned.parameterConfig = new HashMap<>(parameterConfig);
            cloned.tags = new ArrayList<>(tags);
            cloned.metadata = new HashMap<>(metadata);
            cloned.childParameters = new ArrayList<>();
            for (ParameterInterface child : childParameters) {
                cloned.addChildParameter(child.clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone device parameter", e);
        }
    }

    @Override
    public boolean isValid() {
        if (parameterValue == null) {
            return false;
        }

        if (parameterRange != null) {
            if (parameterValue instanceof Number) {
                double value = ((Number) parameterValue).doubleValue();
                return parameterRange.contains(value);
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "AbstractParameter{" +
                "deviceId='" + deviceId + '\'' +
                ", parameterName='" + parameterName + '\'' +
                ", parameterValue=" + parameterValue +
                ", parameterType=" + parameterType +
                ", parameterDescription='" + parameterDescription + '\'' +
                ", parameterUnit='" + parameterUnit + '\'' +
                ", parameterStatus=" + parameterStatus +
                ", version='" + version + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", readOnly=" + readOnly +
                ", accessLevel=" + accessLevel +
                ", tags=" + tags +
                ", metadata=" + metadata +
                ", parentParameter=" + parentParameter +
                ", childParameters=" + childParameters +
                '}';
    }
}
