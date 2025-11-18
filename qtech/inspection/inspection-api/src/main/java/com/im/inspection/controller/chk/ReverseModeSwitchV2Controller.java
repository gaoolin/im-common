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
@RequestMapping(value = "/im/ctrl-mode/v2")
public class ReverseModeSwitchV2Controller {

    /**
     * 更改控制模式。
     *
     * @param mode 新的控制模式字符串
     * @return 成功或错误的消息响应
     */
    @Operation(summary = "更改控制模式")
    @PostMapping("/{module}/{mode}")
    public ApiR<String> changeControlMode(
            @PathVariable("module") String module,
            @PathVariable("mode") String mode) {
        try {
            ControlMode newMode = ControlMode.valueOf(mode.toUpperCase());
            CtrlModeFlag.setControlMode(module, newMode);
            log.info("Module [{}] control mode updated to [{}]", module, newMode);
            return ApiR.success(String.format("Module [%s] control mode updated to [%s]", module, newMode), null);
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
    @GetMapping("/{module}/current")
    public ApiR<String> getControlMode(@PathVariable("module") String module) {
        return ApiR.success(String.format("Module [%s] current control mode is [%s]", module, CtrlModeFlag.getControlMode(module)), null);
    }

    /**
     * 获取控制模式的列表。
     *
     * @return 控制模式的列表的消息响应
     */
    @Operation(summary = "获取控制模式的列表")
    @GetMapping("/list")
    public ApiR<ControlMode[]> getControlModeList() {
        return ApiR.success(ControlMode.values());
    }
}