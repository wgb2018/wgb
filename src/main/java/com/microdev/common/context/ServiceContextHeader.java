package com.microdev.common.context;

import com.alibaba.fastjson.JSON;
import org.springframework.http.HttpHeaders;

/**
 * @author liutf
 */
public class ServiceContextHeader extends HttpHeaders {
    public void setCallId(CallId callId) {
        add(CALLID, JSON.toJSONString(callId));
    }

    public CallId getCallId() {
        if (get(CALLID) != null) {
            return JSON.parseObject(get(CALLID).get(0), CallId.class);
        } else {
            return null;
        }

    }

    public static final String CALLID = "Service-Context-CallId";
}
