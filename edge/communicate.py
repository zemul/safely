import requests
import json
import numpy as np
import os
import hmac
import hashlib
import config
from config import MQ
import pika
import time

DEVICE_API_KEY = os.environ.get("DEVICE_API_KEY", "dev-unsafe-default-key")
MQ_SECRET = os.environ.get("MQ_SIGNING_SECRET", DEVICE_API_KEY)
_api_headers = {"X-API-KEY": DEVICE_API_KEY}

# rabbitMQ
credentials = pika.PlainCredentials(MQ['userName'], MQ['password'])
producer = pika.BlockingConnection(pika.ConnectionParameters(
    MQ['host'], MQ['port'], '/', credentials))
consumer = pika.BlockingConnection(pika.ConnectionParameters(
    MQ['host'], MQ['port'], '/', credentials))


# 上传视频
def up_video(video_path,deviceID, inittime, continuetime):
    files = {'file': open(video_path, 'rb')}
    data = dict()

    data['inittime'] = inittime
    data['continuetime'] = continuetime
    data['mac'] = deviceID
    r = requests.post(config.upVideo, data=data, files=files, headers=_api_headers)
    print(r.text)


# 上传图片
def up_image(img_path):
    files = {'file': open(img_path, 'rb')}
    requests.post(config.upImage, files=files, headers=_api_headers)


# 设备登录与退出
def deviceState(equip_id, flag):
    try:
        data = dict()
        data['id'] = equip_id
        data['flag'] = flag
        r = requests.post(config.DeviceState, data=data, headers=_api_headers)
        result = r.text
        print("threshold is"+result)
    except Exception as e:
        result = -1
    return int(result)


# 每隔一段时间上传一次统计数据
def up_statistic(equip_id, numberList):
    # print("numberList is"+str(numberList))
    data = dict()
    data['mac'] = equip_id
    data['time'] = time.strftime("%Y-%m-%d %H:%M")
    data['max'] = str(np.max(numberList))
    data['avg'] = str(np.mean(numberList))
    data['variance'] = str(np.var(numberList))
    data['center'] = str(np.median(numberList))
    r = requests.post(config.UpIntervals, data=data, headers=_api_headers)
    print(r.text)


# 接受云端请求，改变设备阈值
def change_device():
    channel = consumer.channel()
    channel.exchange_declare(exchange='change', exchange_type='fanout')
    result = channel.queue_declare("", exclusive=True)
    queue_name = result.method.queue
    # print(queue_name)
    # print(' [*] Waiting for logs. To exit press CTRL+C')
    print('start waiting cloud')
    channel.queue_bind(exchange='change',  # 绑定到这个转发器上，只能从这个转发器上接收
                       queue=queue_name)

    channel.basic_consume(queue_name,
                          callback
                          )
    channel.start_consuming()


# 回调函数
def callback(ch, method, properties, body):
    from device import changeThreshold, changeIsUpImage
    msg = body.decode('utf-8')

    # 验证签名（如果消息包含 | 分隔符）
    if '|' in msg:
        parts = msg.rsplit('|', 1)
        payload = parts[0]
        signature = parts[1]
        expected = hmac.new(MQ_SECRET.encode(), payload.encode(), hashlib.sha256).hexdigest()
        if not hmac.compare_digest(signature, expected):
            print("[SECURITY] Invalid MQ message signature, dropping")
            ch.basic_ack(delivery_tag=method.delivery_tag)
            return
        result = str.split(payload)
    else:
        # 向后兼容：无签名消息（过渡期）
        result = str.split(msg)

    print(result)
    if result[0] == '0':
        changeThreshold(result[1], result[2])
    elif result[0] == '1':
        changeIsUpImage(result[1])

    ch.basic_ack(delivery_tag=method.delivery_tag)


# 超过阈值后上传人头数
def realtime_upload(deviceID, number):
    payload = deviceID + " " + str(number)
    # HMAC-SHA256 签名
    signature = hmac.new(MQ_SECRET.encode(), payload.encode(), hashlib.sha256).hexdigest()
    # 格式: payload|signature
    signed_msg = payload + "|" + signature
    channel = producer.channel()
    channel.basic_publish(exchange="order",
                          routing_key='queue',
                          body=signed_msg.encode('utf-8'))
