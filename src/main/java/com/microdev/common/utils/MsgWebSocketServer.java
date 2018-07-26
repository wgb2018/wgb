package com.microdev.common.utils;

import java.net.InetSocketAddress;
import java.util.Iterator;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsgWebSocketServer extends WebSocketServer{
    private static final Logger logger = LoggerFactory.getLogger(MsgWebSocketServer.class);
    public MsgWebSocketServer(int port) {
        // TODO Auto-generated constructor stub
        super(new InetSocketAddress(port));
    }
    /**
     * 在WebSocket连接已关闭时调用。
     */
    @Override
    public void onClose(WebSocket ws, int arg1, String arg2, boolean arg3) {
        // TODO Auto-generated method stub
        System.out.println("------------------onClose-------------------"+ws.isClosed()+"---\n"+
                arg1+"---"+arg2+"---"+arg3);
        WebSocketBuilder.removeWs(ws);
        System.out.println (WebSocketBuilder.wsMap);
    }

    /**
     * 错误发生时调用。
     */
    @Override
    public void onError(WebSocket ws, Exception e) {
        // TODO Auto-generated method stub
        System.out.println("------------------onError-------------------");
        if(ws != null) {
            WebSocketBuilder.removeWs(ws);
            System.out.println (WebSocketBuilder.wsMap);
        }
        e.getStackTrace();

    }

    /**
     * 对从远程主机接收的字符串消息进行回调
     */
    @Override
    public void onMessage(WebSocket ws, String msg) {
        // TODO Auto-generated method stub
        // TODO Auto-generated method stub
        System.out.println("------------------onMessage-------------------连接状态："+ws.getReadyState()+
                "---本地SOCKET地址："+ws.getLocalSocketAddress()+"--客户端SOCKET地址："+ws.getRemoteSocketAddress()+
                +ws.getDraft().INITIAL_FAMESIZE+"--"+ws.getDraft().MAX_FAME_SIZE+
                "---"+ws.DEFAULT_PORT+"---"+ws.DEFAULT_WSS_PORT+"---"+ws.getRemoteSocketAddress().getAddress().getCanonicalHostName()+
                "---"+ws.getRemoteSocketAddress().getPort()+"---"+ws.getRemoteSocketAddress().getHostName()+"---"+ws.getRemoteSocketAddress().getHostString()+"---"+"\n"+
                "---"+ws.getLocalSocketAddress().getPort()+"---"+ws.getLocalSocketAddress().getHostName()+"---"+ws.getLocalSocketAddress().getHostString()+"---"+ws.getLocalSocketAddress().getAddress()+"---"+
                "---"+ws.getLocalSocketAddress().getAddress());
        if(ws.isClosed()) {
            logger.info("ws连接已关闭...");
        } else if (ws.isClosing()) {
            logger.info("ws连接正在关闭...");
        } else if (ws.isConnecting()) {
            logger.info("ws正在连接中...");
        } else if(ws.isOpen()) {
            logger.info("ws连接已打开...");
            System.out.println(msg);
        }
        System.out.println (WebSocketBuilder.wsMap);
    }

    /**
     * 在websocket进行握手之后调用，并且给WebSocket写做准备
     * 通过握手可以获取请求头信息
     */
    @Override
    public void onOpen(WebSocket ws, ClientHandshake shake) {
        // TODO Auto-generated method stub
        for(Iterator<String> it=shake.iterateHttpFields();it.hasNext();) {
            String key = it.next();
            System.out.println(key+":"+shake.getFieldValue(key));
        }
    }
    public void onSend(WebSocket ws, String msg){
           ws.send (msg);
    }
}

