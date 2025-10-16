package com.im.inspection.dpp.batch;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2022/06/12 10:07:53
 */

public abstract class BatchEngine {

    protected abstract void svcProcessData() throws Exception;

    public void start() throws Exception {
        this.svcProcessData();
    }
}
