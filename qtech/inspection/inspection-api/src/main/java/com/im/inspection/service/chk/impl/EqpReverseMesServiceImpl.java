package com.im.inspection.service.chk.impl;

import com.im.inspection.config.dynamic.DataSourceNames;
import com.im.inspection.config.dynamic.DataSourceSwitch;
import com.im.inspection.mapper.chk.EqpReverseMesMapper;
import com.im.inspection.service.chk.IEqpReverseMesService;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static com.im.qtech.data.constant.QtechImBizConstant.REDIS_KEY_PREFIX_EQP_REVERSE_INFO;


/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/13 17:28:21
 */

@Service
public class EqpReverseMesServiceImpl implements IEqpReverseMesService {
    private static final Logger logger = LoggerFactory.getLogger(EqpReverseMesServiceImpl.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final EqpReverseMesMapper mapper;
    private final RedisTemplate<String, EqpReversePOJO> redisTemplate;

    public EqpReverseMesServiceImpl(EqpReverseMesMapper mapper, RedisTemplate<String, EqpReversePOJO> redisTemplate) {
        this.mapper = mapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<EqpReversePOJO> getList(EqpReversePOJO pojo) {
        try {
            List<EqpReversePOJO> result = mapper.getList(pojo);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            logger.error(">>>>> 查询数据库发生错误，selectEqReverseInfo error: ", e);
            return Collections.emptyList();
        }
    }

    @DataSourceSwitch(name = DataSourceNames.FIRST)
    @Override
    public EqpReversePOJO getOneBySimId(String simId) {
        String redisKey = REDIS_KEY_PREFIX_EQP_REVERSE_INFO + simId;

        try {
            EqpReversePOJO pojo = redisTemplate.opsForValue().get(redisKey);
            if (pojo != null) {
                return pojo;
            }
        } catch (Exception e) {
            logger.warn("Failed to retrieve from Redis for key: {}", redisKey, e);
        }

        EqpReversePOJO pojo = mapper.getOneBySimId(simId);
        if (pojo != null) {
            try {
                redisTemplate.opsForValue().set(redisKey, pojo, CACHE_TTL);
            } catch (Exception e) {
                logger.warn("Failed to save to Redis for key: {}", redisKey, e);
            }
        }

        return pojo;
    }
}
