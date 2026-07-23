package com.anbao.controllor;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/webSocket/{mac}")
public class WebSocketControllor {

    private static final String API_KEY;
    static {
        String key = System.getenv("DEVICE_API_KEY");
        API_KEY = (key != null && !key.trim().isEmpty()) ? key : "dev-unsafe-default-key";
    }

    public static Map<String, WebSocketControllor> clients = new ConcurrentHashMap<>();
    public Session session;
    public String mac;

    @OnOpen
    public void onOpen(@PathParam("mac") String mac, Session session) throws IOException {
        // 验证 token 参数
        String token = session.getRequestParameterMap().containsKey("token")
                ? session.getRequestParameterMap().get("token").get(0)
                : null;

        if (token == null || !API_KEY.equals(token)) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "unauthorized"));
            return;
        }

        this.mac = mac;
        this.session = session;
        clients.put(mac, this);
        singleSend("0", mac);
    }

    @OnClose
    public void onClose() {
        if (mac != null) {
            clients.remove(mac);
        }
    }

    @OnError
    public void onError(Throwable error) {
        System.err.println("[WebSocket] Error: " + error.getMessage());
    }

    public void singleSend(String message, String mac) throws IOException {
        WebSocketControllor ws = clients.get(mac);
        if (ws != null && ws.session != null && ws.session.isOpen()) {
            ws.session.getBasicRemote().sendText(message);
        }
    }

    public static synchronized Map<String, WebSocketControllor> getClients() {
        return clients;
    }
}
