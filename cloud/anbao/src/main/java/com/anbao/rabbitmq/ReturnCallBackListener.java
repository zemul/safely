package com.anbao.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 无法发送到指定的queue
 * return 则表示如果你的消息已经正确到达交换机，但是后续处理出错了，那么就会回调 return，并且把信息送回给你（前提是需要设置了 Mandatory，不设置那么就丢弃）；如果消息没有到达交换机，那么不会调用 return 的东西。
 * {@link }
 */
public class ReturnCallBackListener implements RabbitTemplate.ReturnCallback {
    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        System.out.println("ReturnCallBackListener中，消息[" + new String(message.getBody()) + "]发送失败");
    }
}
