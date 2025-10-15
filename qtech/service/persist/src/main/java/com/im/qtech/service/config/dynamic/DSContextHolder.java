package com.im.qtech.service.config.dynamic;

/**
 * 当前线程数据源上下文
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/15
 */

public class DSContextHolder {
    private static final ThreadLocal<DSName> CONTEXT = ThreadLocal.withInitial(() -> DSName.FIRST);

    public static void set(DSName ds) {
        CONTEXT.set(ds);
    }

    public static DSName get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}