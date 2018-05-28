package com.microdev.common.context;

import com.alibaba.fastjson.JSON;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author liutf
 */
public class ServiceContext extends HashMap<String, Object> {
    public void setUser(User user) {
        put(USER, user);
    }

    public User getUser() {
        if (get(USER) != null) {
            return (User) get(USER);
        } else {
            return null;
        }

    }

    public void setHeader(HttpServletRequest request) {
        ServiceContextHeader serviceContextHeader = this.getHeader();

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String k = (String) headerNames.nextElement();
            String v = request.getHeader(k);
            if (k.toLowerCase().equals(ServiceContextHeader.CALLID.toLowerCase())) {
                CallId callId = JSON.parseObject(v, CallId.class);
                serviceContextHeader.setCallId(callId);
            } else if (k.toLowerCase().startsWith(PREFIX.toLowerCase())) {
                serviceContextHeader.add(k, v);
            } else if (k.toLowerCase().equals(Authorization.toLowerCase())) {
                serviceContextHeader.add(k, v);
            } else {
                //没有以 Service-Context 打头的 header 不会被放入上下文对象
            }

        }


        CallId callId = serviceContextHeader.getCallId();
        String thisId = UUID.randomUUID().toString();
        if (callId != null) {
            callId.setParent(callId.getId());
            callId.setId(thisId);
        } else {
            CallId newCallId = new CallId();
            newCallId.setId(thisId);
            newCallId.setParent(thisId);
            newCallId.setRoot(thisId);
            serviceContextHeader.setCallId(newCallId);
        }


        put(HEADER, serviceContextHeader);
    }

    public ServiceContextHeader getHeader() {
        if (get(HEADER) != null) {
            return (ServiceContextHeader) get(HEADER);
        } else {
            return new ServiceContextHeader();
        }

    }

    public void setHttpServletRequest(HttpServletRequest request) {
        put(HTTP_SERVLET_REQUEST, request);
    }

    public HttpServletRequest getHttpServletRequest() {
        if (get(HTTP_SERVLET_REQUEST) != null) {
            return (HttpServletRequest) get(HTTP_SERVLET_REQUEST);
        } else {
            return null;
        }

    }

    public void setHttpServletResponse(HttpServletResponse response) {
        put(HTTP_SERVLET_RESPONSE, response);
    }

    public HttpServletResponse getHttpServletResponse() {
        if (get(HTTP_SERVLET_RESPONSE) != null) {
            return (HttpServletResponse) get(HTTP_SERVLET_RESPONSE);
        } else {
            return null;
        }

    }

    private static final String PREFIX = "Service-Context";
    private static final String Authorization = "Authorization";
    private static final String USER = "Service-Context-User";
    private static final String HEADER = "Service-Context-HttpHeader";
    private static final String HTTP_SERVLET_REQUEST = "Service-Context-HttpRequest";
    private static final String HTTP_SERVLET_RESPONSE = "Service-Context-HttpResponse";
}
