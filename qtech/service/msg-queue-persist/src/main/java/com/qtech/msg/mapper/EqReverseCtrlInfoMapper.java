package com.qtech.msg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/13 17:29:56
 */

@Mapper
public interface EqReverseCtrlInfoMapper extends BaseMapper<EqpReversePOJO> {

    int upsertDoris(EqpReversePOJO pojo);

    int addAaListDoris(EqpReversePOJO pojo);

    int addWbOlpChkDoris(EqpReversePOJO pojo);

    int upsertOracle(EqpReversePOJO pojo);

    int upsertPostgres(EqpReversePOJO pojo);

    int upsertOracleBatch(@Param("list") List<EqpReversePOJO> list);

    int addWbOlpChkDorisBatch(@Param("list") List<EqpReversePOJO> list);
}
