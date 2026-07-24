#-*- coding:utf-8 -*-
"""
人流量时序预测 — 基于 Prophet

优势（相比原 ARIMA）：
- 自动处理日周期 + 周周期
- 内置节假日效应（中国节假日）
- 对缺失值和异常值更鲁棒
- 无需手动选参

使用方式：
  设置环境变量 ANBAO_DB_HOST, ANBAO_DB_USERNAME, ANBAO_DB_PASSWORD
  python forecast_prophet.py
"""

import os
import warnings
import numpy as np
import pandas as pd
import pymysql
from prophet import Prophet

warnings.filterwarnings('ignore')

# 数据库连接
db = pymysql.connect(
    host=os.environ.get("ANBAO_DB_HOST", "localhost"),
    user=os.environ.get("ANBAO_DB_USERNAME", "root"),
    password=os.environ.get("ANBAO_DB_PASSWORD", ""),
    database=os.environ.get("ANBAO_DB_NAME", "anbao")
)

# 中国主要节假日（可按需扩展）
CN_HOLIDAYS = pd.DataFrame({
    'holiday': 'cn_holiday',
    'ds': pd.to_datetime([
        # 元旦
        '2024-01-01', '2025-01-01', '2026-01-01',
        # 春节（除夕-初六）
        '2024-02-09', '2024-02-10', '2024-02-11', '2024-02-12',
        '2024-02-13', '2024-02-14', '2024-02-15',
        '2025-01-28', '2025-01-29', '2025-01-30', '2025-01-31',
        '2025-02-01', '2025-02-02', '2025-02-03',
        # 清明
        '2024-04-04', '2025-04-04', '2026-04-05',
        # 五一
        '2024-05-01', '2024-05-02', '2024-05-03',
        '2025-05-01', '2025-05-02', '2025-05-03',
        # 国庆
        '2024-10-01', '2024-10-02', '2024-10-03', '2024-10-04',
        '2024-10-05', '2024-10-06', '2024-10-07',
        '2025-10-01', '2025-10-02', '2025-10-03', '2025-10-04',
        '2025-10-05', '2025-10-06', '2025-10-07',
    ]),
    'lower_window': 0,
    'upper_window': 0,
})


def read_device_data(mac):
    """读取设备历史人流量数据"""
    sql = "SELECT time, avg FROM flowdata WHERE mac=%s AND time >= (NOW() - INTERVAL 100 DAY)"
    data = pd.read_sql_query(sql, db, params=[mac])
    if data.empty:
        return None

    # Prophet 要求列名为 ds 和 y
    data.columns = ['ds', 'y']
    data['ds'] = pd.to_datetime(data['ds'])

    # 去除异常值（IQR 方法）
    q1 = data['y'].quantile(0.25)
    q3 = data['y'].quantile(0.75)
    iqr = q3 - q1
    lower = q1 - 2 * iqr
    upper = q3 + 2 * iqr
    data.loc[(data['y'] < lower) | (data['y'] > upper), 'y'] = np.nan
    data['y'] = data['y'].interpolate(method='linear')

    return data


def forecast_device(mac, periods=144):
    """
    对单个设备进行预测

    Args:
        mac: 设备MAC地址
        periods: 预测未来多少个时间点（默认144 = 24小时, 每10分钟一个点）

    Returns:
        预测结果 DataFrame，或 None
    """
    data = read_device_data(mac)
    if data is None or len(data) < 288:  # 至少2天数据
        print(f"  跳过 {mac}: 数据不足 ({len(data) if data is not None else 0} 条)")
        return None

    # 构建 Prophet 模型
    model = Prophet(
        daily_seasonality=True,     # 日周期（早高峰/午休/晚高峰）
        weekly_seasonality=True,    # 周周期（工作日/周末差异）
        yearly_seasonality=False,   # 数据不够一年，关闭年周期
        holidays=CN_HOLIDAYS,       # 中国节假日效应
        changepoint_prior_scale=0.05,  # 趋势变化灵敏度（偏保守）
        seasonality_mode='multiplicative',  # 乘法模型：高峰时波动也大
    )

    model.fit(data)

    # 生成未来时间序列
    future = model.make_future_dataframe(periods=periods, freq='10min')
    forecast = model.predict(future)

    # 只取预测部分，且人数不能为负
    result = forecast[['ds', 'yhat']].tail(periods).copy()
    result['yhat'] = result['yhat'].clip(lower=0).round().astype(int)

    return result


def save_forecast(mac, result):
    """将预测结果写入数据库"""
    cursor = db.cursor()

    # 清除旧预测
    cursor.execute("DELETE FROM forecast WHERE mac = %s", (mac,))

    # 批量插入新预测
    records = [(row['ds'], int(row['yhat']), mac) for _, row in result.iterrows()]
    cursor.executemany(
        "INSERT INTO forecast (time, avg, mac) VALUES (%s, %s, %s)",
        records
    )
    db.commit()
    cursor.close()


def main():
    """主流程：遍历所有设备，逐个预测并存储"""
    print("=" * 50)
    print("人流量预测 (Prophet)")
    print("=" * 50)

    # 查询有足够数据的设备
    sql = """
        SELECT mac, COUNT(*) as cnt
        FROM flowdata
        WHERE DATE_SUB(CURDATE(), INTERVAL 100 DAY) <= DATE(time)
        GROUP BY mac
    """
    cursor = db.cursor()
    cursor.execute(sql)
    devices = cursor.fetchall()
    cursor.close()

    success = 0
    skipped = 0

    for mac, count in devices:
        if count < 288:  # 至少2天数据
            skipped += 1
            continue

        print(f"\n处理设备: {mac} ({count} 条历史数据)")
        try:
            result = forecast_device(mac, periods=144)
            if result is not None:
                save_forecast(mac, result)
                print(f"  预测完成: {len(result)} 个时间点")
                success += 1
            else:
                skipped += 1
        except Exception as e:
            print(f"  预测失败: {e}")
            skipped += 1

    print(f"\n{'=' * 50}")
    print(f"完成: {success} 个设备预测成功, {skipped} 个跳过")
    print("=" * 50)

    db.close()


if __name__ == '__main__':
    main()
