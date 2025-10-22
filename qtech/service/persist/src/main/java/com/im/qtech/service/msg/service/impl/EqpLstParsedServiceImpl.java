package com.im.qtech.service.msg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.qtech.service.msg.entity.EqpLstParsed;
import com.im.qtech.service.msg.mapper.EqpLstParsedMapper;
import com.im.qtech.service.msg.service.IEqpLstParsedService;
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

    // 移除重写的 save 方法，直接使用父类的实现
    // MyBatis Plus 会自动处理 insert 操作
}
