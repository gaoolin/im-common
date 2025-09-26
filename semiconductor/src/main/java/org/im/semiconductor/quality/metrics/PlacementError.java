package org.im.semiconductor.quality.metrics;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public enum PlacementError {
    PICKUP_FAILURE("吸料失败"),
    DROP_PART("抛料"),
    POSITION_ERROR("位置偏差"),
    ROTATION_ERROR("旋转错误"),
    HEIGHT_ERROR("高度错误"),
    VACUUM_ERROR("真空异常"),
    NOZZLE_ERROR("吸嘴异常");

    private final String description;

    PlacementError(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
