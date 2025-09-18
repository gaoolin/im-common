package com.qtech.im.constant;

import java.util.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/05/20 10:19:44
 */

public class EqLstChk {
    /*
     * 需要点检是否开启的属性名称
     * 即所有的List项
     */
    public static final List<String> PROPERTIES_TO_COMPARE = Arrays.asList(
            "prodType",
            "aa1",
            "aa2",
            "aa3",
            "backToPosition",
            "blemish",
            "blemish1",
            "blemish2",
            "clampOnOff",
            "chartAlignment",
            "chartAlignment1",
            "chartAlignment2",
            "delay",
            "destroy",
            "destroyStart",
            "dispense",
            "dispense1",
            "dispense2",
            "epoxyInspection",
            "epoxyInspectionAuto",
            "grab",
            "grab1",
            "grab2",
            "grab3",
            "gripperOpen",
            "init",
            "init1",
            "init2",
            "init3",
            "lpBlemish",
            "lpOc",
            "lpOnBlemish",
            "lpOcCheck",
            "lpOn",
            "lpOn1",
            "lpOff",
            "lpOff1",
            "lpIntensity",
            "moveToBlemishPos",
            "mtfCheck",
            "mtfCheck1",
            "mtfCheck2",
            "mtfCheck3",
            "mtfOffAxisCheck1",  // 2025-03-11
            "mtfOffAxisCheck2",
            "mtfOffAxisCheck3",
            "mtfOffAxisCheck4",
            "openCheck",
            "openCheck1",
            "openCheck2",
            "openCheck3",
            "prToBond",
            "recordPosition",
            "reInit",
            "saveOc",
            "saveMtf",
            "sensorReset",
            "sfrCheck",  // 2025-04-17
            "sid",
            "utXyzMove",  // 2025-01-15
            "uvon",
            "uvoff",
            "vcmHall",
            "vcmHall1",
            "vcmHall2",
            "yLevel"
    );

    /*
     * 需要解析对应List项的Item参数值并比对的List项目（不是所有List的Item参数都需要解析，也不是都需要比对和反控）
     * 即List对应的Item项中有哪些参数需要解析，哪些参数需要比对，哪些参数需要反控
     * 需要增减List参数的管控，则可直接在下列列表中添加或删除
     *
     * 此List的名称应和实际List程序的名称一致，而不是使用驼峰名称
     *
     * 此处调整后，还需要调整 CommandHandlerMapper 类 KEY_MAP
     */
    public static final Set<String> CONTROL_LIST_SET = Collections.unmodifiableSet(
            new HashSet<>(
                    Arrays.asList(
                            "AA1",
                            "AA2",
                            "AA3",
                            "ChartAlignment",
                            "ChartAlignment1",
                            "ChartAlignment2",
                            "EpoxyInspection_Auto",
                            "MTF_Check",
                            "MTF_Check1",
                            "MTF_Check2",
                            "MTF_Check3",
                            "MTFOffAxisCheck1",  // 2025-03-11
                            "MTFOffAxisCheck2",
                            "MTFOffAxisCheck3",
                            "MTFOffAxisCheck4",
                            "VCM_Hall",  // 2025-03-27
                            "VCM_Hall1",
                            "VCM_Hall2",
                            "RecordPosition",
                            "Save_MTF"
                    )
            )
    );

    /*
     * 需要计算值的Item项名称
     * 上面 CONTROL_LIST_SET 数组中List对应的Item项中，具体command或者subCommand的value
     * 上面的 CONTROL_LIST_SET 数组发生了增减，下面的数组也要对应的增减相关command或者subCommand
     */
    public static final List<String> PROPERTIES_TO_COMPUTE = Arrays.asList(
            "aa1RoiCc", "aa1RoiUl", "aa1RoiUr", "aa1RoiLl", "aa1RoiLr", "aa1CcToCornerLimit", "aa1CcToCornerLimitMin", "aa1CornerScoreDifferenceRejectValue", "aa1SrchStep", "aa1GoldenGlueThicknessMin", "aa1GoldenGlueThicknessMax",
            "aa2RoiCc", "aa2RoiUl", "aa2RoiUr", "aa2RoiLl", "aa2RoiLr", "aa2CcToCornerLimit", "aa2CcToCornerLimitMin", "aa2CornerScoreDifferenceRejectValue", "aa2SrchStep", "aa2GoldenGlueThicknessMin", "aa2GoldenGlueThicknessMax",
            "aa3RoiCc", "aa3RoiUl", "aa3RoiUr", "aa3RoiLl", "aa3RoiLr", "aa3CcToCornerLimit", "aa3CcToCornerLimitMin", "aa3CornerScoreDifferenceRejectValue", "aa3SrchStep", "aa3GoldenGlueThicknessMin", "aa3GoldenGlueThicknessMax",
            "mtfCheckF", "mtfCheck1F", "mtfCheck2F", "mtfCheck3F",
            "mtfOffAxisCheck1", "mtfOffAxisCheck2", "mtfOffAxisCheck3", "mtfOffAxisCheck4",  // 2025-03-11
            "chartAlignmentXResMin", "chartAlignmentXResMax", "chartAlignmentYResMin", "chartAlignmentYResMax",
            "chartAlignment1XResMin", "chartAlignment1XResMax", "chartAlignment1YResMin", "chartAlignment1YResMax",
            "chartAlignment2XResMin", "chartAlignment2XResMax", "chartAlignment2YResMin", "chartAlignment2YResMax",
            "epoxyInspectionInterval",
            // "saveOcXOffsetMin", "saveOcXOffsetMax", "saveOcYOffsetMin", "saveOcYOffsetMax",  // 2025-02-24 不在管控saveOc的参数值
            "saveMtfCcMin", "saveMtfCcMax",

            // ... Item 参数中用于比较的属性名称(其值为字符串类型)
            // "utXyzMoveVal", // 2025-02-13 上抬下拉不一定为数值，可能数值后代字母  2025-02-24 不在管控utXyzMoveVal的参数值
            "recordPositionName",
            "aa1Target", "aa2Target", "aa3Target",
            "aa1ZRef", "aa2ZRef", "aa3ZRef",
            "vcmHallHallAfMin", "vcmHallHallAfMax", "vcmHallHallXMin", "vcmHallHallXMax", "vcmHallHallYMin", "vcmHallHallYMax",
            "vcmHall1HallAfMin", "vcmHall1HallAfMax", "vcmHall1HallXMin", "vcmHall1HallXMax", "vcmHall1HallYMin", "vcmHall1HallYMax",
            "vcmHall2HallAfMin", "vcmHall2HallAfMax", "vcmHall2HallXMin", "vcmHall2HallXMax", "vcmHall2HallYMin", "vcmHall2HallYMax"
    );
}