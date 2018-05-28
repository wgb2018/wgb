package com.microdev.common.exception;

/**
 * @author liutf
 */
public class ParamsException extends AbstractBusinessException {
    private static String DEFAULT_EXCEPTION_MESSAGE = "参数不正确";
    public ParamsException() {
        super(DEFAULT_EXCEPTION_MESSAGE);
    }

    public ParamsException(String message) {
        super(message);
    }

    public ParamsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParamsException(Throwable cause) {
        super(cause);
    }

    public ParamsException(String message, Object datail) {
        super(message, datail);
    }

    public ParamsException(String message, Throwable cause, Object datail) {
        super(message, cause, datail);
    }

    public ParamsException(Throwable cause, Object datail) {
        super(cause, datail);
    }
}
