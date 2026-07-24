"""区域管理 API"""

from fastapi import APIRouter, Depends
from pydantic import BaseModel
from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from auth import get_current_user
from database import get_db
from models import Area, Monitored, User

router = APIRouter(prefix="/api/area", tags=["area"])


class AreaCreate(BaseModel):
    addr: str


class AreaUpdate(BaseModel):
    aid: int
    addr: str


@router.get("/getAreaName")
async def get_area_name(db: AsyncSession = Depends(get_db)):
    """所有区域名称（下拉框）"""
    result = await db.execute(select(Area))
    areas = result.scalars().all()
    return [{"aid": a.aid, "addr": a.addr} for a in areas]


@router.post("/getAreaDevice")
async def get_area_device(aid: int, db: AsyncSession = Depends(get_db)):
    """某区域下的设备列表"""
    result = await db.execute(select(Monitored).where(Monitored.aid == aid))
    devices = result.scalars().all()
    return [{"mac": d.mac, "node": d.node, "state": d.state} for d in devices]


@router.post("/getAreaList")
async def get_area_list(
    addr: str | None = None,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """区域列表（含关联管理员）"""
    query = select(Area, User).outerjoin(User, Area.aid == User.aid).where(User.state == 2)
    if addr:
        query = query.where(Area.addr.contains(addr) | User.name.contains(addr))
    result = await db.execute(query)
    rows = result.all()
    return {
        "status": 200,
        "data": [
            {"aid": a.aid, "addr": a.addr, "name": u.name if u else None, "tel": u.tel if u else None}
            for a, u in rows
        ],
    }


@router.post("/addArea")
async def add_area(
    req: AreaCreate,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    area = Area(addr=req.addr)
    db.add(area)
    await db.commit()
    return {"status": 200, "msg": str(area.aid)}


@router.post("/updateArea")
async def update_area(
    req: AreaUpdate,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    area = await db.get(Area, req.aid)
    if area:
        area.addr = req.addr
        await db.commit()
    return {"status": 200}


@router.post("/deleteArea")
async def delete_area(
    aid: int,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    await db.execute(delete(Area).where(Area.aid == aid))
    await db.commit()
    return {"status": 200}
