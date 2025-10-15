package org.im.semiconductor.common.parameter.core;

/**
 * 参数验证接口
 * 提供参数有效性验证功能
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public interface ValidatableParameter {
    /**
     * 验证参数有效性
     *
     * @return 验证结果
     */
    ValidationResult validate();

    /**
     * 验证结果内部类
     */
    class ValidationResult {
        private boolean valid;
        private String message;

        // 构造函数、getter和setter方法
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}