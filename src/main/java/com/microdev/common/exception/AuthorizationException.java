package com.microdev.common.exception;

/**
 * @author liutf
 */
public class AuthorizationException extends AbstractBusinessException {

    private static String DEFAULT_EXCEPTION_MESSAGE = "Unauthorized";

    public AuthorizationException() {
        super(DEFAULT_EXCEPTION_MESSAGE);
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizationException(Throwable cause) {
        super(cause);
    }

    public static String getDEFAULT_EXCEPTION_MESSAGE() {
        return DEFAULT_EXCEPTION_MESSAGE;
    }

    public static void setDEFAULT_EXCEPTION_MESSAGE(String DEFAULT_EXCEPTION_MESSAGE) {
        AuthorizationException.DEFAULT_EXCEPTION_MESSAGE = DEFAULT_EXCEPTION_MESSAGE;
    }

}
