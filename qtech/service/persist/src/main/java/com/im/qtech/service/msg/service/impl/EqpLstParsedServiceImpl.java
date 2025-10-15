package com.im.qtech.service.msg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.qtech.service.msg.entity.EqpLstParsed;
import com.im.qtech.service.msg.mapper.EqpLstParsedMapper;
import com.im.qtech.service.msg.service.IEqpLstParsedService;
import org.im.exception.constants.ErrorCode;
import org.im.exception.constants.ErrorMessage;
import org.im.exception.type.data.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/16 14:03:59
 */
@Service
public class EqpLstParsedServiceImpl extends ServiceImpl<EqpLstParsedMapper, EqpLstParsed> implements IEqpLstParsedService {
    private static final Logger logger = LoggerFactory.getLogger(EqpLstParsedServiceImpl.class);

    @Override
    public boolean save(EqpLstParsed aaListParams) {
        try {
            return super.save(aaListParams);
        } catch (Exception e) {
            logger.error(">>>>> save aaListParamsParsed error:{}", e.getMessage());
            throw new DataAccessException(ErrorCode.DB_INSERT_ERROR, ErrorMessage.DB_INSERT_ERROR);
        }
    }
}
