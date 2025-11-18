package com.im.inspection.service.database.impl;

import com.im.inspection.entity.database.ImAaGlueHeartBeat;
import com.im.inspection.entity.database.ImAaGlueLog;
import com.im.inspection.entity.database.ImSparkJobInfo;
import com.im.inspection.mapper.database.OracleMapper;
import com.im.inspection.service.database.IOracleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/01/03 14:38:29
 */

@Service
public class OracleServiceImpl implements IOracleService {
    /**
     * @param jobName
     * @return
     */

    @Autowired
    private OracleMapper oracleMapper;

    @Override
    public ImSparkJobInfo getSparkJobInfo(String jobName) {
        return oracleMapper.getSparkJobInfo(jobName);
    }

    /**
     * @param imSparkJobInfo
     * @return
     */
    @Override
    public boolean updateSparkJobInfo(ImSparkJobInfo imSparkJobInfo) {
        return oracleMapper.updateSparkJobInfo(imSparkJobInfo) > 0;
    }

    /**
     * @param jobName
     * @return
     */
    @Override
    public String getSparkJobSql(String jobName) {
        return oracleMapper.getSparkJobSql(jobName);
    }

    /**
     * @param log
     * @return
     */
    @Override
    public boolean addGlueLog(ImAaGlueLog log) {
        return oracleMapper.addGlueLog(log);
    }

    /**
     * @param heartBeat
     * @return
     */
    @Override
    public boolean addGlueHeartBeat(ImAaGlueHeartBeat heartBeat) {
        return oracleMapper.addGlueHeartBeat(heartBeat);
    }
}
