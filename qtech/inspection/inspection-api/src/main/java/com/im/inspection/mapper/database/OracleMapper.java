package com.im.inspection.mapper.database;

import com.im.inspection.entity.database.ImAaGlueHeartBeat;
import com.im.inspection.entity.database.ImAaGlueLog;
import com.im.inspection.entity.database.ImSparkJobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/01/03 14:40:48
 */

@Mapper
public interface OracleMapper {
    public ImSparkJobInfo getSparkJobInfo(@Param("jobName") String jobName);

    public int updateSparkJobInfo(ImSparkJobInfo imSparkJobInfo);

    public String getSparkJobSql(@Param("jobName") String jobName);

    public boolean addGlueLog(ImAaGlueLog log);

    public boolean addGlueHeartBeat(ImAaGlueHeartBeat heartBeat);
}
