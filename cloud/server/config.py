"""应用配置，全部从环境变量读取"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # MySQL
    db_host: str = "127.0.0.1"
    db_port: int = 3306
    db_user: str = "root"
    db_password: str = "safely123"
    db_name: str = "anbao"

    # Redis
    redis_host: str = "127.0.0.1"
    redis_port: int = 6379
    redis_password: str = ""

    # RabbitMQ
    mq_host: str = "127.0.0.1"
    mq_port: int = 5672
    mq_user: str = "anbao_mq"
    mq_password: str = "safely123"
    mq_vhost: str = "/"

    # S3 / MinIO
    s3_endpoint: str = "http://127.0.0.1:9000"
    s3_access_key: str = "minioadmin"
    s3_secret_key: str = "minioadmin"
    s3_bucket: str = "safely-videos"

    # 安全
    secret_key: str = "change-me-in-production"
    device_api_key: str = "change-me"
    mq_signing_secret: str = "change-me"

    # 服务
    host: str = "0.0.0.0"
    port: int = 8000

    @property
    def database_url(self) -> str:
        return (
            f"mysql+aiomysql://{self.db_user}:{self.db_password}"
            f"@{self.db_host}:{self.db_port}/{self.db_name}?charset=utf8mb4"
        )

    @property
    def redis_url(self) -> str:
        if self.redis_password:
            return f"redis://:{self.redis_password}@{self.redis_host}:{self.redis_port}/0"
        return f"redis://{self.redis_host}:{self.redis_port}/0"

    @property
    def mq_url(self) -> str:
        return (
            f"amqp://{self.mq_user}:{self.mq_password}"
            f"@{self.mq_host}:{self.mq_port}/{self.mq_vhost}"
        )

    class Config:
        env_prefix = ""
        env_file = ".env"


settings = Settings()
