"""Prophet 预测定时任务 —— 每日为每个设备生成未来 24h 预测"""

import logging
from datetime import datetime, timedelta

import pandas as pd
from prophet import Prophet
from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from database import SessionLocal
from models import Flowdata, Forecast, Monitored

logger = logging.getLogger(__name__)


async def run_forecast():
    """对所有设备执行一次预测"""
    async with SessionLocal() as db:
        devices = (await db.execute(select(Monitored.mac))).scalars().all()

    for mac in devices:
        try:
            await forecast_device(mac)
        except Exception as e:
            logger.error(f"Forecast failed for {mac}: {e}")


async def forecast_device(mac: str):
    """单个设备的 Prophet 预测"""
    async with SessionLocal() as db:
        # 取最近 30 天数据
        result = await db.execute(
            select(Flowdata.time, Flowdata.avg)
            .where(Flowdata.mac == mac, Flowdata.time >= datetime.now() - timedelta(days=30))
            .order_by(Flowdata.time)
        )
        rows = result.all()

    if len(rows) < 20:
        logger.info(f"Not enough data for {mac} ({len(rows)} rows), skip")
        return

    df = pd.DataFrame(rows, columns=["ds", "y"])
    df["ds"] = pd.to_datetime(df["ds"])

    # IQR 异常值剔除
    q1, q3 = df["y"].quantile(0.25), df["y"].quantile(0.75)
    iqr = q3 - q1
    df = df[(df["y"] >= q1 - 1.5 * iqr) & (df["y"] <= q3 + 1.5 * iqr)]

    if len(df) < 10:
        return

    # Prophet 拟合
    model = Prophet(
        seasonality_mode="multiplicative",
        daily_seasonality=True,
        weekly_seasonality=True,
    )
    model.fit(df)

    # 预测未来 24h（每 10 分钟一个点）
    future = model.make_future_dataframe(periods=144, freq="10min")
    prediction = model.predict(future)

    # 只取未来部分
    now = datetime.now()
    forecast_df = prediction[prediction["ds"] > now][["ds", "yhat"]].head(144)

    # 写入数据库
    async with SessionLocal() as db:
        await db.execute(delete(Forecast).where(Forecast.mac == mac))
        for _, row in forecast_df.iterrows():
            db.add(Forecast(mac=mac, time=row["ds"], avg=max(0, row["yhat"])))
        await db.commit()

    logger.info(f"Forecast done for {mac}: {len(forecast_df)} points")
