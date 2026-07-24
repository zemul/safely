# 云端部署指南

## 前置要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 1.8+ | 编译和运行 |
| Maven | 3.6+ | 构建 |
| Tomcat | 8.5+ | WAR 部署 |
| MySQL | 5.7+ | 业务数据库 |
| Redis | 5.0+ | 实时数据缓存 |
| RabbitMQ | 3.7+ | 消息队列 |
| MinIO / 对象存储 | 最新 | 视频存储（S3 协议，也可用阿里云 OSS） |
| Python | 3.8+ | Prophet 预测模块 |

## 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE anbao CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构（如有 SQL 文件）
# mysql -u root -p anbao < schema.sql

# 扩展密码列以支持 BCrypt
mysql -u root -p anbao < cloud/anbao/migration/001_password_bcrypt.sql
```

创建应用专用数据库账号（禁止使用 root）：

```sql
CREATE USER 'anbao_app'@'localhost' IDENTIFIED BY '强密码';
GRANT SELECT, INSERT, UPDATE, DELETE ON anbao.* TO 'anbao_app'@'localhost';
FLUSH PRIVILEGES;
```

## 2. Redis 配置

```bash
# 设置 Redis 密码
redis-cli CONFIG SET requirepass "你的Redis密码"
redis-cli CONFIG REWRITE
```

## 3. RabbitMQ 配置

```bash
# 创建专用用户（替换默认 guest）
rabbitmqctl add_user anbao_mq '强密码'
rabbitmqctl set_permissions -p / anbao_mq ".*" ".*" ".*"
rabbitmqctl delete_user guest
```

## 4. 环境变量配置

```bash
cp .env.example .env
# 编辑 .env 填入实际值
```

关键变量（完整清单见 [env-variables.md](env-variables.md)）：

```bash
export ANBAO_DB_URL="jdbc:mysql://localhost:3306/anbao?characterEncoding=utf-8&useSSL=true"
export ANBAO_DB_USERNAME="anbao_app"
export ANBAO_DB_PASSWORD="你的数据库密码"
export RABBITMQ_HOST="localhost"
export RABBITMQ_USERNAME="anbao_mq"
export RABBITMQ_PASSWORD="你的MQ密码"
export REDIS_PASSWORD="你的Redis密码"
export DEVICE_API_KEY="$(openssl rand -hex 32)"
export MQ_SIGNING_SECRET="$(openssl rand -hex 32)"
export ALIYUN_ACCESS_KEY_ID="你的阿里云Key"
export ALIYUN_ACCESS_KEY_SECRET="你的阿里云Secret"
export CORS_ALLOWED_ORIGINS="https://你的前端域名"
```

## 5. 构建 anbao 模块

```bash
cd cloud/anbao
mvn clean package -DskipTests
# 产物: target/boss.war
```

## 6. 部署到 Tomcat

```bash
# anbao 已包含视频模块，只需部署一个 WAR
cp cloud/anbao/target/boss.war $TOMCAT_HOME/webapps/ROOT.war
```

Tomcat 启动前设置环境变量：

```bash
# 在 $TOMCAT_HOME/bin/setenv.sh 中添加
export ANBAO_DB_URL="..."
export ANBAO_DB_PASSWORD="..."
# ... 其他变量
```

## 8. 部署 Prophet 预测模块

预测任务已内置在 anbao 服务中（每天凌晨 2:00 自动执行），只需确保：

1. 服务器安装了 Python 3.8+
2. 安装预测依赖：

```bash
cd cloud/time_serie_ARIMA
pip install -r requirements.txt
```

3. 设置环境变量：

```bash
export PYTHON_CMD=python3
export FORECAST_SCRIPT_PATH=/opt/safely/cloud/time_serie_ARIMA/forecast_prophet.py
export ANBAO_DB_HOST=localhost
export ANBAO_DB_USERNAME=anbao_app
export ANBAO_DB_PASSWORD=你的数据库密码
```

Tomcat 启动后会自动按 cron 调度预测脚本，无需额外配置 crontab。

## 9. 密码迁移（首次部署）

如果是从旧版本升级，需要将明文密码迁移为 BCrypt：

```bash
cd cloud/anbao
mvn dependency:copy-dependencies
java -cp "target/classes:target/dependency/*" com.anbao.utils.PasswordMigration
```

## 10. 验证部署

```bash
# 检查业务后台
curl -s http://localhost:8080/user/login -X POST -d "tel=admin&password=test"
# 应返回 JSON

# 检查设备端接口认证
curl -s http://localhost:8080/normalup -X POST
# 应返回 {"status":403,"msg":"invalid api key"}

curl -s http://localhost:8080/normalup -X POST -H "X-API-KEY: 你的key"
# 应正常响应
```

## 生产环境建议

- [ ] 配置 Nginx 反向代理，启用 HTTPS
- [ ] 设置 Tomcat `maxThreads`、`acceptCount` 限制
- [ ] MySQL 开启 binlog，定期备份
- [ ] Redis 开启持久化 (AOF)
- [ ] 监控 RabbitMQ 队列深度
- [ ] 日志收集（ELK 或类似方案）
