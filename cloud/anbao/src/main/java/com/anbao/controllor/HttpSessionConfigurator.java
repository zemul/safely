package com.anbao.controllor;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * --------------------------------------------------------------
 * CopyRights(c)2018,YJLC
 * All Rights Reserved
 * <p>
 * FileName: HttpSessionConfigurator.java
 * Description:配置类  将http中的session传入websocket中
 * Author: cyb
 * CreateDate: 2018-11-26
 * --------------------------------------------------------------
 */
public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec,
                                HandshakeRequest request, HandshakeResponse response) {
        // 获取当前Http连接的session
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        // 将http session信息注入websocket session
        sec.getUserProperties().put(HttpSession.class.getName(), httpSession);
    }
}