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

    int addDorisAsync(EqpReverseInfo EqpReverseInfo);

    int addWbOlpDoris(EqpReverseInfo EqpReverseInfo);

    int addWbOlpChkDorisBatch(@Param("list") List<EqpReverseInfo> list);

    int upsertOracle(EqpReverseInfo EqpReverseInfo);

    int upsertDoris(EqpReverseInfo EqpReverseInfo);

    int upsertPostgres(EqpReverseInfo EqpReverseInfo);

    int upsertOracleBatch(@Param("list") List<EqpReverseInfo> list);

    int upsertPostgresBatch(@Param("list") List<EqpReverseInfo> list);

    int upsertDorisBatch(@Param("list") List<EqpReverseInfo> list);

    int addOracleBatch(@Param("list") List<EqpReverseInfo> list);

    int addPostgresBatch(@Param("list") List<EqpReverseInfo> list);

    int addDorisBatch(@Param("list") List<EqpReverseInfo> list);
}
