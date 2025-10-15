package com.im.aa.inspection.serde;

import com.google.protobuf.Message;
import com.im.aa.inspection.proto.EqLstProto;
import org.im.common.serde.protobuf.ProtoConverter;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/09
 */
public class EqLstProtoConverter implements ProtoConverter<com.im.qtech.common.dto.param.EqLstPOJO> {
    @Override
    public com.google.protobuf.Message toProto(com.im.qtech.common.dto.param.EqLstPOJO obj) {
        if (obj == null) return null;

        EqLstProto.EqLstPOJO.Builder builder = EqLstProto.EqLstPOJO.newBuilder();

        // 基础设备参数
        if (obj.getModule() != null) builder.setModule(obj.getModule());
        if (obj.getAa1() != null) builder.setAa1(obj.getAa1());
        if (obj.getAa2() != null) builder.setAa2(obj.getAa2());
        if (obj.getAa3() != null) builder.setAa3(obj.getAa3());
        if (obj.getBackToPosition() != null) builder.setBackToPosition(obj.getBackToPosition());
        if (obj.getBlemish() != null) builder.setBlemish(obj.getBlemish());

        // 2025-01-15 扩展参数
        if (obj.getBlemish1() != null) builder.setBlemish1(obj.getBlemish1());
        if (obj.getBlemish2() != null) builder.setBlemish2(obj.getBlemish2());
        if (obj.getChartAlignment() != null) builder.setChartAlignment(obj.getChartAlignment());
        if (obj.getChartAlignment1() != null) builder.setChartAlignment1(obj.getChartAlignment1());
        if (obj.getChartAlignment2() != null) builder.setChartAlignment2(obj.getChartAlignment2());
        if (obj.getClampOnOff() != null) builder.setClampOnOff(obj.getClampOnOff());
        if (obj.getDelay() != null) builder.setDelay(obj.getDelay());
        if (obj.getDestroy() != null) builder.setDestroy(obj.getDestroy());
        if (obj.getDestroyStart() != null) builder.setDestroyStart(obj.getDestroyStart());
        if (obj.getDispense() != null) builder.setDispense(obj.getDispense());

        // 2025-01-15 扩展参数
        if (obj.getDispense1() != null) builder.setDispense1(obj.getDispense1());
        if (obj.getDispense2() != null) builder.setDispense2(obj.getDispense2());
        if (obj.getEpoxyInspection() != null) builder.setEpoxyInspection(obj.getEpoxyInspection());
        if (obj.getEpoxyInspectionAuto() != null) builder.setEpoxyInspectionAuto(obj.getEpoxyInspectionAuto());
        if (obj.getGrab() != null) builder.setGrab(obj.getGrab());

        // 2025-01-15 扩展参数
        if (obj.getGrab1() != null) builder.setGrab1(obj.getGrab1());
        if (obj.getGrab2() != null) builder.setGrab2(obj.getGrab2());

        // 2025-03-11 扩展参数
        if (obj.getGrab3() != null) builder.setGrab3(obj.getGrab3());
        if (obj.getGripperOpen() != null) builder.setGripperOpen(obj.getGripperOpen());
        if (obj.getInit() != null) builder.setInit(obj.getInit());

        // 2025-01-15 扩展参数
        if (obj.getInit1() != null) builder.setInit1(obj.getInit1());
        if (obj.getInit2() != null) builder.setInit2(obj.getInit2());
        if (obj.getInit3() != null) builder.setInit3(obj.getInit3());
        if (obj.getLpBlemish() != null) builder.setLpBlemish(obj.getLpBlemish());
        if (obj.getLpOc() != null) builder.setLpOc(obj.getLpOc());
        if (obj.getLpOcCheck() != null) builder.setLpOcCheck(obj.getLpOcCheck());
        if (obj.getLpOn() != null) builder.setLpOn(obj.getLpOn());

        // 2025-03-11 扩展参数
        if (obj.getLpOn1() != null) builder.setLpOn1(obj.getLpOn1());
        if (obj.getLpOnBlemish() != null) builder.setLpOnBlemish(obj.getLpOnBlemish());
        if (obj.getLpOff() != null) builder.setLpOff(obj.getLpOff());

        // 2025-03-11 扩展参数
        if (obj.getLpOff1() != null) builder.setLpOff1(obj.getLpOff1());

        // 2025-01-16 lpOff0对于mybatis驼峰转换不友好，更改以下指令
        if (obj.getLpIntensity() != null) builder.setLpIntensity(obj.getLpIntensity());
        if (obj.getMoveToBlemishPos() != null) builder.setMoveToBlemishPos(obj.getMoveToBlemishPos());
        if (obj.getMtfCheck() != null) builder.setMtfCheck(obj.getMtfCheck());
        if (obj.getMtfCheck1() != null) builder.setMtfCheck1(obj.getMtfCheck1());
        if (obj.getMtfCheck2() != null) builder.setMtfCheck2(obj.getMtfCheck2());
        if (obj.getMtfCheck3() != null) builder.setMtfCheck3(obj.getMtfCheck3());

        // 2025-03-11 多焦段MTF检查
        if (obj.getMtfOffAxisCheck1() != null) builder.setMtfOffAxisCheck1(obj.getMtfOffAxisCheck1());
        if (obj.getMtfOffAxisCheck2() != null) builder.setMtfOffAxisCheck2(obj.getMtfOffAxisCheck2());
        if (obj.getMtfOffAxisCheck3() != null) builder.setMtfOffAxisCheck3(obj.getMtfOffAxisCheck3());
        if (obj.getMtfOffAxisCheck4() != null) builder.setMtfOffAxisCheck4(obj.getMtfOffAxisCheck4());
        if (obj.getOpenCheck() != null) builder.setOpenCheck(obj.getOpenCheck());

        // 2025-01-15 扩展参数
        if (obj.getOpenCheck1() != null) builder.setOpenCheck1(obj.getOpenCheck1());
        if (obj.getOpenCheck2() != null) builder.setOpenCheck2(obj.getOpenCheck2());
        if (obj.getOpenCheck3() != null) builder.setOpenCheck3(obj.getOpenCheck3());

        // 2025-01-15 新增参数
        if (obj.getPrToBond() != null) builder.setPrToBond(obj.getPrToBond());

        // 台虹厂区的utXyzMove和recordPosition分开管控，因此新增 2025-01-15
        if (obj.getUtXyzMove() != null) builder.setUtXyzMove(obj.getUtXyzMove());
        if (obj.getRecordPosition() != null) builder.setRecordPosition(obj.getRecordPosition());
        if (obj.getReInit() != null) builder.setReInit(obj.getReInit());
        if (obj.getSaveOc() != null) builder.setSaveOc(obj.getSaveOc());
        if (obj.getSaveMtf() != null) builder.setSaveMtf(obj.getSaveMtf());
        if (obj.getSensorReset() != null) builder.setSensorReset(obj.getSensorReset());
        if (obj.getSfrCheck() != null) builder.setSfrCheck(obj.getSfrCheck());

        if (obj.getSid() != null) builder.setSid(obj.getSid());
        if (obj.getUvon() != null) builder.setUvon(obj.getUvon());
        if (obj.getUvoff() != null) builder.setUvoff(obj.getUvoff());

        // vcmHall 指标 2025-03-27
        if (obj.getVcmHall() != null) builder.setVcmHall(obj.getVcmHall());
        if (obj.getVcmHall1() != null) builder.setVcmHall1(obj.getVcmHall1());
        if (obj.getVcmHall2() != null) builder.setVcmHall2(obj.getVcmHall2());

        if (obj.getYLevel() != null) builder.setYLevel(obj.getYLevel());

        // AA Item 指标
        if (obj.getAa1RoiCc() != null) builder.setAa1RoiCc(obj.getAa1RoiCc());
        if (obj.getAa1RoiUl() != null) builder.setAa1RoiUl(obj.getAa1RoiUl());
        if (obj.getAa1RoiUr() != null) builder.setAa1RoiUr(obj.getAa1RoiUr());
        if (obj.getAa1RoiLl() != null) builder.setAa1RoiLl(obj.getAa1RoiLl());
        if (obj.getAa1RoiLr() != null) builder.setAa1RoiLr(obj.getAa1RoiLr());

        // 新增 2024-10-28
        if (obj.getAa1Target() != null) builder.setAa1Target(obj.getAa1Target());
        if (obj.getAa1CcToCornerLimit() != null) builder.setAa1CcToCornerLimit(obj.getAa1CcToCornerLimit());
        if (obj.getAa1CcToCornerLimitMin() != null) builder.setAa1CcToCornerLimitMin(obj.getAa1CcToCornerLimitMin());
        if (obj.getAa1CornerScoreDifferenceRejectValue() != null) builder.setAa1CornerScoreDifferenceRejectValue(obj.getAa1CornerScoreDifferenceRejectValue());
        if (obj.getAa1ZRef() != null) builder.setAa1ZRef(obj.getAa1ZRef());
        if (obj.getAa1SrchStep() != null) builder.setAa1SrchStep(obj.getAa1SrchStep());
        if (obj.getAa1GoldenGlueThicknessMin() != null) builder.setAa1GoldenGlueThicknessMin(obj.getAa1GoldenGlueThicknessMin());
        if (obj.getAa1GoldenGlueThicknessMax() != null) builder.setAa1GoldenGlueThicknessMax(obj.getAa1GoldenGlueThicknessMax());

        if (obj.getAa2RoiCc() != null) builder.setAa2RoiCc(obj.getAa2RoiCc());
        if (obj.getAa2RoiUl() != null) builder.setAa2RoiUl(obj.getAa2RoiUl());
        if (obj.getAa2RoiUr() != null) builder.setAa2RoiUr(obj.getAa2RoiUr());
        if (obj.getAa2RoiLl() != null) builder.setAa2RoiLl(obj.getAa2RoiLl());
        if (obj.getAa2RoiLr() != null) builder.setAa2RoiLr(obj.getAa2RoiLr());

        // 新增 2024-10-28
        if (obj.getAa2Target() != null) builder.setAa2Target(obj.getAa2Target());
        if (obj.getAa2CcToCornerLimit() != null) builder.setAa2CcToCornerLimit(obj.getAa2CcToCornerLimit());
        if (obj.getAa2CcToCornerLimitMin() != null) builder.setAa2CcToCornerLimitMin(obj.getAa2CcToCornerLimitMin());
        if (obj.getAa2CornerScoreDifferenceRejectValue() != null) builder.setAa2CornerScoreDifferenceRejectValue(obj.getAa2CornerScoreDifferenceRejectValue());
        if (obj.getAa2ZRef() != null) builder.setAa2ZRef(obj.getAa2ZRef());
        if (obj.getAa2SrchStep() != null) builder.setAa2SrchStep(obj.getAa2SrchStep());
        if (obj.getAa2GoldenGlueThicknessMin() != null) builder.setAa2GoldenGlueThicknessMin(obj.getAa2GoldenGlueThicknessMin());
        if (obj.getAa2GoldenGlueThicknessMax() != null) builder.setAa2GoldenGlueThicknessMax(obj.getAa2GoldenGlueThicknessMax());

        if (obj.getAa3RoiCc() != null) builder.setAa3RoiCc(obj.getAa3RoiCc());
        if (obj.getAa3RoiUl() != null) builder.setAa3RoiUl(obj.getAa3RoiUl());
        if (obj.getAa3RoiUr() != null) builder.setAa3RoiUr(obj.getAa3RoiUr());
        if (obj.getAa3RoiLl() != null) builder.setAa3RoiLl(obj.getAa3RoiLl());
        if (obj.getAa3RoiLr() != null) builder.setAa3RoiLr(obj.getAa3RoiLr());

        // 新增 2024-10-28
        if (obj.getAa3Target() != null) builder.setAa3Target(obj.getAa3Target());
        if (obj.getAa3CcToCornerLimit() != null) builder.setAa3CcToCornerLimit(obj.getAa3CcToCornerLimit());
        if (obj.getAa3CcToCornerLimitMin() != null) builder.setAa3CcToCornerLimitMin(obj.getAa3CcToCornerLimitMin());
        if (obj.getAa3CornerScoreDifferenceRejectValue() != null) builder.setAa3CornerScoreDifferenceRejectValue(obj.getAa3CornerScoreDifferenceRejectValue());
        if (obj.getAa3ZRef() != null) builder.setAa3ZRef(obj.getAa3ZRef());
        if (obj.getAa3SrchStep() != null) builder.setAa3SrchStep(obj.getAa3SrchStep());
        if (obj.getAa3GoldenGlueThicknessMin() != null) builder.setAa3GoldenGlueThicknessMin(obj.getAa3GoldenGlueThicknessMin());
        if (obj.getAa3GoldenGlueThicknessMax() != null) builder.setAa3GoldenGlueThicknessMax(obj.getAa3GoldenGlueThicknessMax());

        // mtfCheck Item 指标
        if (obj.getMtfCheckF() != null) builder.setMtfCheckF(obj.getMtfCheckF());
        if (obj.getMtfCheck1F() != null) builder.setMtfCheck1F(obj.getMtfCheck1F());
        if (obj.getMtfCheck2F() != null) builder.setMtfCheck2F(obj.getMtfCheck2F());
        if (obj.getMtfCheck3F() != null) builder.setMtfCheck3F(obj.getMtfCheck3F());

        // 2025-03-11 多焦段 mtfCheck
        if (obj.getMtfOffAxisCheck1F() != null) builder.setMtfOffAxisCheck1F(obj.getMtfOffAxisCheck1F());
        if (obj.getMtfOffAxisCheck2F() != null) builder.setMtfOffAxisCheck2F(obj.getMtfOffAxisCheck2F());
        if (obj.getMtfOffAxisCheck3F() != null) builder.setMtfOffAxisCheck3F(obj.getMtfOffAxisCheck3F());
        if (obj.getMtfOffAxisCheck4F() != null) builder.setMtfOffAxisCheck4F(obj.getMtfOffAxisCheck4F());

        // chartAlignment Item 指标
        if (obj.getChartAlignmentXResMin() != null) builder.setChartAlignmentXResMin(obj.getChartAlignmentXResMin());
        if (obj.getChartAlignmentXResMax() != null) builder.setChartAlignmentXResMax(obj.getChartAlignmentXResMax());
        if (obj.getChartAlignmentYResMin() != null) builder.setChartAlignmentYResMin(obj.getChartAlignmentYResMin());
        if (obj.getChartAlignmentYResMax() != null) builder.setChartAlignmentYResMax(obj.getChartAlignmentYResMax());

        if (obj.getChartAlignment1XResMin() != null) builder.setChartAlignment1XResMin(obj.getChartAlignment1XResMin());
        if (obj.getChartAlignment1XResMax() != null) builder.setChartAlignment1XResMax(obj.getChartAlignment1XResMax());
        if (obj.getChartAlignment1YResMin() != null) builder.setChartAlignment1YResMin(obj.getChartAlignment1YResMin());
        if (obj.getChartAlignment1YResMax() != null) builder.setChartAlignment1YResMax(obj.getChartAlignment1YResMax());

        if (obj.getChartAlignment2XResMin() != null) builder.setChartAlignment2XResMin(obj.getChartAlignment2XResMin());
        if (obj.getChartAlignment2XResMax() != null) builder.setChartAlignment2XResMax(obj.getChartAlignment2XResMax());
        if (obj.getChartAlignment2YResMin() != null) builder.setChartAlignment2YResMin(obj.getChartAlignment2YResMin());
        if (obj.getChartAlignment2YResMax() != null) builder.setChartAlignment2YResMax(obj.getChartAlignment2YResMax());

        // EpoxyInspection Auto Item 指标
        if (obj.getEpoxyInspectionInterval() != null) builder.setEpoxyInspectionInterval(obj.getEpoxyInspectionInterval());

        // vcmHall 指标 2025-03-27
        if (obj.getVcmHallHallAfMin() != null) builder.setVcmHallHallAfMin(obj.getVcmHallHallAfMin());
        if (obj.getVcmHallHallAfMax() != null) builder.setVcmHallHallAfMax(obj.getVcmHallHallAfMax());
        if (obj.getVcmHallHallXMin() != null) builder.setVcmHallHallXMin(obj.getVcmHallHallXMin());
        if (obj.getVcmHallHallXMax() != null) builder.setVcmHallHallXMax(obj.getVcmHallHallXMax());
        if (obj.getVcmHallHallYMin() != null) builder.setVcmHallHallYMin(obj.getVcmHallHallYMin());
        if (obj.getVcmHallHallYMax() != null) builder.setVcmHallHallYMax(obj.getVcmHallHallYMax());

        if (obj.getVcmHall1HallAfMin() != null) builder.setVcmHall1HallAfMin(obj.getVcmHall1HallAfMin());
        if (obj.getVcmHall1HallAfMax() != null) builder.setVcmHall1HallAfMax(obj.getVcmHall1HallAfMax());
        if (obj.getVcmHall1HallXMin() != null) builder.setVcmHall1HallXMin(obj.getVcmHall1HallXMin());
        if (obj.getVcmHall1HallXMax() != null) builder.setVcmHall1HallXMax(obj.getVcmHall1HallXMax());
        if (obj.getVcmHall1HallYMin() != null) builder.setVcmHall1HallYMin(obj.getVcmHall1HallYMin());
        if (obj.getVcmHall1HallYMax() != null) builder.setVcmHall1HallYMax(obj.getVcmHall1HallYMax());

        if (obj.getVcmHall2HallAfMin() != null) builder.setVcmHall2HallAfMin(obj.getVcmHall2HallAfMin());
        if (obj.getVcmHall2HallAfMax() != null) builder.setVcmHall2HallAfMax(obj.getVcmHall2HallAfMax());
        if (obj.getVcmHall2HallXMin() != null) builder.setVcmHall2HallXMin(obj.getVcmHall2HallXMin());
        if (obj.getVcmHall2HallXMax() != null) builder.setVcmHall2HallXMax(obj.getVcmHall2HallXMax());
        if (obj.getVcmHall2HallYMin() != null) builder.setVcmHall2HallYMin(obj.getVcmHall2HallYMin());
        if (obj.getVcmHall2HallYMax() != null) builder.setVcmHall2HallYMax(obj.getVcmHall2HallYMax());

        // RecordPosition 指标
        if (obj.getUtXyzMoveVal() != null) builder.setUtXyzMoveVal(obj.getUtXyzMoveVal());
        if (obj.getRecordPositionName() != null) builder.setRecordPositionName(obj.getRecordPositionName());

        // OcCheck 指标 Save Oc
        if (obj.getSaveOcXOffsetMin() != null) builder.setSaveOcXOffsetMin(obj.getSaveOcXOffsetMin());
        if (obj.getSaveOcXOffsetMax() != null) builder.setSaveOcXOffsetMax(obj.getSaveOcXOffsetMax());
        if (obj.getSaveOcYOffsetMin() != null) builder.setSaveOcYOffsetMin(obj.getSaveOcYOffsetMin());
        if (obj.getSaveOcYOffsetMax() != null) builder.setSaveOcYOffsetMax(obj.getSaveOcYOffsetMax());

        // SaveMtf 指标
        if (obj.getSaveMtfCcMin() != null) builder.setSaveMtfCcMin(obj.getSaveMtfCcMin());
        if (obj.getSaveMtfCcMax() != null) builder.setSaveMtfCcMax(obj.getSaveMtfCcMax());

        return builder.build();
    }

