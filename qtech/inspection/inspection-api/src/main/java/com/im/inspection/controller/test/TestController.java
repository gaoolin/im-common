package com.im.inspection.controller.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/01/09 14:03:08
 * desc   :
 */

@RestController
@RequestMapping("/im/protected")
public class TestController {
    @RequestMapping("/test")
    public String test() {
        return "test";
    }
}
