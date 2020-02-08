package com.anbao.rabbitmq;

import com.anbao.controllor.WebSocketControllor;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;

import java.io.IOException;

public class MessageConsumer implements ChannelAwareMessageListener {

//异常人流量
    @Override
    @Test
    public void onMessage(Message message, Channel channel) throws Exception {
        String info = new String(message.getBody());
        System.out.println(info);
        String[] s = info.split(" ");
        String mac = s[0];
        String peopleNum =s[1];
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        //获取session
        WebSocketControllor websocket = WebSocketControllor.clients.get(mac);
        if(websocket!=null){
            websocket.session.getBasicRemote().sendText(peopleNum);
        }

    }
    @Test
    public void aaa() throws IOException {
        String mac = "AAABBBCCC";
        String peopleNum ="20";
        System.out.println("count"+ WebSocketControllor.clients.size());
        WebSocketControllor webSocket = WebSocketControllor.clients.get(mac);
        if(webSocket!=null){
            webSocket.singleSend(peopleNum,mac);
        }
    }
}