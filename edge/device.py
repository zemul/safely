from multiprocessing import Process
import os
import cv2
import time
import communicate
from global_var import run_threads, r
import threading
from density import crowd
from PIL import Image, ImageDraw, ImageFont
import numpy as np

# 稀疏/密集场景切换阈值
SPARSE_THRESHOLD = 25  # 人数低于此值用人头检测，高于用密度估计

# 延迟导入 sparse（可能无 GPU）
_sparse_available = None

def _check_sparse():
    """检测 sparse 模块是否可用"""
    global _sparse_available
    if _sparse_available is None:
        try:
            from sparse import sparse
            _sparse_available = True
        except Exception as e:
            print(f"[Device] sparse 模块不可用，仅使用密度估计: {e}")
            _sparse_available = False
    return _sparse_available


def detect_frame(frame, last_count):
    """
    根据上一帧人数自动选择检测算法

    Args:
        frame: 视频帧
        last_count: 上一帧检测到的人数

    Returns:
        int: 当前帧人数
    """
    if last_count < SPARSE_THRESHOLD and _check_sparse():
        # 稀疏场景：用人头检测（更精确）
        from sparse import sparse
        result = sparse.detect(frame)
    else:
        # 密集场景：用密度估计（更快）
        result = crowd.detect(frame)

    # 检测失败时返回上一帧值
    if result < 0:
        return last_count
    return result


class Device(Process):

    def __init__(self, deviceID, deviceUrl, deviceName):
        Process.__init__(self)
        self.deviceID = deviceID
        self.deviceName = deviceName
        self.deviceUrl = deviceUrl
        self.numberList = []  # 人头数列表，用于统计平均值/最大值等
        self.__interval = 600  # 上传间隔时间(s) 每10分钟传统计数据
        self.__timer = None
        self.__isExceedThreshold = False
        threshold = communicate.deviceState(self.deviceID, "1")  # 设备登录，获取阈值
        r.hmset(self.deviceID, {'isUploadImage': 0, 'isUploadVideo': 0, 'threshold': threshold, 'isRun': 1})

    def run(self):
        video_capture = cv2.VideoCapture(self.deviceUrl)
        if not video_capture.isOpened():
            print(f"[{self.deviceID}] 无法打开视频源: {self.deviceUrl}")
            return

        fourcc = cv2.VideoWriter_fourcc(*'XVID')
        size = (int(video_capture.get(cv2.CAP_PROP_FRAME_WIDTH)), int(video_capture.get(cv2.CAP_PROP_FRAME_HEIGHT)))
        fps = video_capture.get(cv2.CAP_PROP_FPS) or 25
        out = None
        self.__timer = threading.Timer(self.__interval, self.exec_callback)
        self.__timer.start()
        startExceedTime = None
        lastHead = 0

        while int(r.hget(self.deviceID, 'isRun').decode()):
            ret, frame = video_capture.read()
            if not ret:
                print(f"[{self.deviceID}] 读帧失败，尝试重连...")
                time.sleep(2)
                video_capture.release()
                video_capture = cv2.VideoCapture(self.deviceUrl)
                continue

            # 自动选择检测算法
            head = detect_frame(frame, lastHead)
            lastHead = head
            frame = change_cv2_draw(frame, str(head), self.deviceName)
            self.numberList.append(head)

            # 获取阈值
            try:
                threshold = int(r.hget(self.deviceID, 'threshold').decode())
            except (TypeError, ValueError):
                threshold = -1

            # 录制视频与推送人数
            if threshold != -1 and head > threshold:
                self.__isExceedThreshold = True
                if out is None:
                    threading.Thread(target=communicate.deviceState, args=(self.deviceID, "2")).start()
                    startExceedTime = time.time()
                    out = cv2.VideoWriter("./tempVideo/" + self.deviceID + ".mp4", fourcc, fps, size)
                out.write(frame)
                communicate.realtime_upload(self.deviceID, head)
            else:
                self.__isExceedThreshold = False

            if not self.__isExceedThreshold and out is not None:
                endTime = time.time()
                out.release()
                out = None
                threading.Thread(target=communicate.deviceState, args=(self.deviceID, "1")).start()
                threading.Thread(target=communicate.up_video, args=(
                    "./tempVideo/" + self.deviceID + ".mp4", self.deviceID, int(startExceedTime), int(endTime)
                )).start()

            # 云端请求截图
            if r.hget(self.deviceID, 'isUploadImage').decode() == '1':
                r.hset(self.deviceID, 'isUploadImage', 0)
                threading.Thread(target=self.upLoadImage, args=(frame,)).start()

            cv2.imshow(self.deviceID, frame)
            if cv2.waitKey(25) & 0xFF == ord('q'):
                break

        video_capture.release()
        if self.__timer:
            self.__timer.cancel()

    def upLoadImage(self, frame):
        path = "./tempImage/" + self.deviceID + ".jpg"
        cv2.imwrite(path, frame)
        communicate.up_image(path)

    def exec_callback(self):
        self.upload_statistic()
        self.__timer = threading.Timer(self.__interval, self.exec_callback)
        self.__timer.start()

    def upload_statistic(self):
        if self.numberList:
            communicate.up_statistic(self.deviceID, self.numberList)
            self.numberList.clear()


def changeIsUpImage(deviceID):
    for t in run_threads:
        if t.deviceID == deviceID:
            r.hmset(deviceID, {'isUploadImage': 1})
            break


def changeThreshold(deviceID, attribute):
    for t in run_threads:
        if t.deviceID == deviceID:
            r.hmset(deviceID, {'threshold': attribute})
            break


def change_cv2_draw(image, headCount, plance):
    """在画面上绘制人数和地点信息"""
    try:
        img_PIL = Image.fromarray(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
        fillColor = (255, 0, 0)
        draw = ImageDraw.Draw(img_PIL)
        font_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'NotoSansCJK-Black.ttc')
        if os.path.exists(font_path):
            font = ImageFont.truetype(font_path, 34)
        else:
            font = ImageFont.load_default()
        draw.text((50, 50), '人头数:' + headCount, fill=fillColor, font=font)
        draw.text((50, 100), '地点名:' + plance, fill=fillColor, font=font)
        return cv2.cvtColor(np.asarray(img_PIL), cv2.COLOR_RGB2BGR)
    except Exception:
        return image
