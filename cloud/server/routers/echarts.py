"""图表数据 API (ECharts 前端用)"""

from fastapi import APIRouter, Depends
from sqlalchemy import func, select, text
from sqlalchemy.ext.asyncio import AsyncSession

from auth import get_current_user
from database import get_db
from models import Exception_, Flowdata, Forecast, Monitored, User

router = APIRouter(prefix="/api/echarts", tags=["echarts"])


@router.post("/history")
async def get_history(
    mac: str,
    num: str = "24h",
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """历史人流量曲线"""
    interval_map = {"6h": "6 HOUR", "24h": "24 HOUR", "7d": "7 DAY", "30d": "30 DAY"}
    interval = interval_map.get(num, "24 HOUR")

    result = await db.execute(
        select(Flowdata)
        .where(
            Flowdata.mac == mac,
            Flowdata.time >= text(f"NOW() - INTERVAL {interval}"),
        )
        .order_by(Flowdata.time)
    )
    rows = result.scalars().all()
    return {
        "status": 200,
        "data": [{"time": str(r.time), "avg": r.avg, "max": r.max} for r in rows],
    }


@router.post("/normal")
async def get_forecast(
    mac: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Prophet 预测数据（未来24h）"""
    result = await db.execute(
        select(Forecast)
        .where(Forecast.mac == mac, Forecast.time >= text("NOW()"))
        .order_by(Forecast.time)
    )
    rows = result.scalars().all()
    return {
        "status": 200,
        "data": [{"time": str(r.time), "avg": r.avg} for r in rows],
    }


@router.post("/BetweenDayHistory")
async def get_between_day_history(
    mac: str,
    day1: str,
    day2: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """日期范围查询"""
    if day1 == day2:
        result = await db.execute(
            select(Flowdata).where(
                Flowdata.mac == mac,
                func.date(Flowdata.time) == day1,
            ).order_by(Flowdata.time)
        )
    else:
        result = await db.execute(
            select(Flowdata).where(
                Flowdata.mac == mac,
                Flowdata.time.between(day1, day2),
            ).order_by(Flowdata.time)
        )
    rows = result.scalars().all()
    return {
        "status": 200,
        "data": [{"time": str(r.time), "avg": r.avg} for r in rows],
    }


@router.post("/getAreaBarchart")
async def get_area_barchart(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """各区域异常数量柱状图"""
    result = await db.execute(text("""
        SELECT a.addr, COALESCE(COUNT(e.eid), 0) as sum
        FROM area a
        LEFT JOIN monitored m ON a.aid = m.aid
        LEFT JOIN exception e ON m.mac = e.mac
            AND e.inittime >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        GROUP BY a.aid
        ORDER BY sum ASC
    """))
    rows = result.all()
    return {"status": 200, "data": [{"name": r[0], "sum": r[1]} for r in rows]}


@router.post("/getDeviceBarchart")
async def get_device_barchart(
    aid: int,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """某区域各设备异常数量柱状图"""
    result = await db.execute(text("""
        SELECT m.node, COALESCE(COUNT(e.eid), 0) as sum
        FROM monitored m
        LEFT JOIN exception e ON m.mac = e.mac
            AND e.inittime >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        WHERE m.aid = :aid
        GROUP BY m.mac
        ORDER BY sum ASC
    """), {"aid": aid})
    rows = result.all()
    return {"status": 200, "data": [{"name": r[0], "sum": r[1]} for r in rows]}
