package com.im.qtech.service.msg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.qtech.service.msg.entity.EqpReverseInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/13 17:29:56
 */

@Mapper
public interface EqpReverseInfoMapper extends BaseMapper<EqpReverseInfo> {

    int upsertDoris(EqpReverseInfo EqpReverseInfo);

    int addEqpLstDoris(EqpReverseInfo EqpReverseInfo);

    int addWbOlpDoris(EqpReverseInfo EqpReverseInfo);

    int upsertOracle(EqpReverseInfo EqpReverseInfo);

    int upsertPostgres(EqpReverseInfo EqpReverseInfo);

    int upsertOracleBatch(@Param("list") List<EqpReverseInfo> list);

    int addWbOlpChkDorisBatch(@Param("list") List<EqpReverseInfo> list);
}
