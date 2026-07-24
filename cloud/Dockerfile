FROM python:3.11-slim

WORKDIR /app

# 系统依赖（Prophet 需要）
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc g++ && rm -rf /var/lib/apt/lists/*

COPY cloud/server/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY cloud/server/ ./server/
COPY cloud/html/ ./html/

WORKDIR /app/server

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
