package com.microdev.common.exception;

/**
 * @author liutf
 */
public class AuthenticationException extends AbstractBusinessException {
    private static String DEFAULT_EXCEPTION_MESSAGE = "认证失败异常";

    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    public AuthenticationException(String message, Object dateil) {
        super(message, dateil);
    }

    public AuthenticationException(String message, Throwable cause, Object dateil) {
        super(message, cause, dateil);
    }

    public AuthenticationException(Throwable cause, Object dateil) {
        super(cause, dateil);
    }

    public static String getDEFAULT_EXCEPTION_MESSAGE() {
        return DEFAULT_EXCEPTION_MESSAGE;
    }

    public static void setDEFAULT_EXCEPTION_MESSAGE(String DEFAULT_EXCEPTION_MESSAGE) {
        AuthenticationException.DEFAULT_EXCEPTION_MESSAGE = DEFAULT_EXCEPTION_MESSAGE;
    }
}
