"""WebSocket 实时推送 —— 管理端连接后收到设备实时人数更新"""

import asyncio
import json
from collections import defaultdict

from fastapi import APIRouter, WebSocket, WebSocketDisconnect

router = APIRouter()

# mac -> set of connected websockets
connections: dict[str, set[WebSocket]] = defaultdict(set)


@router.websocket("/ws/{mac}")
async def websocket_endpoint(websocket: WebSocket, mac: str):
    await websocket.accept()
    connections[mac].add(websocket)
    try:
        while True:
            # 保持连接，接收客户端心跳
            await websocket.receive_text()
    except WebSocketDisconnect:
        connections[mac].discard(websocket)


async def broadcast(mac: str, data: dict):
    """向订阅了某设备的所有前端推送消息"""
    message = json.dumps(data, ensure_ascii=False)
    dead = set()
    for ws in connections.get(mac, set()):
        try:
            await ws.send_text(message)
        except Exception:
            dead.add(ws)
    connections[mac] -= dead
