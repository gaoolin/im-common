package com.qtech.msg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.qtech.data.dto.param.WbOlpRawData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/23 13:39:50
 */

@Mapper
public interface WbOlpRawDataMapper extends BaseMapper<WbOlpRawData> {
    public int addWbOlpRawDataBatch(@Param("wbOlpRawDataList") List<WbOlpRawData> wbOlpRawDataList);
}