# 环境变量配置清单

所有敏感配置通过环境变量注入，禁止硬编码在源码中。

## 云端后台 (anbao / videoIO)

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `ANBAO_DB_URL` | 否 | `jdbc:mysql://localhost:3306/anbao?characterEncoding=utf-8` | 数据库 JDBC URL |
| `ANBAO_DB_USERNAME` | 否 | `root` | 数据库用户名 |
| `ANBAO_DB_PASSWORD` | **是** | (空) | 数据库密码 |
| `RABBITMQ_HOST` | 否 | `localhost` | RabbitMQ 地址 |
| `RABBITMQ_PORT` | 否 | `5672` | RabbitMQ 端口 |
| `RABBITMQ_USERNAME` | 否 | `guest` | RabbitMQ 用户名 |
| `RABBITMQ_PASSWORD` | **是** | (空) | RabbitMQ 密码 |
| `RABBITMQ_VHOST` | 否 | `/` | RabbitMQ 虚拟主机 |
| `REDIS_HOST` | 否 | `127.0.0.1` | Redis 地址 |
| `REDIS_PORT` | 否 | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | **是** | (空) | Redis 密码 |
| `ALIYUN_ACCESS_KEY_ID` | **是** | — | 阿里云短信 AccessKey ID |
| `ALIYUN_ACCESS_KEY_SECRET` | **是** | — | 阿里云短信 AccessKey Secret |
| `DEVICE_API_KEY` | **是** | `dev-unsafe-default-key` | 设备端 API 认证密钥 |
| `MQ_SIGNING_SECRET` | 否 | (同 DEVICE_API_KEY) | MQ 消息 HMAC 签名密钥 |
| `CORS_ALLOWED_ORIGINS` | 否 | `http://localhost:8080,http://localhost:3000` | CORS 允许的前端域名 |

## 边缘端 (edge/traffic)

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `BASE_SERVER` | 否 | `http://localhost:8080/` | 云端服务器地址 |
| `MYSQL_HOST` | 否 | `localhost` | 本地 MySQL 地址 |
| `MYSQL_PORT` | 否 | `3306` | MySQL 端口 |
| `MYSQL_DB` | 否 | `py3` | 数据库名 |
| `MYSQL_USERNAME` | 否 | `root` | MySQL 用户名 |
| `MYSQL_PASSWORD` | **是** | (空) | MySQL 密码 |
| `REDIS_HOST` | 否 | `localhost` | Redis 地址 |
| `REDIS_PORT` | 否 | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 否 | (空) | Redis 密码 |
| `MQ_HOST` | 否 | `localhost` | RabbitMQ 地址 |
| `MQ_PORT` | 否 | `5672` | RabbitMQ 端口 |
| `MQ_USERNAME` | 否 | `guest` | RabbitMQ 用户名 |
| `MQ_PASSWORD` | **是** | (空) | RabbitMQ 密码 |
| `DEVICE_API_KEY` | **是** | `dev-unsafe-default-key` | 与云端一致的 API Key |
| `MQ_SIGNING_SECRET` | 否 | (同 DEVICE_API_KEY) | MQ 消息签名密钥 |

## Prophet 预测模块

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `ANBAO_DB_HOST` | 否 | `localhost` | 数据库地址 |
| `ANBAO_DB_NAME` | 否 | `anbao` | 数据库名 |
| `ANBAO_DB_USERNAME` | 否 | `root` | 数据库用户名 |
| `ANBAO_DB_PASSWORD` | **是** | (空) | 数据库密码 |

## 密钥生成建议

```bash
# 生成随机 API Key (64 字符十六进制)
openssl rand -hex 32

# 生成随机密码 (20 字符)
openssl rand -base64 15
```

## 注意事项

1. **生产环境** 中所有标记 "必填" 的变量都必须显式设置
2. `DEVICE_API_KEY` 必须在云端和所有边缘端保持一致
3. `MQ_SIGNING_SECRET` 同上，建议与 `DEVICE_API_KEY` 使用不同的值
4. 禁止将 `.env` 文件提交到版本控制（已在 `.gitignore` 中排除）
5. 阿里云 AccessKey 建议使用 RAM 子账号，仅授予短信发送权限
