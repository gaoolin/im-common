package com.im.inspection.controller.database;

import com.im.inspection.entity.database.ImAaGlueHeartBeat;
import com.im.inspection.entity.database.ImAaGlueLog;
import com.im.inspection.entity.database.ImSparkJobInfo;
import com.im.inspection.service.database.IOracleService;
import com.im.inspection.util.response.ApiR;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/01/03 14:30:19
 */

@RestController
@RequestMapping("/im/db/oracle")
public class OracleController {

    @Resource
    private IOracleService oracleService;

    @GetMapping("/sparkJobInfo/{jobName}")
    public ApiR<ImSparkJobInfo> getSparkJobInfo(@PathVariable String jobName) {
        ImSparkJobInfo sparkJobInfo = oracleService.getSparkJobInfo(jobName);
        return ApiR.success("success", sparkJobInfo);
    }

    @PutMapping("/sparkJobInfo/{jobName}")
    public ApiR<Boolean> updateSparkJobInfo(@PathVariable String jobName, @RequestBody ImSparkJobInfo imSparkJobInfo) {
        imSparkJobInfo.setJobName(jobName);
        return ApiR.success("success", oracleService.updateSparkJobInfo(imSparkJobInfo));
    }

    @GetMapping("/sparkJobSql/{jobName}")
    public ApiR<String> getSparkJobSql(@PathVariable String jobName) {
        String sql = oracleService.getSparkJobSql(jobName);
        return ApiR.success("success", sql);
    }

    @PutMapping("/aa/glue/log")
    public ApiR<Boolean> addGlueLog(@RequestBody ImAaGlueLog log) {
        return ApiR.success("success", oracleService.addGlueLog(log));
    }

    @PutMapping("/aa/glue/heartBeat")
    public ApiR<Boolean> addGlueHeartBeat(@RequestBody ImAaGlueHeartBeat heartBeat) {
        return ApiR.success("success", oracleService.addGlueHeartBeat(heartBeat));
    }
}