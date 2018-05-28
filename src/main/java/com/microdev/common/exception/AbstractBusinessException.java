package com.microdev.common.exception;

/**
 * 业务逻辑异常基类
 * <p/>
 * 所有业务系统能够捕获的异常都要转换为具体类型的业务逻辑异常抛出，如OrderNotFoundException
 *
 * @author liutf
 */
public abstract class AbstractBusinessException extends RuntimeException {
    private static String DEFAULT_EXCEPTION_MESSAGE = "业务逻辑异常";
    private Object detail;

    public AbstractBusinessException() {
        super(DEFAULT_EXCEPTION_MESSAGE);
    }

    public AbstractBusinessException(String message) {
        super(message);
    }

    public AbstractBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbstractBusinessException(Throwable cause) {
        super(cause);
    }

    public AbstractBusinessException(String message, Object detail) {
        super(message);
        this.detail = detail;
    }

    public AbstractBusinessException(String message, Throwable cause, Object detail) {
        super(message, cause);
        this.detail = detail;
    }

    public AbstractBusinessException(Throwable cause, Object detail) {
        super(cause);
        this.detail = detail;
    }

    public static String getDEFAULT_EXCEPTION_MESSAGE() {
        return DEFAULT_EXCEPTION_MESSAGE;
    }

    public static void setDEFAULT_EXCEPTION_MESSAGE(String DEFAULT_EXCEPTION_MESSAGE) {
        AbstractBusinessException.DEFAULT_EXCEPTION_MESSAGE = DEFAULT_EXCEPTION_MESSAGE;
    }

    public Object getDetail() {
        return detail;
    }

    public void setDetail(Object detail) {
        this.detail = detail;
    }
}
