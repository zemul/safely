"""Safely 云端后台 —— FastAPI 主入口"""

import asyncio
import logging
from contextlib import asynccontextmanager
from pathlib import Path

from apscheduler.schedulers.asyncio import AsyncIOScheduler
from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles

from config import settings
from routers import area, device, echarts, user, ws
import mq
from forecast import run_forecast

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(name)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)

scheduler = AsyncIOScheduler()


async def on_mq_message(data: dict):
    """MQ 收到边缘端实时人数时，通过 WebSocket 推送给前端"""
    mac = data.get("mac")
    if mac:
        await ws.broadcast(mac, data)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动
    logger.info("Starting Safely server...")
    await mq.connect()
    asyncio.create_task(mq.start_consumer(on_mq_message))

    # 定时预测：每天凌晨 3 点
    scheduler.add_job(run_forecast, "cron", hour=3, minute=0)
    scheduler.start()
    logger.info("Scheduler started (forecast daily at 03:00)")

    yield

    # 关闭
    scheduler.shutdown()
    await mq.close()


app = FastAPI(title="Safely", version="2.0.0", lifespan=lifespan)

# API 路由
app.include_router(user.router)
app.include_router(area.router)
app.include_router(device.router)
app.include_router(echarts.router)
app.include_router(ws.router)

# 静态文件（前端 HTML）—— 放在最后，兜底所有非 /api /ws 路径
static_dir = Path(__file__).parent.parent / "html"
if static_dir.exists():
    app.mount("/", StaticFiles(directory=str(static_dir), html=True), name="static")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host=settings.host, port=settings.port, reload=False)
