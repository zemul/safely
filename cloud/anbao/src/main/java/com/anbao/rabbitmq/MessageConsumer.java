package com.anbao.rabbitmq;

import com.anbao.controllor.WebSocketControllor;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MessageConsumer implements ChannelAwareMessageListener {

    private static final String MQ_SECRET;
    static {
        String secret = System.getenv("MQ_SIGNING_SECRET");
        if (secret == null || secret.trim().isEmpty()) {
            secret = System.getenv("DEVICE_API_KEY");
        }
        MQ_SECRET = (secret != null && !secret.trim().isEmpty()) ? secret : "dev-unsafe-default-key";
    }

    // 异常人流量
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        String info = new String(message.getBody(), StandardCharsets.UTF_8);

        String payload;
        // 验证 HMAC 签名
        if (info.contains("|")) {
            int lastPipe = info.lastIndexOf('|');
            payload = info.substring(0, lastPipe);
            String signature = info.substring(lastPipe + 1);

            String expected = hmacSha256(MQ_SECRET, payload);
            if (!MessageDigest.isEqual(expected.getBytes(), signature.getBytes())) {
                System.err.println("[SECURITY] Invalid MQ message signature, dropping: " + info);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
        } else {
            // 向后兼容：无签名消息（过渡期）
            payload = info;
        }

        System.out.println(payload);
        String[] s = payload.split(" ");
        String mac = s[0];
        String peopleNum = s[1];
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        // 获取 session
        WebSocketControllor websocket = WebSocketControllor.clients.get(mac);
        if (websocket != null && websocket.session != null && websocket.session.isOpen()) {
            websocket.session.getBasicRemote().sendText(peopleNum);
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
