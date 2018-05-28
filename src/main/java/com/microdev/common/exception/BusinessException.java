package com.microdev.common.exception;

/**
 * @author yinbaoxin 业务逻辑异常
 */
public class BusinessException extends AbstractBusinessException {
    private static String DEFAULT_EXCEPTION_MESSAGE = "业务逻辑异常";
    public BusinessException() {
        super(DEFAULT_EXCEPTION_MESSAGE);
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Object datail) {
        super(message, datail);
    }

    public BusinessException(String message, Throwable cause, Object datail) {
        super(message, cause, datail);
    }

    public BusinessException(Throwable cause, Object datail) {
        super(cause, datail);
    }
}
