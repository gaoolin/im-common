package com.im.qtech.service.msg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.im.qtech.service.msg.entity.EqpLstParsedForDoris;
import com.im.qtech.service.msg.entity.EqpLstParsedForOracle;

import java.util.concurrent.CompletableFuture;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/15
 */


public interface IEqpLstParsedForDoris extends IService<EqpLstParsedForDoris> {
    CompletableFuture<Boolean> saveAsync(EqpLstParsedForDoris entity);
}
