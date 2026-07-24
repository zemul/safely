# 人流量时序预测

基于历史监控数据预测未来 24 小时各设备的人流量趋势。

## 算法演进

| 版本 | 文件 | 算法 | 状态 |
|------|------|------|------|
| v1 | `decomp_modelsql.py` | ARIMA(1,1,3) + seasonal_decompose | 旧版（仅日周期） |
| v2 | `forecast_prophet.py` | Prophet | **当前推荐** |

## Prophet 方案优势

相比原 ARIMA：

- **自动识别周周期** — 区分工作日和周末的人流量模式
- **节假日效应** — 内置中国节假日（春节/国庆/五一等），预测更准
- **乘法模型** — 高峰时段波动更大，乘法分解更符合实际
- **鲁棒性** — 自动处理缺失值和异常值，无需手动调参
- **可解释** — 输出趋势、日周期、周周期、节假日各分量

## 预测流程

```
flowdata 表 (每10分钟一条)
        │
        ▼
异常值剔除 (IQR 方法) + 线性插值
        │
        ▼
Prophet 模型拟合:
  · 趋势 (changepoint 自动检测)
  · 日周期 (早高峰/午休/晚高峰)
  · 周周期 (工作日 vs 周末)
  · 节假日效应
        │
        ▼
预测未来 144 个点 (24小时)
        │
        ▼
写入 forecast 表
```

## 使用方式

```bash
# 安装依赖
pip install -r requirements.txt

# 设置环境变量
export ANBAO_DB_HOST=localhost
export ANBAO_DB_USERNAME=anbao_app
export ANBAO_DB_PASSWORD=your_password

# 运行预测
python forecast_prophet.py
```

建议通过 crontab 每日定时执行：

```cron
0 2 * * * cd /path/to/time_serie_ARIMA && python forecast_prophet.py >> /var/log/forecast.log 2>&1
```

## 数据要求

- 每个设备至少 2 天（288 条）历史数据才会进行预测
- 数据越多预测越准（建议 30 天以上）
- 数据来源：`flowdata` 表的 `avg` 字段（每 10 分钟边缘端上报的均值）
