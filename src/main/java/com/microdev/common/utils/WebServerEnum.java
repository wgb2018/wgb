package com.microdev.common.utils;

import org.java_websocket.WebSocket;

public enum WebServerEnum {

    server;

    private static MsgWebSocketServer socketServer = null;

    public static void init(MsgWebSocketServer server) {
        socketServer = server;
        if (socketServer != null) {
            socketServer.start ( );
        }
    }
}
