package com.anbao.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class sendFanoutGb {


    public  static void sendGb(String message,Integer flag) {
        //New一个RabbitMQ的连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置需要连接的RabbitMQ地址，这里指向本机
        factory.setHost("192.168.25.100");
        factory.setUsername("hadoop");
        factory.setPassword("hadoop");
        String data = flag+" "+message;
        try {
            //尝试获取一个连接
            Connection connection = factory.newConnection();
            //尝试创建一个channel
            Channel channel = connection.createChannel();
            //声明交换机（参数为：交换机名称; 交换机类型，广播模式）
            channel.exchangeDeclare("change", "fanout");
            //消息发布（参数为：交换机名称; routingKey，忽略。在广播模式中，生产者声明交换机的名称和类型即可）
            channel.basicPublish("change","", null,data.getBytes("UTF-8"));
            channel.close();
            connection.close();
        } catch (IOException |TimeoutException e) {
            e.printStackTrace();
        }
    }

}
