package com.im.inspection.controller.chk;

import com.im.inspection.util.chk.ControlMode;
import com.im.inspection.util.chk.CtrlModeFlag;
import com.im.inspection.util.response.ApiR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/17
 */
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "控制模式设置控制器", description = "控制模式设置控制器")
@RestController
@RequestMapping(value = "/im/aa/control-mode")
public class ReverseModeSwitchController {

    /**
     * 更改控制模式。
     *
     * @param mode 新的控制模式字符串
     * @return 成功或错误的消息响应
     */
    @Operation(summary = "更改控制模式")
    @PostMapping
    public ApiR<String> changeControlMode(@RequestParam("mode") String mode) {
        try {
            ControlMode newMode = ControlMode.valueOf(mode.toUpperCase());
            CtrlModeFlag.defaultControlMode = newMode;
            log.info(">>>>> Control mode updated to " + newMode);
            return ApiR.success("Control mode updated to " + newMode, null);
        } catch (IllegalArgumentException e) {
            return ApiR.badRequest("Invalid control mode");
        }
    }

    /**
     * 获取当前控制模式。
     *
     * @return 当前控制模式的消息响应
     */
    @Operation(summary = "获取当前控制模式")
    @GetMapping("/current")
    public ApiR<String> getControlMode() {
        return ApiR.success("Current control mode is: " + CtrlModeFlag.getControlMode("AA"), CtrlModeFlag.defaultControlMode.name());
    }

    /**
     * 获取控制模式的列表。
     *
     * @return 控制模式的列表的消息响应
     */
    @Operation(summary = "获取控制模式的列表")
    @GetMapping("/list")
    public ApiR<ControlMode[]> getControlModeList() {
        return ApiR.success("Current control mode list is: " + CtrlModeFlag.defaultControlMode, ControlMode.values());
    }
}