import os

BaseServer = os.environ.get("BASE_SERVER", "http://localhost:8080/")

# MySql
MysqlHost = os.environ.get("MYSQL_HOST", "localhost")
MysqlPort = int(os.environ.get("MYSQL_PORT", "3306"))
MysqlDb = os.environ.get("MYSQL_DB", "py3")
MysqlUserName = os.environ.get("MYSQL_USERNAME", "root")
MysqlPassword = os.environ.get("MYSQL_PASSWORD", "")

# 每隔一段时间上传统计数据
UpIntervals = BaseServer + "normalup/"
# 修改设备状态
DeviceState = BaseServer + "upState/"
# 上传图片
upImage = BaseServer + "device/getImage/"
# 上传视频
upVideo = BaseServer + "uploadVideo/"

MQ = {
    "userName": os.environ.get("RABBITMQ_USERNAME", "guest"),
    "password": os.environ.get("RABBITMQ_PASSWORD", ""),
    "host": os.environ.get("RABBITMQ_HOST", "localhost"),
    "port": int(os.environ.get("RABBITMQ_PORT", "5672"))
}
