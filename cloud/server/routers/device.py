"""设备管理 API + 边缘端数据接收"""

import uuid
from datetime import datetime

from fastapi import APIRouter, Depends, Header, HTTPException, UploadFile, File
from pydantic import BaseModel
from sqlalchemy import delete, func, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from auth import get_current_user
from config import settings
from database import get_db
from models import Exception_, Flowdata, Monitored, User, UserMac

router = APIRouter(prefix="/api/device", tags=["device"])


class FlowReport(BaseModel):
    """边缘端定时上报的统计数据"""
    mac: str
    avg: float
    max: int
    center: float = 0.0
    variance: float = 0.0


class ExceptionReport(BaseModel):
    """边缘端超阈值事件上报"""
    mac: str
    continuetime: str


class DeviceCreate(BaseModel):
    mac: str
    aid: int
    node: str
    threshold: int = 30


class DeviceUpdate(BaseModel):
    mac: str
    node: str | None = None
    threshold: int | None = None
    aid: int | None = None


# ---------- 边缘端接口（API Key 认证） ----------

def verify_device_key(x_api_key: str = Header(...)):
    if x_api_key != settings.device_api_key:
        raise HTTPException(status_code=403, detail="无效的设备 API Key")


@router.post("/report", dependencies=[Depends(verify_device_key)])
async def report_flow(req: FlowReport, db: AsyncSession = Depends(get_db)):
    """边缘端定时上报人流量统计"""
    flow = Flowdata(mac=req.mac, time=datetime.now(), avg=req.avg, max=req.max,
                    center=req.center, variance=req.variance)
    db.add(flow)
    # 更新设备最新人数和时间
    await db.execute(
        update(Monitored)
        .where(Monitored.mac == req.mac)
        .values(num=req.max, time=datetime.now(), state="online")
    )
    await db.commit()
    return {"status": 200}


@router.post("/exception", dependencies=[Depends(verify_device_key)])
async def report_exception(req: ExceptionReport, db: AsyncSession = Depends(get_db)):
    """边缘端超阈值异常上报"""
    exc = Exception_(
        eid=str(uuid.uuid4())[:8],
        mac=req.mac,
        inittime=datetime.now(),
        continuetime=req.continuetime,
    )
    db.add(exc)
    await db.commit()
    return {"status": 200, "eid": exc.eid}


@router.post("/uploadVideo", dependencies=[Depends(verify_device_key)])
async def upload_video(
    mac: str, eid: str, file: UploadFile = File(...),
    db: AsyncSession = Depends(get_db),
):
    """边缘端上传异常视频到 S3"""
    import boto3
    s3 = boto3.client(
        "s3", endpoint_url=settings.s3_endpoint,
        aws_access_key_id=settings.s3_access_key,
        aws_secret_access_key=settings.s3_secret_key,
    )
    key = f"{mac}/{eid}/{file.filename}"
    s3.upload_fileobj(file.file, settings.s3_bucket, key)
    video_url = f"{settings.s3_endpoint}/{settings.s3_bucket}/{key}"

    await db.execute(
        update(Exception_).where(Exception_.eid == eid).values(videourl=video_url)
    )
    await db.commit()
    return {"status": 200, "url": video_url}


@router.get("/upState", dependencies=[Depends(verify_device_key)])
async def up_state(mac: str, state: str = "online", db: AsyncSession = Depends(get_db)):
    """边缘端心跳/状态更新"""
    await db.execute(
        update(Monitored).where(Monitored.mac == mac).values(state=state, time=datetime.now())
    )
    await db.commit()
    return {"status": 200}


# ---------- 管理端接口 ----------

@router.post("/Device")
async def get_devices(
    aid: int | None = None,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """设备列表（按区域筛选）"""
    query = select(Monitored)
    if aid:
        query = query.where(Monitored.aid == aid)
    result = await db.execute(query)
    devices = result.scalars().all()
    return {
        "status": 200,
        "data": [
            {
                "mac": d.mac, "aid": d.aid, "node": d.node,
                "state": d.state, "threshold": d.threshold,
                "num": d.num, "time": str(d.time) if d.time else None,
            }
            for d in devices
        ],
    }


@router.post("/addDevice")
async def add_device(
    req: DeviceCreate,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    device = Monitored(mac=req.mac, aid=req.aid, node=req.node, threshold=req.threshold)
    db.add(device)
    await db.commit()
    return {"status": 200}


@router.post("/updateDevice")
async def update_device(
    req: DeviceUpdate,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    device = await db.get(Monitored, req.mac)
    if not device:
        return {"status": 404, "msg": "设备不存在"}
    if req.node is not None:
        device.node = req.node
    if req.threshold is not None:
        device.threshold = req.threshold
    if req.aid is not None:
        device.aid = req.aid
    await db.commit()
    return {"status": 200}


@router.post("/deleteDevice")
async def delete_device(
    mac: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    await db.execute(delete(Monitored).where(Monitored.mac == mac))
    await db.commit()
    return {"status": 200}


@router.get("/indexInfo")
async def index_info(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """首页看板数据"""
    aid = user.aid
    # 设备总数
    device_count = await db.scalar(
        select(func.count()).select_from(Monitored).where(Monitored.aid == aid)
    )
    # 在线设备数
    online_count = await db.scalar(
        select(func.count()).select_from(Monitored)
        .where(Monitored.aid == aid, Monitored.state == "online")
    )
    # 30天异常次数
    from models import Exception_
    exception_count = await db.scalar(
        select(func.count()).select_from(Exception_)
        .join(Monitored, Exception_.mac == Monitored.mac)
        .where(Monitored.aid == aid)
    )
    return {
        "status": 200,
        "data": {
            "deviceCount": device_count or 0,
            "onlineCount": online_count or 0,
            "exceptionCount": exception_count or 0,
        },
    }


@router.get("/getSelectDevice")
async def get_select_device(aid: int | None = None, db: AsyncSession = Depends(get_db)):
    """设备下拉选择"""
    query = select(Monitored)
    if aid:
        query = query.where(Monitored.aid == aid)
    result = await db.execute(query)
    devices = result.scalars().all()
    return [{"name": d.node, "value": d.mac} for d in devices]
