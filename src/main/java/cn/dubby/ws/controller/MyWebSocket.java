package cn.dubby.ws.controller;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket")
@Component
public class MyWebSocket {

    private static int onlineCount = 0;

    private static ConcurrentHashMap<String, Session> webSocketMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        webSocketMap.put(session.getId(), session);
        addOnlineCount();
        System.out.println("有新链接加入!当前在线人数为" + getOnlineCount());
    }

    @OnClose
    public void onClose(Session session) {
        webSocketMap.remove(session.getId());
        subOnlineCount();
        System.out.println("有一链接关闭!当前在线人数为" + getOnlineCount());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        if (message.contains("#")) {
            String[] msgMap = message.split("#");
            String targetId = msgMap[0];
            String msg = String.format("[%s]:\t%s", session.getId(), msgMap[1]);
            session.getBasicRemote().sendText(message + "[自己]");
            // 群发消息
            for (Session item : webSocketMap.values()) {
                if (item.getId().equals(targetId)) {
                    sendMessage(item, msg);
                }
            }
        } else {
            String msg = String.format("[%s]:\t%s", session.getId(), message);
            System.out.println(msg);
            session.getBasicRemote().sendText(msg + "[自己]");
            // 群发消息
            for (Session item : webSocketMap.values()) {
                if (!item.getId().equals(session.getId())) {
                    sendMessage(item, msg);
                }
            }
        }
    }

    public void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public static synchronized int getOnlineCount() {
        return MyWebSocket.onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }

}
