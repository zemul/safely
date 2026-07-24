"""SQLAlchemy ORM 模型，对应 db/init.sql 的 8 张表"""

from datetime import datetime

from sqlalchemy import (
    Column, DateTime, Double, ForeignKey, Integer, String, Text, func
)
from sqlalchemy.orm import DeclarativeBase, relationship


class Base(DeclarativeBase):
    pass


class Area(Base):
    __tablename__ = "area"

    aid = Column(Integer, primary_key=True, autoincrement=True)
    addr = Column(String(255), nullable=False)

    devices = relationship("Monitored", back_populates="area")
    users = relationship("User", back_populates="area")


class User(Base):
    __tablename__ = "user"

    uid = Column(String(50), primary_key=True)
    tel = Column(String(20), unique=True, nullable=False)
    name = Column(String(50))
    password = Column(String(100))
    state = Column(Integer, default=2)  # 2=管理员, 3=普通用户
    email = Column(String(100))
    aid = Column(Integer, ForeignKey("area.aid"))

    area = relationship("Area", back_populates="users")


class Monitored(Base):
    """设备表"""
    __tablename__ = "monitored"

    mac = Column(String(50), primary_key=True)
    aid = Column(Integer, ForeignKey("area.aid"))
    node = Column(String(100))  # 设备名/节点名
    state = Column(String(20), default="offline")
    threshold = Column(Integer, default=30)
    createtime = Column(DateTime, default=func.now())
    time = Column(DateTime)
    num = Column(Integer, default=0)

    area = relationship("Area", back_populates="devices")


class Flowdata(Base):
    """人流量统计数据"""
    __tablename__ = "flowdata"

    id = Column(Integer, primary_key=True, autoincrement=True)
    mac = Column(String(50), ForeignKey("monitored.mac"), nullable=False)
    time = Column(DateTime, nullable=False)
    avg = Column(Double)
    max = Column(Integer)
    center = Column(Double)
    variance = Column(Double)


class Exception_(Base):
    """超阈值异常事件"""
    __tablename__ = "exception"

    eid = Column(String(50), primary_key=True)
    inittime = Column(DateTime)
    continuetime = Column(String(50))
    videourl = Column(String(500))
    mac = Column(String(50), ForeignKey("monitored.mac"))


class UserMac(Base):
    """用户-设备关联"""
    __tablename__ = "user_mac"

    id = Column(Integer, primary_key=True, autoincrement=True)
    uid = Column(String(50), ForeignKey("user.uid"))
    mac = Column(String(50), ForeignKey("monitored.mac"))


class Message(Base):
    """站内消息"""
    __tablename__ = "message"

    id = Column(Integer, primary_key=True, autoincrement=True)
    uid = Column(String(50), ForeignKey("user.uid"))
    content = Column(Text)
    time = Column(DateTime, default=func.now())


class Forecast(Base):
    """Prophet 预测结果"""
    __tablename__ = "forecast"

    id = Column(Integer, primary_key=True, autoincrement=True)
    mac = Column(String(50), ForeignKey("monitored.mac"))
    time = Column(DateTime, nullable=False)
    avg = Column(Double)
