package com.anbao.controllor;


import org.springframework.web.bind.annotation.CrossOrigin;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@ServerEndpoint("/webSocket/{mac}")
@CrossOrigin(origins = {"*"}, maxAge = 3600)
public class WebSocketControllor {
    //用来存放每个客户端对应的MyWebSocket对象。
    public static Map<String, WebSocketControllor> clients = new ConcurrentHashMap();
    public  Session session;
    public String mac;


    /**
     *@description: 连接websocket
     *@author:cyb
     *@date: 2018-12-03 19:12
     *@param: userId 用户id
    *@param: cnName 用户真实名称
    *@param: session
     *@return: void
     */
    @OnOpen
    public void onOpen(@PathParam("mac") String mac, Session session) throws IOException {
        this.mac = mac;
        this.session = session;
        System.out.println("mac:"+mac+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        clients.put(mac, this);
        singleSend("0",mac);
    }



    /**
     * @ClassName: onClose
     * @Description: 连接关闭的操作
     */
    @OnClose
    public void onClose(){
        clients.remove(mac);
        System.out.println("close"+mac);
    }


    /**
     * @ClassName: OnError
     * @Description: 出错的操作
     */
    @OnError
    public void onError(Throwable error){
        System.out.println(error);
        error.printStackTrace();
    }




    /**
     * 对特定用户发送消息
     * @param message
     */
    public void singleSend(String message, String mac) throws IOException {
        for(WebSocketControllor ws : WebSocketControllor.clients.values()){
            if(ws.mac.equals(mac)){
                ws.session.getBasicRemote().sendText(message);
            }

        }



    }

    public static  synchronized  Map<String, WebSocketControllor> getClients() {
        return clients;
    }

}
