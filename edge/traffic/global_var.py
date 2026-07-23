from mysqlHelper import *

import redis, config
mysqlHelper = MysqlHelper()

# Redis 连接池（支持密码认证）
_redis_kwargs = {
    'host': config.RedisHost,
    'port': int(config.RedisPort),
}
if hasattr(config, 'RedisPassword') and config.RedisPassword:
    _redis_kwargs['password'] = config.RedisPassword

pool = redis.ConnectionPool(**_redis_kwargs)
r = redis.Redis(connection_pool=pool)

# 已运行设备
run_threads = []
