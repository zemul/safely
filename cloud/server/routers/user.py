"""用户相关 API"""

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy import select, delete
from sqlalchemy.ext.asyncio import AsyncSession

from auth import (
    create_token, get_current_user, hash_password, verify_password,
)
from database import get_db
from models import Message, User

router = APIRouter(prefix="/api/user", tags=["user"])


class LoginReq(BaseModel):
    tel: str
    password: str


class LoginResp(BaseModel):
    status: int
    msg: str | None = None
    token: str | None = None


class RegisterReq(BaseModel):
    uid: str
    tel: str
    name: str
    password: str
    email: str | None = None
    aid: int | None = None


class UserUpdate(BaseModel):
    name: str | None = None
    tel: str | None = None
    email: str | None = None
    aid: int | None = None


@router.post("/login", response_model=LoginResp)
async def login(req: LoginReq, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.tel == req.tel))
    user = result.scalar_one_or_none()

    if not user:
        return LoginResp(status=201, msg="用户不存在")
    if not user.password:
        return LoginResp(status=203, msg="请联系管理员设置密码")
    if not verify_password(req.password, user.password):
        return LoginResp(status=201, msg="密码错误")

    token = create_token(user.uid, user.aid)
    return LoginResp(status=200, msg=str(user.aid), token=token)


@router.post("/register")
async def register(req: RegisterReq, db: AsyncSession = Depends(get_db)):
    user = User(
        uid=req.uid,
        tel=req.tel,
        name=req.name,
        password=hash_password(req.password),
        email=req.email,
        aid=req.aid,
        state=3,
    )
    db.add(user)
    await db.commit()
    return {"status": 200, "msg": "注册成功"}


@router.post("/resetpassword")
async def reset_password(
    old_password: str,
    new_password: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    if not verify_password(old_password, user.password):
        raise HTTPException(status_code=400, detail="原密码错误")
    user.password = hash_password(new_password)
    await db.commit()
    return {"status": 200, "msg": "修改成功"}


@router.post("/resetuserinfo")
async def reset_user_info(
    req: UserUpdate,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    if req.name is not None:
        user.name = req.name
    if req.tel is not None:
        user.tel = req.tel
    if req.email is not None:
        user.email = req.email
    if req.aid is not None:
        user.aid = req.aid
    await db.commit()
    return {"status": 200, "msg": "修改成功"}


@router.get("/message")
async def get_messages(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(Message)
        .where(Message.uid == user.uid)
        .order_by(Message.time.desc())
        .limit(5)
    )
    messages = result.scalars().all()
    return {"status": 200, "data": [{"content": m.content, "time": str(m.time)} for m in messages]}


@router.get("/getUserList")
async def get_user_list(
    aid: int | None = None,
    name: str | None = None,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    query = select(User).where(User.state == 3)
    if aid:
        query = query.where(User.aid == aid)
    if name:
        query = query.where(User.name.contains(name))
    result = await db.execute(query)
    users = result.scalars().all()
    return {
        "status": 200,
        "data": [
            {"uid": u.uid, "name": u.name, "tel": u.tel, "email": u.email, "aid": u.aid}
            for u in users
        ],
    }


@router.get("/getSelectUser")
async def get_select_user(aid: int, db: AsyncSession = Depends(get_db)):
    """下拉选择框用"""
    result = await db.execute(
        select(User).where(User.aid == aid, User.state == 3)
    )
    users = result.scalars().all()
    return [{"name": u.name, "value": u.uid} for u in users]


@router.post("/insertUser")
async def insert_user(
    req: RegisterReq,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    new_user = User(
        uid=req.uid, tel=req.tel, name=req.name,
        password=hash_password(req.password) if req.password else None,
        email=req.email, aid=req.aid, state=3,
    )
    db.add(new_user)
    await db.commit()
    return {"status": 200}


@router.post("/deleteUser")
async def delete_user(
    uid: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    await db.execute(delete(User).where(User.uid == uid))
    await db.commit()
    return {"status": 200}


@router.get("/getIndexInfo")
async def get_index_info(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    return {
        "status": 200,
        "data": {"uid": user.uid, "name": user.name, "tel": user.tel, "aid": user.aid},
    }
