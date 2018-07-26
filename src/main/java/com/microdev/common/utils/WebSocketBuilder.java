package com.microdev.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.java_websocket.WebSocket;

/**
 *
 * 保存ws对象
 *
 * @version 1.0
 * @since JDK1.7
 * @author yaomy
 * @date 2018年1月8日 下午5:00:39
 */
public class WebSocketBuilder {

    public static Map<WebSocket, String> wsMap = new ConcurrentHashMap<>();

    /**
     *
     * 方法描述 新增WS对象
     *
     * @param ws
     *
     * @author yaomy
     * @date 2018年1月8日 下午5:01:08
     */
    public static void addWs(WebSocket ws, String user) {
        if(ws != null && !wsMap.containsKey(ws)) {
            wsMap.put(ws, user);
        }
        System.out.println(wsMap);
    }
    /**
     *
     * 方法描述 删除ws对象
     *
     * @param ws
     *
     * @author yaomy
     * @date 2018年1月8日 下午5:01:24
     */
    public static void removeWs(WebSocket ws) {
        if(ws != null && wsMap.containsKey(ws)) {
            wsMap.remove(ws);
        }
    }


}
