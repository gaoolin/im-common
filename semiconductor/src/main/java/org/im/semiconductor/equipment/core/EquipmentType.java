package org.im.semiconductor.equipment.core;

/**
 * 设备类型枚举
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public enum EquipmentType {
    // 前段工艺设备 (Front-End Process Equipment)
    WAFER_CLEANING("晶圆清洗设备"),           // Wafer Cleaning Equipment
    OXIDATION_DEPOSITION("氧化沉积设备"),     // Oxidation/Diffusion Equipment
    LITHOGRAPHY("光刻设备"),                 // Lithography Equipment
    ETCHING("刻蚀设备"),                     // Etching Equipment
    ION_IMPLANTATION("离子注入设备"),         // Ion Implantation Equipment
    CMP("化学机械抛光设备"),                 // Chemical Mechanical Polishing

    // 后段工艺设备 (Back-End Process Equipment)
    DIE_ATTACH("芯片贴装设备"),              // Die Attach Equipment
    WIRE_BONDING("引线键合设备"),            // Wire Bonding Equipment
    FLIP_CHIP("倒装芯片设备"),               // Flip Chip Equipment
    PROBE("晶圆测试设备"),                   // Wafer Probe Equipment

    // COB设备 (Chip on Board Equipment)
    COB_BONDING("COB邦定设备"),              // COB Bonding Equipment
    COB_ASSEMBLY("COB组装设备"),             // COB Assembly Equipment

    // 测试设备 (Testing Equipment)
    SORTER("分选机"),                        // Sorter/Test Handler
    TESTER("测试机"),                        // IC Tester
    BURN_IN("老化测试设备"),                 // Burn-inspection Equipment
    ATE("自动测试设备"),                     // Automatic Test Equipment

    // 封装设备 (Packaging Equipment)
    MOLDING("塑封设备"),                     // Molding Equipment
    TRIM_FORM("切筋成型设备"),               // Trim & Form Equipment
    MARKING("打印标记设备"),                 // Marking Equipment
    PLATING("电镀设备"),                     // Plating Equipment

    // 辅助设备 (Support Equipment)
    CLEANING("清洗设备"),                    // Cleaning Equipment
    DRYING("干燥设备"),                      // Drying Equipment
    INSPECTION("检测设备"),                  // Inspection Equipment
    SORTING("分拣设备"),                     // Sorting Equipment

    // 搬运存储设备 (Material Handling Equipment)
    ROBOT("机械手臂"),                       // Robot Equipment
    CONVEYOR("传送带设备"),                  // Conveyor Equipment
    STORAGE("存储设备"),                     // Storage Equipment

    // 其他设备 (Other Equipment)
    UTILITY("辅助设施设备"),                 // Utility Equipment
    MONITORING("监控测量设备"),              // Monitoring Equipment
    QUALITY_CONTROL("质量控制设备");         // Quality Control Equipment

    private final String description;

    EquipmentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}