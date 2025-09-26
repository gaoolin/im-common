package com.im.qtech.chk.model;

import org.im.semiconductor.equipment.metrics.EquipmentContextInfo;

/**
 * 设备参数模型
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/09/25
 */
public class EqpContextInfoImpl implements EquipmentContextInfo {
    private String company;
    private String site;
    private String building;
    private String floor;
    private String section;
    private String deviceId;
    private String deviceName;
    private String eqpType;
    private String line;
    private String step;
    private String recipe;
    private String paramGroup;

    @Override
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Override
    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    /**
     * 车间
     */
    @Override
    public String getWorkshop() {
        return null;
    }

    @Override
    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    @Override
    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    @Override
    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public String getFullLocation() {
        return null;
    }

    @Override
    public String getDeviceId() {
        return null;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String getDeviceName() {
        return null;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String getEqpType() {
        return eqpType;
    }

    public void setEqpType(String eqpType) {
        this.eqpType = eqpType;
    }

    @Override
    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    @Override
    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    @Override
    public String getParamGroup() {
        return paramGroup;
    }

    public void setParamGroup(String paramGroup) {
        this.paramGroup = paramGroup;
    }
}
