package com.microdev.common.utils;
public enum WebServerEnum {

    server;

    private static MsgWebSocketServer socketServer = null;

    public static MsgWebSocketServer init(MsgWebSocketServer server) {
        socketServer = server;
        if (socketServer != null) {
            socketServer.start ( );
        }
        return socketServer;
    }
}
