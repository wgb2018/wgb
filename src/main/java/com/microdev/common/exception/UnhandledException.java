package com.microdev.common.exception;

/**
 * @author liutf
 */
public class UnhandledException extends AbstractBusinessException {
    private static String DEFAULT_EXCEPTION_MESSAGE = "系统未知异常";

    public UnhandledException() {
        super(DEFAULT_EXCEPTION_MESSAGE);
    }

    public UnhandledException(String message) {
        super(message);
    }

    public UnhandledException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnhandledException(Throwable cause) {
        super(cause);
    }

    public static String getDEFAULT_EXCEPTION_MESSAGE() {
        return DEFAULT_EXCEPTION_MESSAGE;
    }

    public static void setDEFAULT_EXCEPTION_MESSAGE(String DEFAULT_EXCEPTION_MESSAGE) {
        UnhandledException.DEFAULT_EXCEPTION_MESSAGE = DEFAULT_EXCEPTION_MESSAGE;
    }
}
