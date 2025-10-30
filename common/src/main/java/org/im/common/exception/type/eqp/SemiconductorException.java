package org.im.common.exception.type.eqp;

import org.im.common.exception.type.BaseException;

/**
 * 半导体行业专用异常类
 * <p>
 * 特性：
 * - 通用性：覆盖半导体制造各环节异常
 * - 规范性：统一的异常码和消息格式
 * - 专业性：半导体行业标准异常分类
 * - 灵活性：支持详细错误信息和上下文
 * - 可靠性：完善的异常链和堆栈跟踪
 * - 安全性：敏感信息保护
 * - 复用性：层次化异常结构
 * - 容错性：支持恢复和重试机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/20
 */
public class SemiconductorException extends BaseException {
    private static final long serialVersionUID = 1L;
    private final ExceptionType exceptionType;

    public SemiconductorException(ExceptionType exceptionType, String errorCode, String errorMessage) {
        super(exceptionType.getCodePrefix() + "-" + errorCode, errorMessage);
        this.exceptionType = exceptionType;
    }

    public SemiconductorException(ExceptionType exceptionType, String errorCode, String errorMessage, Throwable cause) {
        super(exceptionType.getCodePrefix() + "-" + errorCode, errorMessage, cause);
        this.exceptionType = exceptionType;
    }

    public SemiconductorException(ExceptionType exceptionType, String errorCode, String errorMessage,
                                  Object errorDetails, Throwable cause) {
        super(exceptionType.getCodePrefix() + "-" + errorCode, errorMessage, errorDetails, cause);
        this.exceptionType = exceptionType;
    }

    // Getters
    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    // 异常类型枚举
    public enum ExceptionType {
        TEST_ERROR("T", "Test Error"),
        EQUIPMENT_ERROR("E", "Equipment Error"),
        PROCESS_ERROR("P", "Process Error"),
        QUALITY_ERROR("Q", "Quality Error"),
        MATERIAL_ERROR("M", "Material Error"),
        SYSTEM_ERROR("S", "System Error");

        private final String codePrefix;
        private final String description;

        ExceptionType(String codePrefix, String description) {
            this.codePrefix = codePrefix;
            this.description = description;
        }

        public String getCodePrefix() {
            return codePrefix;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 测试异常
     */
    public static class TestException extends SemiconductorException {
        public TestException(String errorCode, String errorMessage) {
            super(ExceptionType.TEST_ERROR, errorCode, errorMessage);
        }

        public TestException(String errorCode, String errorMessage, Throwable cause) {
            super(ExceptionType.TEST_ERROR, errorCode, errorMessage, cause);
        }

        public TestException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
            super(ExceptionType.TEST_ERROR, errorCode, errorMessage, errorDetails, cause);
        }
    }

    /**
     * 设备异常
     */
    public static class EquipmentException extends SemiconductorException {
        public EquipmentException(String errorCode, String errorMessage) {
            super(ExceptionType.EQUIPMENT_ERROR, errorCode, errorMessage);
        }

        public EquipmentException(String errorCode, String errorMessage, Throwable cause) {
            super(ExceptionType.EQUIPMENT_ERROR, errorCode, errorMessage, cause);
        }

        public EquipmentException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
            super(ExceptionType.EQUIPMENT_ERROR, errorCode, errorMessage, errorDetails, cause);
        }
    }

    /**
     * 工艺异常
     */
    public static class ProcessException extends SemiconductorException {
        public ProcessException(String errorCode, String errorMessage) {
            super(ExceptionType.PROCESS_ERROR, errorCode, errorMessage);
        }

        public ProcessException(String errorCode, String errorMessage, Throwable cause) {
            super(ExceptionType.PROCESS_ERROR, errorCode, errorMessage, cause);
        }

        public ProcessException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
            super(ExceptionType.PROCESS_ERROR, errorCode, errorMessage, errorDetails, cause);
        }
    }

    /**
     * 质量异常
     */
    public static class QualityException extends SemiconductorException {
        public QualityException(String errorCode, String errorMessage) {
            super(ExceptionType.QUALITY_ERROR, errorCode, errorMessage);
        }

        public QualityException(String errorCode, String errorMessage, Throwable cause) {
            super(ExceptionType.QUALITY_ERROR, errorCode, errorMessage, cause);
        }

        public QualityException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
            super(ExceptionType.QUALITY_ERROR, errorCode, errorMessage, errorDetails, cause);
        }
    }

    /**
     * 物料异常
     */
    public static class MaterialException extends SemiconductorException {
        public MaterialException(String errorCode, String errorMessage) {
            super(ExceptionType.MATERIAL_ERROR, errorCode, errorMessage);
        }

        public MaterialException(String errorCode, String errorMessage, Throwable cause) {
            super(ExceptionType.MATERIAL_ERROR, errorCode, errorMessage, cause);
        }

        public MaterialException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
            super(ExceptionType.MATERIAL_ERROR, errorCode, errorMessage, errorDetails, cause);
        }
    }

    /**
     * 系统异常
     */
    public static class SystemException extends SemiconductorException {
        public SystemException(String errorCode, String errorMessage) {
            super(ExceptionType.SYSTEM_ERROR, errorCode, errorMessage);
        }

        public SystemException(String errorCode, String errorMessage, Throwable cause) {
            super(ExceptionType.SYSTEM_ERROR, errorCode, errorMessage, cause);
        }

        public SystemException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
            super(ExceptionType.SYSTEM_ERROR, errorCode, errorMessage, errorDetails, cause);
        }
    }
}
