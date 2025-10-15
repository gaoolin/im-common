package com.im.aa.inspection.context;

import org.im.semiconductor.common.parameter.core.DefaultParameterInspection;
import org.im.semiconductor.equipment.metrics.EqpCtx;

import java.util.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/28
 */
public class EqLstInspectionResult extends DefaultParameterInspection implements EqpCtx {
    private String company;     // 公司
    private String site;        // 厂区
    private String workshop;    // 车间
    private String section;     // 工段
    private String deviceId;    // 设备ID
    private String deviceName;  // 设备名称
    private String deviceType;  // 设备类型
    private String line;        // 线体
    private String module;      // 模组

    // 添加无参构造函数供 MyBatis 使用
    public EqLstInspectionResult() {
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    /**
     * 获取公司唯一标识符
     * <p>
     * 公司的全局唯一标识
     * </p>
     *
     * @return 公司ID，如果未设置则返回null
     */
    @Override
    public String getCompanyId() {
        return null;
    }

    @Override
    public String getCompany() {
        return Optional.ofNullable(company).orElse("");
    }

    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * 获取厂区唯一标识符
     * <p>
     * 厂区的全局唯一标识
     * </p>
     *
     * @return 厂区ID，如果未设置则返回null
     */
    @Override
    public String getSiteId() {
        return null;
    }

    @Override
    public String getSite() {
        return Optional.ofNullable(site).orElse("");
    }

    public void setSite(String site) {
        this.site = site;
    }

    /**
     * 获取车间唯一标识符
     * <p>
     * 车间的全局唯一标识
     * </p>
     *
     * @return 车间ID，如果未设置则返回null
     */
    @Override
    public String getWorkshopId() {
        return null;
    }

    @Override
    public String getWorkshop() {
        return Optional.ofNullable(workshop).orElse("");
    }

    public void setWorkshop(String workshop) {
        this.workshop = workshop;
    }

    /**
     * 获取建筑物唯一标识符
     * <p>
     * 建筑物的全局唯一标识
     * </p>
     *
     * @return 建筑物ID，如果未设置则返回null
     */
    @Override
    public String getBuildingId() {
        return null;
    }

    /**
     * 获取建筑物名称
     * <p>
     * 建筑物的可读名称
     * </p>
     *
     * @return 建筑物名称，如果未设置则返回null
     */
    @Override
    public String getBuilding() {
        return null;
    }

    /**
     * 获取楼层信息
     * <p>
     * 所在楼层标识
     * </p>
     *
     * @return 楼层，如果未设置则返回null
     */
    @Override
    public String getFloor() {
        return null;
    }

    /**
     * 获取工段唯一标识符
     * <p>
     * 工段的全局唯一标识
     * </p>
     *
     * @return 工段ID，如果未设置则返回null
     */
    @Override
    public String getSectionId() {
        return null;
    }

    @Override
    public String getSection() {
        return Optional.ofNullable(section).orElse("");
    }

    public void setSection(String section) {
        this.section = section;
    }

    /**
     * 获取完整位置信息
     * <p>
     * 包含从公司到工段的完整位置路径描述
     * </p>
     *
     * @return 完整位置信息字符串
     */
    @Override
    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        if (company != null && !company.isEmpty()) sb.append(company);
        if (site != null && !site.isEmpty()) sb.append("/").append(site);
        if (workshop != null && !workshop.isEmpty()) sb.append("/").append(workshop);
        if (section != null && !section.isEmpty()) sb.append("/").append(section);
        return sb.toString();
    }

    /**
     * 获取位置编码
     * <p>
     * 标准化的位置编码标识
     * </p>
     *
     * @return 位置编码，如果未设置则返回null
     */
    @Override
    public String getLocationCode() {
        return null;
    }

    /**
     * 验证位置信息的完整性
     * <p>
     * 检查必需字段是否都已正确设置
     * </p>
     *
     * @return 如果位置信息有效返回true，否则返回false
     */
    @Override
    public boolean isValid() {
        return hasRequiredFields();
    }

