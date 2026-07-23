# 边缘端部署指南

## 前置要求

| 组件 | 版本 | 说明 |
|------|------|------|
| Python | 3.8+ | 运行环境 |
| pip | 20+ | 包管理 |
| MySQL | 5.7+ | 本地数据缓存 |
| Redis | 5.0+ | 实时状态存储 |
| 摄像头 | USB / RTSP | 视频源 |
| GPU（推荐） | NVIDIA + CUDA 11 | 加速推理 |

## 硬件建议

- CPU: 4核以上
- 内存: 8GB+
- GPU: NVIDIA GTX 1060 或更高（密集场景检测需要）
- 存储: 50GB+（临时视频缓存）

## 1. 环境准备

```bash
# 克隆代码
cd edge/traffic

# 创建虚拟环境（推荐）
python3 -m venv venv
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

### GPU 支持（可选）

如果使用 GPU 加速：

```bash
# 根据 CUDA 版本安装对应的 PyTorch
pip install torch==2.1.2+cu118 torchvision==0.16.2+cu118 -f https://download.pytorch.org/whl/torch_stable.html
```

## 2. 模型文件准备

### 稀疏场景（Faster-RCNN）

```bash
# 下载预训练模型
# 百度网盘: https://pan.baidu.com/s/1K6HLh2QhJrCaYCf7M8IORA 提取码: oxk3

# 放置模型文件
cp detect sparse/checkpoints/
cp vgg16_caffe.pth sparse/data/pretrained_model/
```

修改模型路径：

```bash
# 编辑 sparse/src/config.py
# caffe_pretrain_path = "sparse/data/pretrained_model/vgg16_caffe.pth"
# model_save_path = "sparse/checkpoints/"
```

### 密集场景（CSRNet）

模型文件应放在 `density/` 目录下（参照 density/README 说明）。

## 3. 数据库初始化

```bash
mysql -u root -p -e "CREATE DATABASE py3 CHARACTER SET utf8mb4;"

# 创建应用账号
mysql -u root -p -e "
  CREATE USER 'edge_app'@'localhost' IDENTIFIED BY '强密码';
  GRANT ALL ON py3.* TO 'edge_app'@'localhost';
  FLUSH PRIVILEGES;
"
```

## 4. 环境变量配置

```bash
# 云端服务器地址（HTTPS）
export BASE_SERVER="https://你的云端地址:8080/"

# 数据库
export MYSQL_HOST="localhost"
export MYSQL_PORT="3306"
export MYSQL_DB="py3"
export MYSQL_USERNAME="edge_app"
export MYSQL_PASSWORD="你的数据库密码"

# Redis
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
export REDIS_PASSWORD="你的Redis密码"

# RabbitMQ（连接云端）
export MQ_HOST="云端MQ地址"
export MQ_PORT="5672"
export MQ_USERNAME="anbao_mq"
export MQ_PASSWORD="你的MQ密码"

# API Key（与云端一致）
export DEVICE_API_KEY="与云端相同的API Key"

# MQ 消息签名密钥（与云端一致）
export MQ_SIGNING_SECRET="与云端相同的签名密钥"
```

## 5. 启动服务

```bash
cd edge/traffic

# 主检测服务
python server.py
```

服务启动后会：
1. 连接 RabbitMQ 监听云端指令
2. 初始化摄像头视频采集
3. 定时上报统计数据到云端
4. 异常时触发预警

## 6. 验证部署

```bash
# 检查云端连接
curl -H "X-API-KEY: $DEVICE_API_KEY" "$BASE_SERVER/upState?id=test&flag=1"

# 检查 Redis
redis-cli -a "$REDIS_PASSWORD" ping
# 应返回 PONG

# 检查 MySQL
mysql -u edge_app -p$MYSQL_PASSWORD py3 -e "SELECT 1;"
```

## 7. 设备注册

边缘端设备启动后需要在云端管理界面注册：

1. 登录云端管理界面
2. 进入「设备管理」
3. 添加设备，填入 MAC 地址和节点名称
4. 设置预警阈值

## 开机自启（systemd）

```ini
# /etc/systemd/system/safely-edge.service
[Unit]
Description=Safely Edge Traffic Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=safely
WorkingDirectory=/opt/safely/edge/traffic
EnvironmentFile=/opt/safely/.env
ExecStart=/opt/safely/edge/traffic/venv/bin/python server.py
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable safely-edge
sudo systemctl start safely-edge
sudo journalctl -u safely-edge -f  # 查看日志
```

## 故障排查

| 问题 | 可能原因 | 解决方案 |
|------|---------|---------|
| 无法连接云端 | 网络/防火墙/API Key 错误 | 检查 `BASE_SERVER` 和 `DEVICE_API_KEY` |
| MQ 连接失败 | 凭据错误或端口不通 | 检查 MQ 环境变量，telnet 测试端口 |
| GPU 未使用 | CUDA 未安装或版本不匹配 | `nvidia-smi` 检查，重装对应版本 torch |
| 检测不准确 | 模型文件缺失或路径错误 | 检查 sparse/checkpoints/ 和 density/ |
