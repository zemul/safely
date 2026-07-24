"""RabbitMQ 消费者 + 生产者：接收边缘端实时数据，下发指令"""

import asyncio
import json
import logging

import aio_pika
from aio_pika import ExchangeType

from config import settings

logger = logging.getLogger(__name__)

_connection: aio_pika.RobustConnection | None = None
_channel: aio_pika.Channel | None = None
_exchange: aio_pika.Exchange | None = None


async def connect():
    global _connection, _channel, _exchange
    _connection = await aio_pika.connect_robust(settings.mq_url)
    _channel = await _connection.channel()
    _exchange = await _channel.declare_exchange("safely.fanout", ExchangeType.FANOUT, durable=True)
    logger.info("RabbitMQ connected")


async def start_consumer(on_message):
    """启动消费者，监听边缘端实时数据"""
    if not _channel:
        await connect()

    queue = await _channel.declare_queue("safely.realtime", durable=True)
    await queue.bind(_exchange)

    async with queue.iterator() as queue_iter:
        async for message in queue_iter:
            async with message.process():
                try:
                    data = json.loads(message.body.decode())
                    await on_message(data)
                except Exception as e:
                    logger.error(f"MQ message error: {e}")


async def publish_command(mac: str, command: dict):
    """向边缘端下发指令（通过 fanout exchange）"""
    if not _exchange:
        await connect()
    body = json.dumps({"mac": mac, **command}, ensure_ascii=False).encode()
    await _exchange.publish(aio_pika.Message(body=body), routing_key="")


async def close():
    if _connection:
        await _connection.close()
