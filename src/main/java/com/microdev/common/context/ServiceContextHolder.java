package com.microdev.common.context;

/**
 * @author liutf
 */
public class ServiceContextHolder {
    public static void setServiceContext(ServiceContext serviceContext) {
        threadLocal.set(serviceContext);
    }

    public static ServiceContext getServiceContext() {
        return threadLocal.get();
    }

    private static InheritableThreadLocal<ServiceContext> threadLocal = new InheritableThreadLocal<ServiceContext>() {
    };
}
