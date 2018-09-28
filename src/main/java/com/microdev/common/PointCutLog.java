package com.microdev.common;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Component
@Aspect
public class PointCutLog {

    private static final Logger logger = LoggerFactory.getLogger(PointCutLog.class);

    @Pointcut("execution(* com.microdev.Controller.*.*(..))")
    public void pointCutMethod() {

    }

    /*@Before("pointCutMethod()")
    public void beforeRequest(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String uri = request.getRequestURI();
        String message = "访问类" + className + "的方法" + methodName + ";url是：" + uri + ";传递的参数是" + Arrays.toString(args);
        logger.info(message);
    }*/

    @Around("pointCutMethod()")
    public Object aroundMethod(ProceedingJoinPoint pjp) throws Throwable{
        Object result = null;
        Object[] args = null;

            long start = System.currentTimeMillis();
            args = pjp.getArgs();
            String className = pjp.getSignature().getDeclaringType().getName();
            String methodName = pjp.getSignature().getName();
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            String uri = request.getRequestURI();
            String message = "访问类" + className + "的方法" + methodName + ";url是：" + uri + ";传递的参数是" + Arrays.toString(args);
            logger.info(message);
            result = pjp.proceed();
            long end = System.currentTimeMillis();
            logger.info("方法" + uri + "花费的时间(ms):" + (end - start));

        return result;
    }
}
