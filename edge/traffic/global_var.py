from mysqlHelper import *

import redis, config
mysqlHelper = MysqlHelper()


pool = redis.ConnectionPool(host=config.RedisHost, port=config.RedisPort)

r = redis.Redis(connection_pool=pool)

# 已运行设备
run_threads = []