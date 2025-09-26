package org.im.semiconductor.production.execution;

import java.io.Serializable;
import java.util.Objects;

/**
 * 生产环境上下文信息
 * <p>
 * 特性：
 * - 通用性：支持各种生产环境场景
 * - 规范性：遵循生产环境信息标准
 * - 专业性：提供专业的生产环境信息管理能力
 * - 灵活性：支持丰富的生产环境属性
 * - 可靠性：确保生产环境信息的完整性
 * - 安全性：防止环境信息非法操作
 * - 复用性：可被各种生产场景复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/09/25
 */
public class ProductionContext implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    /**
     * 公司名称 (Company Name)
     */
    private String company;

    /**
     * 厂区名称 (Site Name)
     */
    private String site;

    /**
     * 建筑物名称 (Building Name)
     */
    private String building;

    /**
     * 楼层 (Floor)
     */
    private String floor;

    /**
     * 工段名称 (Work Section Name)
     * 如: TEST(测试), COB(覆晶), FAB(前段), ASSY(组装), PACK(包装)
     */
    private String section;

    /**
     * 设备类型 (Equipment Type)
     */
    private String eqpType;

    /**
     * 产品类型/机型 (Product Type/Model)
     */
    private String product;

    /**
     * 工单号码 (Work Order Number)
     */
    private String workOrder;

    /**
     * 批次号码 (Lot Number)
     */
    private String lot;

    /**
     * 子批次号码 (Sub Lot Number)
     */
    private String subLot;

    /**
     * 操作员ID (Operator ID)
     */
    private String operatorId;

    /**
     * 操作员姓名 (Operator Name)
     */
    private String operator;

    /**
     * 班次 (Shift)
     * 如: A班, B班, C班 或 Day/Night Shift
     */
    private String shift;

    /**
     * 生产线 (Production Line)
     */
    private String line;

    /**
     * 工艺步骤 (Process Step)
     */
    private String step;

    /**
     * 参数群组 (Parameter Group)
     */
    private String paramGroup;

    /**
     * 质量等级 (Quality Grade)
     */
    private String quality;

    /**
     * 客户名称 (Customer Name)
     */
    private String customer;

    /**
     * 产品规格 (Product Specification)
     */
    private String spec;

    /**
     * 晶圆片号 (Wafer ID)
     */
    private String wafer;

    /**
     * 芯片位置 (Die Position)
     */
    private String die;

    /**
     * 测试程序名称 (Test Program Name)
     */
    private String testProgram;

    /**
     * 配方名称 (Recipe Name)
     */
    private String recipe;

    /**
     * 载具ID (Carrier ID)
     * 如: Tray(托盘), Tube(管子), Tape(卷带)
     */
    private String carrier;

    /**
     * 默认构造函数
     */
    public ProductionContext() {
    }

    /**
     * 带必要参数的构造函数
     *
     * @param company 公司名称
     * @param site    厂区名称
     * @param eqpType 设备类型
     */
    public ProductionContext(String company, String site, String eqpType) {
        this.company = company;
        this.site = site;
        this.eqpType = eqpType;
    }

    /**
     * 完整参数构造函数
     *
     * @param company   公司名称
     * @param site      厂区名称
     * @param building  建筑物名称
     * @param section   工段名称
     * @param eqpType   设备类型
     * @param product   产品类型
     * @param workOrder 工单号码
     */
    public ProductionContext(String company, String site, String building,
                             String section, String eqpType, String product, String workOrder) {
        this.company = company;
        this.site = site;
        this.building = building;
        this.section = section;
        this.eqpType = eqpType;
        this.product = product;
        this.workOrder = workOrder;
    }

    // Getters and Setters

    /**
     * 获取公司名称
     *
     * @return 公司名称
     */
    public String getCompany() {
        return company;
    }

    /**
     * 设置公司名称
     *
     * @param company 公司名称
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * 获取厂区名称
     *
     * @return 厂区名称
     */
    public String getSite() {
        return site;
    }

    /**
     * 设置厂区名称
     *
     * @param site 厂区名称
     */
    public void setSite(String site) {
        this.site = site;
    }

    /**
     * 获取建筑物名称
     *
     * @return 建筑物名称
     */
    public String getBuilding() {
        return building;
    }

    /**
     * 设置建筑物名称
     *
     * @param building 建筑物名称
     */
    public void setBuilding(String building) {
        this.building = building;
    }

    /**
     * 获取楼层
     *
     * @return 楼层
     */
    public String getFloor() {
        return floor;
    }

    /**
     * 设置楼层
     *
     * @param floor 楼层
     */
    public void setFloor(String floor) {
        this.floor = floor;
    }

    /**
     * 获取工段名称
     *
     * @return 工段名称
     */
    public String getSection() {
        return section;
    }

    /**
     * 设置工段名称
     *
     * @param section 工段名称
     */
    public void setSection(String section) {
        this.section = section;
    }

    /**
     * 获取设备类型
     *
     * @return 设备类型
     */
    public String getEqpType() {
        return eqpType;
    }

    /**
     * 设置设备类型
     *
     * @param eqpType 设备类型
     */
    public void setEqpType(String eqpType) {
        this.eqpType = eqpType;
    }

    /**
     * 获取产品类型
     *
     * @return 产品类型
     */
    public String getProduct() {
        return product;
    }

    /**
     * 设置产品类型
     *
     * @param product 产品类型
     */
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     * 获取工单号码
     *
     * @return 工单号码
     */
    public String getWorkOrder() {
        return workOrder;
    }

    /**
     * 设置工单号码
     *
     * @param workOrder 工单号码
     */
    public void setWorkOrder(String workOrder) {
        this.workOrder = workOrder;
    }

    /**
     * 获取批次号码
     *
     * @return 批次号码
     */
    public String getLot() {
        return lot;
    }

    /**
     * 设置批次号码
     *
     * @param lot 批次号码
     */
    public void setLot(String lot) {
        this.lot = lot;
    }

    /**
     * 获取子批次号码
     *
     * @return 子批次号码
     */
    public String getSubLot() {
        return subLot;
    }

    /**
     * 设置子批次号码
     *
     * @param subLot 子批次号码
     */
    public void setSubLot(String subLot) {
        this.subLot = subLot;
    }

    /**
     * 获取操作员ID
     *
     * @return 操作员ID
     */
    public String getOperatorId() {
        return operatorId;
    }

    /**
     * 设置操作员ID
     *
     * @param operatorId 操作员ID
     */
    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    /**
     * 获取操作员姓名
     *
     * @return 操作员姓名
     */
    public String getOperator() {
        return operator;
    }

    /**
     * 设置操作员姓名
     *
     * @param operator 操作员姓名
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * 获取班次
     *
     * @return 班次
     */
    public String getShift() {
        return shift;
    }

    /**
     * 设置班次
     *
     * @param shift 班次
     */
    public void setShift(String shift) {
        this.shift = shift;
    }

    /**
     * 获取生产线
     *
     * @return 生产线
     */
    public String getLine() {
        return line;
    }

    /**
     * 设置生产线
     *
     * @param line 生产线
     */
    public void setLine(String line) {
        this.line = line;
    }

    /**
     * 获取工艺步骤
     *
     * @return 工艺步骤
     */
    public String getStep() {
        return step;
    }

    /**
     * 设置工艺步骤
     *
     * @param step 工艺步骤
     */
    public void setStep(String step) {
        this.step = step;
    }

    /**
     * 获取参数群组
     *
     * @return 参数群组
     */
    public String getParamGroup() {
        return paramGroup;
    }

    /**
     * 设置参数群组
     *
     * @param paramGroup 参数群组
     */
    public void setParamGroup(String paramGroup) {
        this.paramGroup = paramGroup;
    }

    /**
     * 获取质量等级
     *
     * @return 质量等级
     */
    public String getQuality() {
        return quality;
    }

    /**
     * 设置质量等级
     *
     * @param quality 质量等级
     */
    public void setQuality(String quality) {
        this.quality = quality;
    }

    /**
     * 获取客户名称
     *
     * @return 客户名称
     */
    public String getCustomer() {
        return customer;
    }

    /**
     * 设置客户名称
     *
     * @param customer 客户名称
     */
    public void setCustomer(String customer) {
        this.customer = customer;
    }

    /**
     * 获取产品规格
     *
     * @return 产品规格
     */
    public String getSpec() {
        return spec;
    }

    /**
     * 设置产品规格
     *
     * @param spec 产品规格
     */
    public void setSpec(String spec) {
        this.spec = spec;
    }

    /**
     * 获取晶圆片号
     *
     * @return 晶圆片号
     */
    public String getWafer() {
        return wafer;
    }

    /**
     * 设置晶圆片号
     *
     * @param wafer 晶圆片号
     */
    public void setWafer(String wafer) {
        this.wafer = wafer;
    }

    /**
     * 获取芯片位置
     *
     * @return 芯片位置
     */
    public String getDie() {
        return die;
    }

    /**
     * 设置芯片位置
     *
     * @param die 芯片位置
     */
    public void setDie(String die) {
        this.die = die;
    }

    /**
     * 获取测试程序名称
     *
     * @return 测试程序名称
     */
    public String getTestProgram() {
        return testProgram;
    }

    /**
     * 设置测试程序名称
     *
     * @param testProgram 测试程序名称
     */
    public void setTestProgram(String testProgram) {
        this.testProgram = testProgram;
    }

    /**
     * 获取配方名称
     *
     * @return 配方名称
     */
    public String getRecipe() {
        return recipe;
    }

    /**
     * 设置配方名称
     *
     * @param recipe 配方名称
     */
    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    /**
     * 获取载具ID
     *
     * @return 载具ID
     */
    public String getCarrier() {
        return carrier;
    }

    /**
     * 设置载具ID
     *
     * @param carrier 载具ID
     */
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    /**
     * 检查生产环境信息是否完整
     *
     * @return 生产环境信息是否完整
     */
    public boolean isComplete() {
        return company != null && !company.isEmpty() &&
                site != null && !site.isEmpty() &&
                eqpType != null && !eqpType.isEmpty();
    }

    /**
     * 获取完整的位置信息
     *
     * @return 完整位置信息字符串
     */
    public String getFullLocation() {
        StringBuilder location = new StringBuilder();
        if (company != null && !company.isEmpty()) {
            location.append(company);
        }
        if (site != null && !site.isEmpty()) {
            if (location.length() > 0) location.append("-");
            location.append(site);
        }
        if (building != null && !building.isEmpty()) {
            if (location.length() > 0) location.append("-");
            location.append(building);
        }
        if (floor != null && !floor.isEmpty()) {
            if (location.length() > 0) location.append("-");
            location.append(floor);
        }
        if (section != null && !section.isEmpty()) {
            if (location.length() > 0) location.append("-");
            location.append(section);
        }
        return location.toString();
    }

    /**
     * 获取完整的设备标识信息
     *
     * @return 完整设备标识信息字符串
     */
    public String getFullEquipmentId() {
        StringBuilder fullId = new StringBuilder();
        if (eqpType != null && !eqpType.isEmpty()) {
            fullId.append(eqpType);
        }
        if (product != null && !product.isEmpty()) {
            if (fullId.length() > 0) fullId.append("-");
            fullId.append(product);
        }
        return fullId.toString();
    }

    /**
     * 克隆生产环境信息对象
     *
     * @return 生产环境信息对象副本
     */
    @Override
    public ProductionContext clone() {
        try {
            return (ProductionContext) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("克隆生产环境信息对象失败", e);
        }
    }

    /**
     * 比较生产环境信息对象是否相等
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductionContext that = (ProductionContext) o;
        return Objects.equals(company, that.company) &&
                Objects.equals(site, that.site) &&
                Objects.equals(building, that.building) &&
                Objects.equals(floor, that.floor) &&
                Objects.equals(section, that.section) &&
                Objects.equals(eqpType, that.eqpType) &&
                Objects.equals(product, that.product) &&
                Objects.equals(workOrder, that.workOrder) &&
                Objects.equals(lot, that.lot) &&
                Objects.equals(subLot, that.subLot) &&
                Objects.equals(operatorId, that.operatorId) &&
                Objects.equals(operator, that.operator) &&
                Objects.equals(shift, that.shift) &&
                Objects.equals(line, that.line) &&
                Objects.equals(step, that.step) &&
                Objects.equals(paramGroup, that.paramGroup) &&
                Objects.equals(quality, that.quality) &&
                Objects.equals(customer, that.customer) &&
                Objects.equals(spec, that.spec) &&
                Objects.equals(wafer, that.wafer) &&
                Objects.equals(die, that.die) &&
                Objects.equals(testProgram, that.testProgram) &&
                Objects.equals(recipe, that.recipe) &&
                Objects.equals(carrier, that.carrier);
    }

    /**
     * 计算生产环境信息对象的哈希值
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(company, site, building, floor, section,
                eqpType, product, workOrder, lot, subLot, operatorId, operator,
                shift, line, step, paramGroup, quality, customer,
                spec, wafer, die, testProgram, recipe, carrier);
    }

    /**
     * 返回生产环境信息对象的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "ProductionContext{" +
                "company='" + company + '\'' +
                ", site='" + site + '\'' +
                ", building='" + building + '\'' +
                ", floor='" + floor + '\'' +
                ", section='" + section + '\'' +
                ", eqpType='" + eqpType + '\'' +
                ", product='" + product + '\'' +
                ", workOrder='" + workOrder + '\'' +
                ", lot='" + lot + '\'' +
                ", subLot='" + subLot + '\'' +
                ", operatorId='" + operatorId + '\'' +
                ", operator='" + operator + '\'' +
                ", shift='" + shift + '\'' +
                ", line='" + line + '\'' +
                ", step='" + step + '\'' +
                ", paramGroup='" + paramGroup + '\'' +
                ", quality='" + quality + '\'' +
                ", customer='" + customer + '\'' +
                ", spec='" + spec + '\'' +
                ", wafer='" + wafer + '\'' +
                ", die='" + die + '\'' +
                ", testProgram='" + testProgram + '\'' +
                ", recipe='" + recipe + '\'' +
                ", carrier='" + carrier + '\'' +
                '}';
    }
}
