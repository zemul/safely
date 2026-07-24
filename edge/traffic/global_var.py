"""
全局状态管理（替代 Redis）
使用线程安全的内存存储，边缘端单机运行不需要外部 Redis
"""

import threading
from collections import deque
from mysqlHelper import MysqlHelper

mysqlHelper = MysqlHelper()

# 已运行设备
run_threads = []


class DeviceStore:
    """
    线程安全的设备状态存储
    替代 Redis 的 hset/hget/hmset 操作
    """

    def __init__(self):
        self._data = {}  # {deviceID: {field: value}}
        self._lock = threading.Lock()
        self._queue = deque()  # 替代 Redis list (lpush/lpop)

    def hmset(self, key, mapping):
        with self._lock:
            if key not in self._data:
                self._data[key] = {}
            self._data[key].update(mapping)

    def hset(self, key, field, value):
        with self._lock:
            if key not in self._data:
                self._data[key] = {}
            self._data[key][field] = value

    def hget(self, key, field):
        with self._lock:
            if key in self._data and field in self._data[key]:
                val = self._data[key][field]
                # 模拟 Redis 返回 bytes
                return str(val).encode()
            return b'0'

    def lpush(self, key, value):
        """模拟 Redis lpush（用作任务队列）"""
        with self._lock:
            self._queue.appendleft(value)

    def lpop(self, key):
        """模拟 Redis lpop"""
        with self._lock:
            if self._queue:
                return self._queue.pop().encode() if isinstance(self._queue[-1], str) else self._queue.pop()
            return None


# 全局实例（替代 redis.Redis 对象）
r = DeviceStore()
