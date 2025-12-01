package com.qtech.msg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qtech.im.constant.ErrorCode;
import com.qtech.im.constant.ErrorMessage;
import com.qtech.im.exception.DataAccessException;
import com.qtech.msg.common.dynamic.DataSourceNames;
import com.qtech.msg.common.dynamic.DataSourceSwitch;
import com.qtech.msg.entity.EqpLstParsed;
import com.qtech.msg.mapper.EqpLstParsedMapper;
import com.qtech.msg.service.IEqpLstParsedService;
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

    @DataSourceSwitch(name = DataSourceNames.SECOND)
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
