package cn.dubby.ws.controller;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ServerEndpoint("/websocket")
@Component
public class MyWebSocket {

    private static AtomicLong onlineCount = new AtomicLong();

    private static ConcurrentHashMap<String, Session> webSocketMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        webSocketMap.put(session.getId(), session);
        System.out.println("有新链接加入!当前在线人数为:\t" + onlineCount.incrementAndGet());
    }

    @OnClose
    public void onClose(Session session) {
        webSocketMap.remove(session.getId());
        System.out.println("有一链接关闭!当前在线人数为:\t" + onlineCount.decrementAndGet());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
//        System.out.println("收到消息:【" + message + "】,Thread ID:" + Thread.currentThread().getId() + ",Thread Name:" + Thread.currentThread().getName());
        if (message.contains("#")) {
            String[] msgMap = message.split("#");
            String targetId = msgMap[0];
            String msg = String.format("[%s]:\t%s", session.getId(), msgMap[1]);
            session.getBasicRemote().sendText(message + "[self]");
            // 单发消息
            for (Session item : webSocketMap.values()) {
                if (item.getId().equals(targetId)) {
                    sendMessage(item, msg);
                }
            }
        } else {
            String msg = String.format("[%s]:\t%s", session.getId(), message);
            session.getBasicRemote().sendText(msg + "[self]");
            // 群发消息
            for (Session item : webSocketMap.values()) {
                if (!item.getId().equals(session.getId())) {
                    sendMessage(item, msg);
                }
            }
        }
    }

    private void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

}
