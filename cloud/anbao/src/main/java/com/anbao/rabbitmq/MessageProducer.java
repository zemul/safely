package com.anbao.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 功能概要：消息产生,提交到队列中去
 */
@Service
public class MessageProducer {



    @Resource
    private RabbitTemplate amqpTemplate;
    @Resource
    private RabbitTemplate amqpTemplate2;
    @Resource
    private RabbitTemplate amqpTemplate3;




    @Test
    public void sendMessage4() throws IOException, TimeoutException {
       ConnectionFactory factory = new ConnectionFactory();
        //设置需要连接的RabbitMQ地址，这里指向本机
        factory.setHost("192.168.25.100");
        factory.setUsername("hadoop");
        factory.setPassword("hadoop");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        String message = "drriver aaabbbcc is  died";
        //声明交换机（参数为：交换机名称; 交换机类型，广播模式）
        channel.exchangeDeclare("change1", "fanout");
        //消息发布（参数为：交换机名称; routingKey，忽略。在广播模式中，生产者声明交换机的名称和类型即可）
        channel.basicPublish("change1","", null,message.getBytes());
        channel.close();
        connection.close();



    }

    public void sendMessage(Object message) {
        System.out.println("1发送：" + message);
        amqpTemplate.convertAndSend(message);
    }

    public void sendMessage2(Object message) {
        System.out.println("2发送：" + message);
        amqpTemplate2.convertAndSend(message);
    }

    public void sendMessage3(final String routingKey, final Object message) {
        System.out.println("3发送：[" + message + "] routing-key = [" + routingKey + "]");
        amqpTemplate3.convertAndSend(routingKey, message);
//        for (int i = 1; i < 3000000; i++) {
//            System.out.println("3发送：[" + message + "] routing-key = [" + routingKey + "]" + i);
//            amqpTemplate3.convertAndSend(routingKey, message);
//        }
    }
}
