package com.microdev.common.exception;

/**
 * @author liutf
 */
public class ServiceNotFoundException extends AbstractBusinessException {

    private static String DEFAULT_EXCEPTION_MESSAGE = "该服务不存在";

    public ServiceNotFoundException() {
        super(DEFAULT_EXCEPTION_MESSAGE);
    }

    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotFoundException(Throwable cause) {
        super(cause);
    }

    public static String getDEFAULT_EXCEPTION_MESSAGE() {
        return DEFAULT_EXCEPTION_MESSAGE;
    }

    public static void setDEFAULT_EXCEPTION_MESSAGE(String DEFAULT_EXCEPTION_MESSAGE) {
        ServiceNotFoundException.DEFAULT_EXCEPTION_MESSAGE = DEFAULT_EXCEPTION_MESSAGE;
    }

}
