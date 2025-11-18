package com.im.inspection.service.database;

import com.im.inspection.entity.database.ImAaGlueHeartBeat;
import com.im.inspection.entity.database.ImAaGlueLog;
import com.im.inspection.entity.database.ImSparkJobInfo;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/01/03 14:38:03
 */

public interface IOracleService {
    public ImSparkJobInfo getSparkJobInfo(String jobName);

    public boolean updateSparkJobInfo(ImSparkJobInfo imSparkJobInfo);

    public String getSparkJobSql(String jobName);

    public boolean addGlueLog(ImAaGlueLog log);

    public boolean addGlueHeartBeat(ImAaGlueHeartBeat heartBeat);
}
