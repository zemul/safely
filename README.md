# Safely — 公共地点人流量云监管平台

基于云边协同架构的实时人流量监控系统，支持视频流分析、人群密度估计、异常预警和流量预测。

## 系统组成

```
┌─────────────────────────────────────────────┐
│                   云端 (Cloud)               │
│  ┌──────────┐  ┌──────────┐  ┌───────────┐ │
│  │ anbao    │  │ videoIO  │  │  ARIMA    │ │
│  │ 业务后台  │  │ 视频传输  │  │ 流量预测  │ │
│  └──────────┘  └──────────┘  └───────────┘ │
│  ┌──────────┐                               │
│  │  html    │                               │
│  │ Web前端   │                               │
│  └──────────┘                               │
└──────────────────────┬──────────────────────┘
                       │ HTTP/RabbitMQ/WebSocket
┌──────────────────────┴──────────────────────┐
│                边缘端 (Edge)                  │
│  ┌──────────┐  ┌──────────┐                 │
│  │ traffic  │  │  flask   │                 │
│  │ 检测后台  │  │ 管理前台  │                 │
│  └──────────┘  └──────────┘                 │
└─────────────────────────────────────────────┘
```

| 模块 | 技术栈 | 说明 |
|------|--------|------|
| `cloud/anbao` | Spring MVC 4.3 + MyBatis + MySQL | 业务后台（用户、设备、区域、预警） |
| `cloud/videoIO` | Spring MVC + Hadoop HDFS + RabbitMQ | 视频流存储与转发 |
| `cloud/html` | Layui + jQuery | 管理端 Web 前台 |
| `cloud/time_serie_ARIMA` | Python + statsmodels | 时序预测（ARIMA） |
| `edge/traffic` | Python + OpenCV + PyTorch | 边缘端视频分析与人群检测 |

## 快速开始

```bash
# 1. 配置环境变量
cp .env.example .env
# 编辑 .env 填入实际凭据

# 2. 云端部署
cd cloud/anbao
mvn package -DskipTests
# 部署 WAR 到 Tomcat

# 3. 边缘端部署
cd edge/traffic
pip install -r requirements.txt
python server.py
```

详细部署指南见 [docs/deployment/](docs/deployment/)。

## 文档

| 文档 | 说明 |
|------|------|
| [系统架构](docs/architecture.md) | 整体架构、模块关系、数据流 |
| [云端部署](docs/deployment/cloud.md) | 云端环境搭建与部署步骤 |
| [边缘端部署](docs/deployment/edge.md) | 边缘设备部署与配置 |
| [环境变量](docs/deployment/env-variables.md) | 所有环境变量配置清单 |
| [安全机制](docs/security/overview.md) | 认证、加密、防护机制说明 |
| [云端 API](docs/api/cloud-api.md) | REST API 接口文档 |
| [设备协议](docs/api/device-protocol.md) | 设备通信协议（MQ / WebSocket） |

## 历史文档

比赛期间的原始设计文档归档在 [docs/legacy/](docs/legacy/)。

## License

本项目为中国软件杯参赛作品。
