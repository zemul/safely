# 设备通信协议

边缘端与云端之间通过 HTTP API、RabbitMQ 和 WebSocket 三种方式通信。

## 通信概览

```
┌─────────────┐                              ┌─────────────┐
│   边缘端     │                              │    云端      │
│  (traffic)  │                              │  (anbao)    │
│             │──── HTTP + X-API-KEY ────────▶│             │
│             │    (统计数据/状态/视频/图片)     │             │
│             │                              │             │
│             │◀─── RabbitMQ (fanout) ────────│             │
│             │    (修改阈值/请求截图)          │             │
│             │                              │             │
│             │──── RabbitMQ (direct) ────────▶│             │
│             │    (实时人数推送)               │             │
│             │                              │             │
│             │                              │  ┌────────┐ │
│             │                              │  │WebSocket│◀── 浏览器
│             │                              │  └────────┘ │
└─────────────┘                              └─────────────┘
```

## HTTP 通信

详见 [cloud-api.md](cloud-api.md) 中的「设备通信端点」部分。

认证方式：`X-API-KEY` 请求头。

## RabbitMQ 消息协议

### 连接参数

| 参数 | 云端配置来源 | 边缘端配置来源 |
|------|------------|--------------|
| host | `${RABBITMQ_HOST}` | `MQ_HOST` 环境变量 |
| port | `${RABBITMQ_PORT}` (5672) | `MQ_PORT` 环境变量 |
| username | `${RABBITMQ_USERNAME}` | `MQ_USERNAME` 环境变量 |
| password | `${RABBITMQ_PASSWORD}` | `MQ_PASSWORD` 环境变量 |
| vhost | `/` | `/` |

### Exchange 和 Queue 定义

| Exchange | 类型 | Queue | 方向 | 用途 |
|----------|------|-------|------|------|
| `order` | direct | `queue` | 边缘→云 | 实时人数推送 |
| `exchange` | direct | `queue2` | 内部 | 预留 |
| `change` | fanout | (临时) | 云→边缘 | 下发指令 |
| `message` | — | — | 云内部 | WebSocket 推送 |

### 消息格式

所有消息使用 HMAC-SHA256 签名，格式为：

```
payload|hmac_sha256_hex_signature
```

签名密钥：`MQ_SIGNING_SECRET` 环境变量。

---

### 边缘→云：实时人数推送

**Exchange**: `order`  
**Routing Key**: `queue`  
**场景**: 设备检测到人数超过阈值时，持续推送实时数据

```
格式: {deviceMAC} {人数}|{signature}
示例: AA:BB:CC:DD:EE:FF 45|a3f2b1...
```

云端 `MessageConsumer` 接收后：
1. 验证 HMAC 签名
2. 解析 MAC 和人数
3. 通过 WebSocket 推送到前端仪表盘
4. 存入 Redis 缓存

---

### 云→边缘：修改阈值

**Exchange**: `change` (fanout)  
**场景**: 管理员在 Web 界面修改设备阈值

```
格式: 0 {deviceMAC} {新阈值}|{signature}
示例: 0 AA:BB:CC:DD:EE:FF 50|b4e5c2...
```

边缘端 `callback` 接收后：
1. 验证签名
2. 解析指令类型 `0` = 修改阈值
3. 更新本地 Redis 中对应设备的阈值

---

### 云→边缘：请求实时截图

**Exchange**: `change` (fanout)  
**场景**: 管理员点击「请求截图」按钮

```
格式: 1 {deviceMAC}|{signature}
示例: 1 AA:BB:CC:DD:EE:FF|c7d8e9...
```

边缘端接收后：
1. 验证签名
2. 解析指令类型 `1` = 请求截图
3. 从摄像头截取当前帧
4. 通过 HTTP POST `/device/getImage` 上传

---

## WebSocket 协议

### 连接

```
ws://{host}:8080/webSocket/{deviceMAC}?token={DEVICE_API_KEY}
```

- **认证**：`token` 查询参数必须等于 `DEVICE_API_KEY`
- **无效连接**：服务端立即关闭，CloseCode = 1008 (VIOLATED_POLICY)

### 消息流向

```
云端 MessageConsumer 收到 MQ 实时人数
        │
        ▼
WebSocketControllor.singleSend(人数, mac)
        │
        ▼
浏览器 websocket.onmessage = function(event) {
    // event.data = "45" (当前人数)
}
```

### 前端连接示例

```javascript
var mac = "AA:BB:CC:DD:EE:FF";
var token = "your-device-api-key"; // 前端需要获取此 token
var ws = new WebSocket("ws://host:8080/webSocket/" + mac + "?token=" + token);

ws.onopen = function() {
    console.log("连接成功");
};

ws.onmessage = function(event) {
    // event.data: "0" = 连接确认, 其他数字 = 实时人数
    console.log("当前人数: " + event.data);
};

ws.onclose = function(event) {
    if (event.code === 1008) {
        console.error("认证失败");
    }
};
```

## 安全要求

1. **传输加密**：生产环境应使用 TLS（HTTPS / WSS / AMQPS）
2. **API Key 轮换**：定期更换 `DEVICE_API_KEY`，更新所有边缘设备
3. **签名密钥**：`MQ_SIGNING_SECRET` 应独立于 `DEVICE_API_KEY`
4. **消息验证**：拒绝签名无效的 MQ 消息
5. **连接限制**：同一 MAC 仅允许一个 WebSocket 连接
