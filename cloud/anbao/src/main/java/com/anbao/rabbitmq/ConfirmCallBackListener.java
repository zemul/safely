package com.anbao.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

/**
 * 无法发送到指定的exchange
 * confirm 主要是用来判断消息是否有正确到达交换机，如果有，那么就 ack 就返回 true；如果没有，则是 false。
 */
public class ConfirmCallBackListener implements RabbitTemplate.ConfirmCallback {

    @Override
    public void confirm(CorrelationData correlationData, boolean b) {
        if (b) {

        } else {
            System.out.println("ConfirmCallBackListener中，发送时没有抵达正确的exchange");
        }
    }
}
