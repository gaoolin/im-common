package org.im.semiconductor.common.context.test;

/**
 * 测试相关信息
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface TestInfo {
    String getTestProgram(); // 测试程序名称

    String getWafer();       // 晶圆片号

    String getDie();         // 芯片位置
}