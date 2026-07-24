FROM python:3.8-slim

WORKDIR /app

# 系统依赖（OpenCV 需要）
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    libgl1-mesa-glx \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender1 \
    && rm -rf /var/lib/apt/lists/*

# Python 依赖
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 应用代码
COPY *.py ./
COPY density/__init__.py density/crowd.py ./density/
COPY sparse/ ./sparse/

# 创建临时目录
RUN mkdir -p tempVideo tempImage

CMD ["python", "server.py"]