    /**
     * 检查是否包含必需字段
     *
     * @return 如果包含必需字段返回true，否则返回false
     */
    @Override
    public boolean hasRequiredFields() {
        return company != null && !company.isEmpty()
                && site != null && !site.isEmpty()
                && workshop != null && !workshop.isEmpty()
                && section != null && !section.isEmpty();
    }

    /**
     * 获取验证错误信息列表
     *
     * @return 验证错误信息列表，如果没有错误则返回空列表
     */
    @Override
    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        if (company == null || company.isEmpty()) errors.add("公司不能为空");
        if (site == null || site.isEmpty()) errors.add("厂区不能为空");
        if (workshop == null || workshop.isEmpty()) errors.add("车间不能为空");
        if (section == null || section.isEmpty()) errors.add("工段不能为空");
        return errors;
    }

    /**
     * 转换为JSON字符串表示
     *
     * @return 位置信息的JSON格式字符串
     */
    @Override
    public String toJson() {
        return "{"
                + "\"company\":\"" + (company == null ? "" : company) + "\","
                + "\"site\":\"" + (site == null ? "" : site) + "\","
                + "\"workshop\":\"" + (workshop == null ? "" : workshop) + "\","
                + "\"section\":\"" + (section == null ? "" : section) + "\""
                + "}";
    }

    /**
     * 转换为键值对映射
     *
     * @return 包含位置信息的Map对象
     */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("company", company);
        map.put("site", site);
        map.put("workshop", workshop);
        map.put("section", section);
        return map;
    }

    /**
     * 获取对象的唯一标识符
     *
     * @return 唯一标识符字符串
     */
    @Override
    public String getUniqueIdentifier() {
        return Integer.toString(Objects.hash(company, site, workshop, section));
    }


    @Override
    public String getDeviceId() {
        return Optional.ofNullable(deviceId).orElse("");
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String getDeviceName() {
        return Optional.ofNullable(deviceName).orElse("");
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String getDeviceType() {
        return Optional.ofNullable(deviceType).orElse("");
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public String getLine() {
        return Optional.ofNullable(line).orElse("");
    }

    public void setLine(String line) {
        this.line = line;
    }

    /**
     * 获取工艺步骤
     * <p>
     * 设备当前执行的工艺步骤标识
     * </p>
     *
     * @return 工艺步骤，如果未设置则返回null
     */
    @Override
    public String getStep() {
        return null;
    }

    /**
     * 获取配方名称
     * <p>
     * 设备当前使用的工艺配方名称
     * </p>
     *
     * @return 配方名称，如果未设置则返回null
     */
    @Override
    public String getRecipe() {
        return null;
    }

    /**
     * 获取参数群组
     * <p>
     * 设备使用的参数群组标识
     * </p>
     *
     * @return 参数群组，如果未设置则返回null
     */
    @Override
    public String getParamGroup() {
        return null;
    }

    /**
     * 获取设备状态
     * <p>
     * 设备的当前运行状态，如运行、停止、维护等
     * </p>
     *
     * @return 设备状态，如果未设置则返回null
     */
    @Override
    public String getDeviceStatus() {
        return null;
    }

    /**
     * 获取设备型号
     * <p>
     * 设备的具体型号信息
     * </p>
     *
     * @return 设备型号，如果未设置则返回null
     */
    @Override
    public String getModelNumber() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EqLstInspectionResult)) return false;
        EqLstInspectionResult that = (EqLstInspectionResult) o;
        return Objects.equals(company, that.company)
                && Objects.equals(site, that.site)
                && Objects.equals(workshop, that.workshop)
                && Objects.equals(section, that.section);
    }

    @Override
    public int hashCode() {
        return Objects.hash(company, site, workshop, section);
    }

    @Override
    public String toString() {
        return "EqLstInspectionResult{" +
                "company='" + company + '\'' +
                ", site='" + site + '\'' +
                ", workshop='" + workshop + '\'' +
                ", section='" + section + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", line='" + line + '\'' +
                '}';
    }
}
