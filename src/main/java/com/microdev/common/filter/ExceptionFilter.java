package com.microdev.common.filter;

import com.alibaba.fastjson.JSON;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.AuthenticationException;
import com.microdev.common.exception.AuthorizationException;
import com.microdev.common.exception.ParamsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.NestedServletException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author liutf
 */
@Order(-1000)
@Component
public class ExceptionFilter implements Filter {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            chain.doFilter(request, response);
        } catch (NestedServletException e) {
            log.error(e.getCause().getMessage(), e.getCause());
            if (e.getCause() instanceof ParamsException) {
                responseException(response, 400, e.getCause().getMessage());
            } else {
                responseException(response, 500, e.getCause().getMessage());
            }
        } catch (AuthenticationException e) {
            log.debug(e.getMessage(), e);
            responseException(response, 401, e.getMessage());
        } catch (AuthorizationException e) {
            log.debug(e.getMessage(), e);
            responseException(response, 403, e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            responseException(response, 500, e.getMessage());
        }

    }

    private void responseException(HttpServletResponse response, Integer statusCode, String responseObj) throws IOException {
        PrintWriter out = response.getWriter();
        response.setStatus(statusCode);
        response.setContentLength(-1);
        response.setContentType("application/json;UTF-8");
        response.setDateHeader("expires", -1);
        response.setHeader("cache-control", "no-cach");
        response.setHeader("pragram", "no-cach");
        out.write(JSON.toJSONString(ResultDO.buildError(responseObj)));
        out.flush();
        out.close();
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {

    }
}

@ControllerAdvice
@ResponseBody
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(Exception.class)
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (e instanceof ParamsException) {
            log.error(e.getMessage(), e);
            status = HttpStatus.BAD_REQUEST;//400
        } else if (e instanceof AuthenticationException) {
            log.debug(e.getMessage(), e);
            status = HttpStatus.UNAUTHORIZED;//401
        } else if (e instanceof AuthorizationException) {
            log.debug(e.getMessage(), e);
            status = HttpStatus.FORBIDDEN;//403
        }
        return new ResponseEntity<>(ResultDO.buildError(e.getMessage()), status);
    }
}