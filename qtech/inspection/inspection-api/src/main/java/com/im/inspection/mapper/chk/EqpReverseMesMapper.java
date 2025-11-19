package com.im.inspection.mapper.chk;

import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/13 17:29:56
 */

@Mapper
public interface EqpReverseMesMapper {

    public EqpReversePOJO getOneBySimId(String simId);
}
