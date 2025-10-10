package com.im.qtech.common.dto.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * AA List参数基础实体类
 * <p>
 * 该类作为设备参数点检的基础实体，具有以下特点：
 * - 可扩展性：支持通过继承扩展新的参数字段
 * - 通用性：适用于多种设备参数管理场景
 * - 易维护性：提供统一的参数重置和填充机制
 * - 兼容性：保持与现有代码的兼容性
 *
 * @author gaozhilin
 * @since 2024/11/27
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@MappedSuperclass
public class EqLstPOJO implements Serializable {
    private static final long serialVersionUID = 529L;

    // 基础设备参数
    private String module;
    private String aa1;
    private String aa2;
    private String aa3;
    private String backToPosition;
    private String blemish;

    // 2025-01-15 扩展参数
    private String blemish1;
    private String blemish2;
    private String chartAlignment;
    private String chartAlignment1;
    private String chartAlignment2;
    private String clampOnOff;
    private String delay;
    private String destroy;
    private String destroyStart;
    private String dispense;

    // 2025-01-15 扩展参数
    private String dispense1;
    private String dispense2;
    private String epoxyInspection;
    private String epoxyInspectionAuto;
    private String grab;

    // 2025-01-15 扩展参数
    private String grab1;
    private String grab2;

    // 2025-03-11 扩展参数
    private String grab3;
    private String gripperOpen;
    private String init;

    // 2025-01-15 扩展参数
    private String init1;
    private String init2;
    private String init3;
    private String lpBlemish;
    private String lpOc;
    private String lpOcCheck;
    private String lpOn;

    // 2025-03-11 扩展参数
    private String lpOn1;
    private String lpOnBlemish;
    private String lpOff;

    // 2025-03-11 扩展参数
    private String lpOff1;

    // 2025-01-16 lpOff0对于mybatis驼峰转换不友好，更改以下指令
    private String lpIntensity;
    private String moveToBlemishPos;
    private String mtfCheck;
    private String mtfCheck1;
    private String mtfCheck2;
    private String mtfCheck3;

    // 2025-03-11 多焦段MTF检查
    private String mtfOffAxisCheck1;
    private String mtfOffAxisCheck2;
    private String mtfOffAxisCheck3;
    private String mtfOffAxisCheck4;
    private String openCheck;

    // 2025-01-15 扩展参数
    private String openCheck1;
    private String openCheck2;
    private String openCheck3;

    // 2025-01-15 新增参数
    private String prToBond;

    // 台虹厂区的utXyzMove和recordPosition分开管控，因此新增 2025-01-15
    private String utXyzMove;
    private String recordPosition;
    private String reInit;
    private String saveOc;
    private String saveMtf;
    private String sensorReset;
    private String sfrCheck; // 2025-04-17

    private String sid;
    private String uvon;
    private String uvoff;

    // vcmHall 指标 2025-03-27
    private String vcmHall;
    private String vcmHall1;
    private String vcmHall2;

    @JsonProperty("yLevel")
    private String yLevel;

    // AA Item 指标
    private String aa1RoiCc;
    private String aa1RoiUl;
    private String aa1RoiUr;
    private String aa1RoiLl;
    private String aa1RoiLr;

    // 新增 2024-10-28
    private String aa1Target;
    private String aa1CcToCornerLimit;
    private String aa1CcToCornerLimitMin;
    private String aa1CornerScoreDifferenceRejectValue;
    private String aa1ZRef;
    private String aa1SrchStep;
    private String aa1GoldenGlueThicknessMin;
    private String aa1GoldenGlueThicknessMax;

    private String aa2RoiCc;
    private String aa2RoiUl;
    private String aa2RoiUr;
    private String aa2RoiLl;
    private String aa2RoiLr;

    // 新增 2024-10-28
    private String aa2Target;
    private String aa2CcToCornerLimit;
    private String aa2CcToCornerLimitMin;
    private String aa2CornerScoreDifferenceRejectValue;
    private String aa2ZRef;
    private String aa2SrchStep;
    private String aa2GoldenGlueThicknessMin;
    private String aa2GoldenGlueThicknessMax;

    private String aa3RoiCc;
    private String aa3RoiUl;
    private String aa3RoiUr;
    private String aa3RoiLl;
    private String aa3RoiLr;

    // 新增 2024-10-28
    private String aa3Target;
    private String aa3CcToCornerLimit;
    private String aa3CcToCornerLimitMin;
    private String aa3CornerScoreDifferenceRejectValue;
    private String aa3ZRef;
    private String aa3SrchStep;
    private String aa3GoldenGlueThicknessMin;
    private String aa3GoldenGlueThicknessMax;

    // mtfCheck Item 指标
    private String mtfCheckF;
    private String mtfCheck1F;
    private String mtfCheck2F;
    private String mtfCheck3F;

    // 2025-03-11 多焦段 mtfCheck
    private String mtfOffAxisCheck1F;
    private String mtfOffAxisCheck2F;
    private String mtfOffAxisCheck3F;
    private String mtfOffAxisCheck4F;

    // chartAlignment Item 指标
    private String chartAlignmentXResMin;
    private String chartAlignmentXResMax;
    private String chartAlignmentYResMin;
    private String chartAlignmentYResMax;

    private String chartAlignment1XResMin;
    private String chartAlignment1XResMax;
    private String chartAlignment1YResMin;
    private String chartAlignment1YResMax;

    private String chartAlignment2XResMin;
    private String chartAlignment2XResMax;
    private String chartAlignment2YResMin;
    private String chartAlignment2YResMax;

    // EpoxyInspection Auto Item 指标
    private String epoxyInspectionInterval;

    // vcmHall 指标 2025-03-27
    private String vcmHallHallAfMin;
    private String vcmHallHallAfMax;
    private String vcmHallHallXMin;
    private String vcmHallHallXMax;
    private String vcmHallHallYMin;
    private String vcmHallHallYMax;

    private String vcmHall1HallAfMin;
    private String vcmHall1HallAfMax;
    private String vcmHall1HallXMin;
    private String vcmHall1HallXMax;
    private String vcmHall1HallYMin;
    private String vcmHall1HallYMax;

    private String vcmHall2HallAfMin;
    private String vcmHall2HallAfMax;
    private String vcmHall2HallXMin;
    private String vcmHall2HallXMax;
    private String vcmHall2HallYMin;
    private String vcmHall2HallYMax;

    // RecordPosition 指标
    // 由于新增utXyzMove List命令，因此新增utXyzMoveVal作为参数管控值 2025-01-15
    private String utXyzMoveVal;
    // 由于新增utXyzMove List命令，因此新增recordPositionName作为参数管控值，原recordPositionXyzMove 被替换掉 2025-01-15
    private String recordPositionName;

    // OcCheck 指标 Save Oc
    private String saveOcXOffsetMin;
    private String saveOcXOffsetMax;
    private String saveOcYOffsetMin;
    private String saveOcYOffsetMax;

    // SaveMtf 指标
    private String saveMtfCcMin;
    private String saveMtfCcMax;
}
