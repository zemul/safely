BaseServer = "http://39.108.61.148:8080/"

# Redis
RedisHost = 'localhost'
RedisPort = '6379'

# MySql
MysqlHost = "localhost"
MysqlPort = 3306
MysqlDb = 'py3'
MysqlUserName = 'root'
MysqlPassword = "root"

# 每隔一段时间上传统计数据
UpIntervals = BaseServer + "normalup/"
# 修改设备状态
DeviceState = BaseServer + "upState/"
# 上传图片
upImage = BaseServer + "device/getImage/"
# 上传视频
upVideo = BaseServer + "uploadVideo/"

MQ = {
    "userName":"hadoop",
    "password":"hadoop",
    "host":"39.108.61.148",
    "port":5672
}

# MQ = {
#     "userName": "admin",
#     "password": "admin",
#     "host": "127.0.0.1",
#     "port": 5672
# }