    /**
     * 将 Protobuf 消息转换为实体对象
     *
     * @param msg Protobuf 消息
     * @return 实体对象
     */
    @Override
    public com.im.qtech.common.dto.param.EqLstPOJO fromProto(Message msg) {
        if (msg == null) return null;

        EqLstProto.EqLstPOJO proto = null;
        com.im.qtech.common.dto.param.EqLstPOJO obj = null;

        if (msg instanceof EqLstProto.EqLstPOJO) {
            proto = (EqLstProto.EqLstPOJO) msg;
            obj = new com.im.qtech.common.dto.param.EqLstPOJO();
        } else {
            return null;
        }

        // 基础设备参数
        if (!proto.getModule().isEmpty()) obj.setModule(proto.getModule());
        if (!proto.getAa1().isEmpty()) obj.setAa1(proto.getAa1());
        if (!proto.getAa2().isEmpty()) obj.setAa2(proto.getAa2());
        if (!proto.getAa3().isEmpty()) obj.setAa3(proto.getAa3());
        if (!proto.getBackToPosition().isEmpty()) obj.setBackToPosition(proto.getBackToPosition());
        if (!proto.getBlemish().isEmpty()) obj.setBlemish(proto.getBlemish());

        // 2025-01-15 扩展参数
        if (!proto.getBlemish1().isEmpty()) obj.setBlemish1(proto.getBlemish1());
        if (!proto.getBlemish2().isEmpty()) obj.setBlemish2(proto.getBlemish2());
        if (!proto.getChartAlignment().isEmpty()) obj.setChartAlignment(proto.getChartAlignment());
        if (!proto.getChartAlignment1().isEmpty()) obj.setChartAlignment1(proto.getChartAlignment1());
        if (!proto.getChartAlignment2().isEmpty()) obj.setChartAlignment2(proto.getChartAlignment2());
        if (!proto.getClampOnOff().isEmpty()) obj.setClampOnOff(proto.getClampOnOff());
        if (!proto.getDelay().isEmpty()) obj.setDelay(proto.getDelay());
        if (!proto.getDestroy().isEmpty()) obj.setDestroy(proto.getDestroy());
        if (!proto.getDestroyStart().isEmpty()) obj.setDestroyStart(proto.getDestroyStart());
        if (!proto.getDispense().isEmpty()) obj.setDispense(proto.getDispense());

        // 2025-01-15 扩展参数
        if (!proto.getDispense1().isEmpty()) obj.setDispense1(proto.getDispense1());
        if (!proto.getDispense2().isEmpty()) obj.setDispense2(proto.getDispense2());
        if (!proto.getEpoxyInspection().isEmpty()) obj.setEpoxyInspection(proto.getEpoxyInspection());
        if (!proto.getEpoxyInspectionAuto().isEmpty()) obj.setEpoxyInspectionAuto(proto.getEpoxyInspectionAuto());
        if (!proto.getGrab().isEmpty()) obj.setGrab(proto.getGrab());

        // 2025-01-15 扩展参数
        if (!proto.getGrab1().isEmpty()) obj.setGrab1(proto.getGrab1());
        if (!proto.getGrab2().isEmpty()) obj.setGrab2(proto.getGrab2());

        // 2025-03-11 扩展参数
        if (!proto.getGrab3().isEmpty()) obj.setGrab3(proto.getGrab3());
        if (!proto.getGripperOpen().isEmpty()) obj.setGripperOpen(proto.getGripperOpen());
        if (!proto.getInit().isEmpty()) obj.setInit(proto.getInit());

        // 2025-01-15 扩展参数
        if (!proto.getInit1().isEmpty()) obj.setInit1(proto.getInit1());
        if (!proto.getInit2().isEmpty()) obj.setInit2(proto.getInit2());
        if (!proto.getInit3().isEmpty()) obj.setInit3(proto.getInit3());
        if (!proto.getLpBlemish().isEmpty()) obj.setLpBlemish(proto.getLpBlemish());
        if (!proto.getLpOc().isEmpty()) obj.setLpOc(proto.getLpOc());
        if (!proto.getLpOcCheck().isEmpty()) obj.setLpOcCheck(proto.getLpOcCheck());
        if (!proto.getLpOn().isEmpty()) obj.setLpOn(proto.getLpOn());

        // 2025-03-11 扩展参数
        if (!proto.getLpOn1().isEmpty()) obj.setLpOn1(proto.getLpOn1());
        if (!proto.getLpOnBlemish().isEmpty()) obj.setLpOnBlemish(proto.getLpOnBlemish());
        if (!proto.getLpOff().isEmpty()) obj.setLpOff(proto.getLpOff());

        // 2025-03-11 扩展参数
        if (!proto.getLpOff1().isEmpty()) obj.setLpOff1(proto.getLpOff1());

        // 2025-01-16 lpOff0对于mybatis驼峰转换不友好，更改以下指令
        if (!proto.getLpIntensity().isEmpty()) obj.setLpIntensity(proto.getLpIntensity());
        if (!proto.getMoveToBlemishPos().isEmpty()) obj.setMoveToBlemishPos(proto.getMoveToBlemishPos());
        if (!proto.getMtfCheck().isEmpty()) obj.setMtfCheck(proto.getMtfCheck());
        if (!proto.getMtfCheck1().isEmpty()) obj.setMtfCheck1(proto.getMtfCheck1());
        if (!proto.getMtfCheck2().isEmpty()) obj.setMtfCheck2(proto.getMtfCheck2());
        if (!proto.getMtfCheck3().isEmpty()) obj.setMtfCheck3(proto.getMtfCheck3());

        // 2025-03-11 多焦段MTF检查
        if (!proto.getMtfOffAxisCheck1().isEmpty()) obj.setMtfOffAxisCheck1(proto.getMtfOffAxisCheck1());
        if (!proto.getMtfOffAxisCheck2().isEmpty()) obj.setMtfOffAxisCheck2(proto.getMtfOffAxisCheck2());
        if (!proto.getMtfOffAxisCheck3().isEmpty()) obj.setMtfOffAxisCheck3(proto.getMtfOffAxisCheck3());
        if (!proto.getMtfOffAxisCheck4().isEmpty()) obj.setMtfOffAxisCheck4(proto.getMtfOffAxisCheck4());
        if (!proto.getOpenCheck().isEmpty()) obj.setOpenCheck(proto.getOpenCheck());

        // 2025-01-15 扩展参数
        if (!proto.getOpenCheck1().isEmpty()) obj.setOpenCheck1(proto.getOpenCheck1());
        if (!proto.getOpenCheck2().isEmpty()) obj.setOpenCheck2(proto.getOpenCheck2());
        if (!proto.getOpenCheck3().isEmpty()) obj.setOpenCheck3(proto.getOpenCheck3());

        // 2025-01-15 新增参数
        if (!proto.getPrToBond().isEmpty()) obj.setPrToBond(proto.getPrToBond());

        // 台虹厂区的utXyzMove和recordPosition分开管控，因此新增 2025-01-15
        if (!proto.getUtXyzMove().isEmpty()) obj.setUtXyzMove(proto.getUtXyzMove());
        if (!proto.getRecordPosition().isEmpty()) obj.setRecordPosition(proto.getRecordPosition());
        if (!proto.getReInit().isEmpty()) obj.setReInit(proto.getReInit());
        if (!proto.getSaveOc().isEmpty()) obj.setSaveOc(proto.getSaveOc());
        if (!proto.getSaveMtf().isEmpty()) obj.setSaveMtf(proto.getSaveMtf());
        if (!proto.getSensorReset().isEmpty()) obj.setSensorReset(proto.getSensorReset());
        if (!proto.getSfrCheck().isEmpty()) obj.setSfrCheck(proto.getSfrCheck());

        if (!proto.getSid().isEmpty()) obj.setSid(proto.getSid());
        if (!proto.getUvon().isEmpty()) obj.setUvon(proto.getUvon());
        if (!proto.getUvoff().isEmpty()) obj.setUvoff(proto.getUvoff());

        // vcmHall 指标 2025-03-27
        if (!proto.getVcmHall().isEmpty()) obj.setVcmHall(proto.getVcmHall());
        if (!proto.getVcmHall1().isEmpty()) obj.setVcmHall1(proto.getVcmHall1());
        if (!proto.getVcmHall2().isEmpty()) obj.setVcmHall2(proto.getVcmHall2());

        if (!proto.getYLevel().isEmpty()) obj.setYLevel(proto.getYLevel());

        // AA Item 指标
        if (!proto.getAa1RoiCc().isEmpty()) obj.setAa1RoiCc(proto.getAa1RoiCc());
        if (!proto.getAa1RoiUl().isEmpty()) obj.setAa1RoiUl(proto.getAa1RoiUl());
        if (!proto.getAa1RoiUr().isEmpty()) obj.setAa1RoiUr(proto.getAa1RoiUr());
        if (!proto.getAa1RoiLl().isEmpty()) obj.setAa1RoiLl(proto.getAa1RoiLl());
        if (!proto.getAa1RoiLr().isEmpty()) obj.setAa1RoiLr(proto.getAa1RoiLr());

        // 新增 2024-10-28
        if (!proto.getAa1Target().isEmpty()) obj.setAa1Target(proto.getAa1Target());
        if (!proto.getAa1CcToCornerLimit().isEmpty()) obj.setAa1CcToCornerLimit(proto.getAa1CcToCornerLimit());
        if (!proto.getAa1CcToCornerLimitMin().isEmpty()) obj.setAa1CcToCornerLimitMin(proto.getAa1CcToCornerLimitMin());
        if (!proto.getAa1CornerScoreDifferenceRejectValue().isEmpty()) obj.setAa1CornerScoreDifferenceRejectValue(proto.getAa1CornerScoreDifferenceRejectValue());
        if (!proto.getAa1ZRef().isEmpty()) obj.setAa1ZRef(proto.getAa1ZRef());
        if (!proto.getAa1SrchStep().isEmpty()) obj.setAa1SrchStep(proto.getAa1SrchStep());
        if (!proto.getAa1GoldenGlueThicknessMin().isEmpty()) obj.setAa1GoldenGlueThicknessMin(proto.getAa1GoldenGlueThicknessMin());
        if (!proto.getAa1GoldenGlueThicknessMax().isEmpty()) obj.setAa1GoldenGlueThicknessMax(proto.getAa1GoldenGlueThicknessMax());

        if (!proto.getAa2RoiCc().isEmpty()) obj.setAa2RoiCc(proto.getAa2RoiCc());
        if (!proto.getAa2RoiUl().isEmpty()) obj.setAa2RoiUl(proto.getAa2RoiUl());
        if (!proto.getAa2RoiUr().isEmpty()) obj.setAa2RoiUr(proto.getAa2RoiUr());
        if (!proto.getAa2RoiLl().isEmpty()) obj.setAa2RoiLl(proto.getAa2RoiLl());
        if (!proto.getAa2RoiLr().isEmpty()) obj.setAa2RoiLr(proto.getAa2RoiLr());

        // 新增 2024-10-28
        if (!proto.getAa2Target().isEmpty()) obj.setAa2Target(proto.getAa2Target());
        if (!proto.getAa2CcToCornerLimit().isEmpty()) obj.setAa2CcToCornerLimit(proto.getAa2CcToCornerLimit());
        if (!proto.getAa2CcToCornerLimitMin().isEmpty()) obj.setAa2CcToCornerLimitMin(proto.getAa2CcToCornerLimitMin());
        if (!proto.getAa2CornerScoreDifferenceRejectValue().isEmpty()) obj.setAa2CornerScoreDifferenceRejectValue(proto.getAa2CornerScoreDifferenceRejectValue());
        if (!proto.getAa2ZRef().isEmpty()) obj.setAa2ZRef(proto.getAa2ZRef());
        if (!proto.getAa2SrchStep().isEmpty()) obj.setAa2SrchStep(proto.getAa2SrchStep());
        if (!proto.getAa2GoldenGlueThicknessMin().isEmpty()) obj.setAa2GoldenGlueThicknessMin(proto.getAa2GoldenGlueThicknessMin());
        if (!proto.getAa2GoldenGlueThicknessMax().isEmpty()) obj.setAa2GoldenGlueThicknessMax(proto.getAa2GoldenGlueThicknessMax());

        if (!proto.getAa3RoiCc().isEmpty()) obj.setAa3RoiCc(proto.getAa3RoiCc());
        if (!proto.getAa3RoiUl().isEmpty()) obj.setAa3RoiUl(proto.getAa3RoiUl());
        if (!proto.getAa3RoiUr().isEmpty()) obj.setAa3RoiUr(proto.getAa3RoiUr());
        if (!proto.getAa3RoiLl().isEmpty()) obj.setAa3RoiLl(proto.getAa3RoiLl());
        if (!proto.getAa3RoiLr().isEmpty()) obj.setAa3RoiLr(proto.getAa3RoiLr());

        // 新增 2024-10-28
        if (!proto.getAa3Target().isEmpty()) obj.setAa3Target(proto.getAa3Target());
        if (!proto.getAa3CcToCornerLimit().isEmpty()) obj.setAa3CcToCornerLimit(proto.getAa3CcToCornerLimit());
        if (!proto.getAa3CcToCornerLimitMin().isEmpty()) obj.setAa3CcToCornerLimitMin(proto.getAa3CcToCornerLimitMin());
        if (!proto.getAa3CornerScoreDifferenceRejectValue().isEmpty()) obj.setAa3CornerScoreDifferenceRejectValue(proto.getAa3CornerScoreDifferenceRejectValue());
        if (!proto.getAa3ZRef().isEmpty()) obj.setAa3ZRef(proto.getAa3ZRef());
        if (!proto.getAa3SrchStep().isEmpty()) obj.setAa3SrchStep(proto.getAa3SrchStep());
        if (!proto.getAa3GoldenGlueThicknessMin().isEmpty()) obj.setAa3GoldenGlueThicknessMin(proto.getAa3GoldenGlueThicknessMin());
        if (!proto.getAa3GoldenGlueThicknessMax().isEmpty()) obj.setAa3GoldenGlueThicknessMax(proto.getAa3GoldenGlueThicknessMax());

        // mtfCheck Item 指标
        if (!proto.getMtfCheckF().isEmpty()) obj.setMtfCheckF(proto.getMtfCheckF());
        if (!proto.getMtfCheck1F().isEmpty()) obj.setMtfCheck1F(proto.getMtfCheck1F());
        if (!proto.getMtfCheck2F().isEmpty()) obj.setMtfCheck2F(proto.getMtfCheck2F());
        if (!proto.getMtfCheck3F().isEmpty()) obj.setMtfCheck3F(proto.getMtfCheck3F());

        // 2025-03-11 多焦段 mtfCheck
        if (!proto.getMtfOffAxisCheck1F().isEmpty()) obj.setMtfOffAxisCheck1F(proto.getMtfOffAxisCheck1F());
        if (!proto.getMtfOffAxisCheck2F().isEmpty()) obj.setMtfOffAxisCheck2F(proto.getMtfOffAxisCheck2F());
        if (!proto.getMtfOffAxisCheck3F().isEmpty()) obj.setMtfOffAxisCheck3F(proto.getMtfOffAxisCheck3F());
        if (!proto.getMtfOffAxisCheck4F().isEmpty()) obj.setMtfOffAxisCheck4F(proto.getMtfOffAxisCheck4F());

        // chartAlignment Item 指标
        if (!proto.getChartAlignmentXResMin().isEmpty()) obj.setChartAlignmentXResMin(proto.getChartAlignmentXResMin());
        if (!proto.getChartAlignmentXResMax().isEmpty()) obj.setChartAlignmentXResMax(proto.getChartAlignmentXResMax());
        if (!proto.getChartAlignmentYResMin().isEmpty()) obj.setChartAlignmentYResMin(proto.getChartAlignmentYResMin());
        if (!proto.getChartAlignmentYResMax().isEmpty()) obj.setChartAlignmentYResMax(proto.getChartAlignmentYResMax());

        if (!proto.getChartAlignment1XResMin().isEmpty()) obj.setChartAlignment1XResMin(proto.getChartAlignment1XResMin());
        if (!proto.getChartAlignment1XResMax().isEmpty()) obj.setChartAlignment1XResMax(proto.getChartAlignment1XResMax());
        if (!proto.getChartAlignment1YResMin().isEmpty()) obj.setChartAlignment1YResMin(proto.getChartAlignment1YResMin());
        if (!proto.getChartAlignment1YResMax().isEmpty()) obj.setChartAlignment1YResMax(proto.getChartAlignment1YResMax());

        if (!proto.getChartAlignment2XResMin().isEmpty()) obj.setChartAlignment2XResMin(proto.getChartAlignment2XResMin());
        if (!proto.getChartAlignment2XResMax().isEmpty()) obj.setChartAlignment2XResMax(proto.getChartAlignment2XResMax());
        if (!proto.getChartAlignment2YResMin().isEmpty()) obj.setChartAlignment2YResMin(proto.getChartAlignment2YResMin());
        if (!proto.getChartAlignment2YResMax().isEmpty()) obj.setChartAlignment2YResMax(proto.getChartAlignment2YResMax());

        // EpoxyInspection Auto Item 指标
        if (!proto.getEpoxyInspectionInterval().isEmpty()) obj.setEpoxyInspectionInterval(proto.getEpoxyInspectionInterval());

        // vcmHall 指标 2025-03-27
        if (!proto.getVcmHallHallAfMin().isEmpty()) obj.setVcmHallHallAfMin(proto.getVcmHallHallAfMin());
        if (!proto.getVcmHallHallAfMax().isEmpty()) obj.setVcmHallHallAfMax(proto.getVcmHallHallAfMax());
        if (!proto.getVcmHallHallXMin().isEmpty()) obj.setVcmHallHallXMin(proto.getVcmHallHallXMin());
        if (!proto.getVcmHallHallXMax().isEmpty()) obj.setVcmHallHallXMax(proto.getVcmHallHallXMax());
        if (!proto.getVcmHallHallYMin().isEmpty()) obj.setVcmHallHallYMin(proto.getVcmHallHallYMin());
        if (!proto.getVcmHallHallYMax().isEmpty()) obj.setVcmHallHallYMax(proto.getVcmHallHallYMax());

        if (!proto.getVcmHall1HallAfMin().isEmpty()) obj.setVcmHall1HallAfMin(proto.getVcmHall1HallAfMin());
        if (!proto.getVcmHall1HallAfMax().isEmpty()) obj.setVcmHall1HallAfMax(proto.getVcmHall1HallAfMax());
        if (!proto.getVcmHall1HallXMin().isEmpty()) obj.setVcmHall1HallXMin(proto.getVcmHall1HallXMin());
        if (!proto.getVcmHall1HallXMax().isEmpty()) obj.setVcmHall1HallXMax(proto.getVcmHall1HallXMax());
        if (!proto.getVcmHall1HallYMin().isEmpty()) obj.setVcmHall1HallYMin(proto.getVcmHall1HallYMin());
        if (!proto.getVcmHall1HallYMax().isEmpty()) obj.setVcmHall1HallYMax(proto.getVcmHall1HallYMax());

        if (!proto.getVcmHall2HallAfMin().isEmpty()) obj.setVcmHall2HallAfMin(proto.getVcmHall2HallAfMin());
        if (!proto.getVcmHall2HallAfMax().isEmpty()) obj.setVcmHall2HallAfMax(proto.getVcmHall2HallAfMax());
        if (!proto.getVcmHall2HallXMin().isEmpty()) obj.setVcmHall2HallXMin(proto.getVcmHall2HallXMin());
        if (!proto.getVcmHall2HallXMax().isEmpty()) obj.setVcmHall2HallXMax(proto.getVcmHall2HallXMax());
        if (!proto.getVcmHall2HallYMin().isEmpty()) obj.setVcmHall2HallYMin(proto.getVcmHall2HallYMin());
        if (!proto.getVcmHall2HallYMax().isEmpty()) obj.setVcmHall2HallYMax(proto.getVcmHall2HallYMax());

        // RecordPosition 指标
        if (!proto.getUtXyzMoveVal().isEmpty()) obj.setUtXyzMoveVal(proto.getUtXyzMoveVal());
        if (!proto.getRecordPositionName().isEmpty()) obj.setRecordPositionName(proto.getRecordPositionName());

        // OcCheck 指标 Save Oc
        if (!proto.getSaveOcXOffsetMin().isEmpty()) obj.setSaveOcXOffsetMin(proto.getSaveOcXOffsetMin());
        if (!proto.getSaveOcXOffsetMax().isEmpty()) obj.setSaveOcXOffsetMax(proto.getSaveOcXOffsetMax());
        if (!proto.getSaveOcYOffsetMin().isEmpty()) obj.setSaveOcYOffsetMin(proto.getSaveOcYOffsetMin());
        if (!proto.getSaveOcYOffsetMax().isEmpty()) obj.setSaveOcYOffsetMax(proto.getSaveOcYOffsetMax());

        // SaveMtf 指标
        if (!proto.getSaveMtfCcMin().isEmpty()) obj.setSaveMtfCcMin(proto.getSaveMtfCcMin());
        if (!proto.getSaveMtfCcMax().isEmpty()) obj.setSaveMtfCcMax(proto.getSaveMtfCcMax());

        return obj;
    }
}
