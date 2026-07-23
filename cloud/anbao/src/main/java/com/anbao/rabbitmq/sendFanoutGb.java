package com.anbao.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;


public class sendFanoutGb {

    private static final String MQ_SECRET;
    static {
        String secret = System.getenv("MQ_SIGNING_SECRET");
        if (secret == null || secret.trim().isEmpty()) {
            secret = System.getenv("DEVICE_API_KEY");
        }
        MQ_SECRET = (secret != null && !secret.trim().isEmpty()) ? secret : "dev-unsafe-default-key";
    }

    public static void sendGb(String message, Integer flag) {
        // New一个RabbitMQ的连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置需要连接的RabbitMQ地址
        factory.setHost("192.168.25.100");
        factory.setUsername("hadoop");
        factory.setPassword("hadoop");

        String payload = flag + " " + message;
        // HMAC-SHA256 签名
        String signature = hmacSha256(MQ_SECRET, payload);
        String signedMsg = payload + "|" + signature;

        try {
            // 尝试获取一个连接
            Connection connection = factory.newConnection();
            // 尝试创建一个channel
            Channel channel = connection.createChannel();
            // 声明交换机（参数为：交换机名称; 交换机类型，广播模式）
            channel.exchangeDeclare("change", "fanout");
            // 消息发布（参数为：交换机名称; routingKey，忽略。在广播模式中，生产者声明交换机的名称和类型即可）
            channel.basicPublish("change", "", null, signedMsg.getBytes(StandardCharsets.UTF_8));
            channel.close();
            connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }
}
