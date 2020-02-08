from multiprocessing import Process
import cv2
import time
import communicate
from global_var import run_threads, r
import threading
from density import crowd
from PIL import Image, ImageDraw, ImageFont
import numpy as np
from sparse import sparse


class Device(Process):

    def __init__(self, deviceID, deviceUrl, deviceName):
        Process.__init__(self)
        self.deviceID = deviceID
        self.deviceName = deviceName
        self.deviceUrl = deviceUrl
        self.numberList = []  # 人头树列表，用于统计平均值/最大值灯
        self.__interval = 600  # 上传间隔时间(s) 每10分钟传统计数据
        self.__timer = None  # 定时器
        self.__isExceedThreshold = False
        threshold = communicate.deviceState(self.deviceID, "1")  # 设备登录，获取阈值
        r.hmset(self.deviceID, {'isUploadImage': 0, 'isUploadVideo': 0, 'threshold': threshold, 'isRun': 1})

    def run(self):
        # video_capture = cv2.VideoCapture(self.deviceUrl)
        # video_capture = cv2.VideoCapture("rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov")
        video_capture = cv2.VideoCapture("./1.mp4")
        fourcc = cv2.VideoWriter_fourcc(*'XVID')
        size = (int(video_capture.get(cv2.CAP_PROP_FRAME_WIDTH)), int(video_capture.get(cv2.CAP_PROP_FRAME_HEIGHT)))
        fps = video_capture.get(cv2.CAP_PROP_FPS)
        out = None
        self.__timer = threading.Timer(self.__interval, self.exec_callback)
        self.__timer.start()
        startExceedTime = None
        lastHead = 0  # 上一帧检测出的人头

        while int(r.hget(self.deviceID, 'isRun').decode()):
            ret, frame = video_capture.read()  # frame shape 640*480*3
            if ret is False:
                print(self.deviceID + "设备异常")
                continue
            # if lastHead<25:
            #     head = sparse.detect(frame)
            # else:
            #     head = crowd.detect(frame)
            head = crowd.detect(frame)
            # print("head is" + str(head))
            lastHead = head
            frame = change_cv2_draw(frame, str(head), self.deviceName)
            self.numberList.append(head)
            threshold = r.hget(self.deviceID, 'threshold').decode()
            # print("threshold is" + threshold)
            # 录制视频与传人头
            if int(threshold) != -1 and int(head) > int(threshold):
                self.__isExceedThreshold = True
                # 保存视频
                if out is None:
                    threading.Thread(target=communicate.deviceState, args=(self.deviceID, "2")).start()
                    startExceedTime = time.time()
                    out = cv2.VideoWriter("./tempVideo/"+self.deviceID + ".mp4", fourcc, fps, size)
                out.write(frame)  # 保存录像结果
                # 传人头
                communicate.realtime_upload(self.deviceID, head)
            else:
                self.__isExceedThreshold = False

            if self.__isExceedThreshold is False and out is not None:
                endTime = time.time()
                out.release()
                out = None
                # 恢复正常状态 上传视频
                threading.Thread(target=communicate.deviceState, args=(self.deviceID, "1")).start()
                threading.Thread(target=communicate.up_video, args=
                ("./tempVideo/"+self.deviceID + ".mp4", self.deviceID, int(startExceedTime), int(endTime))).start()

            if r.hget(self.deviceID, 'isUploadImage').decode() == '1':
                r.hset(self.deviceID, 'isUploadImage', 0)
                t = threading.Thread(target=self.upLoadImage, args=(frame,))
                t.start()

            cv2.imshow(self.deviceID, frame)
            if cv2.waitKey(25) & 0xFF == ord('q'):
                break
        video_capture.release()

    def upLoadImage(self, frame):
        cv2.imwrite("./tempImage/"+self.deviceID + ".jpg", frame)
        communicate.up_image("./tempImage/"+self.deviceID + ".jpg")

    def exec_callback(self):
        self.upload_statistic()
        self.__timer = threading.Timer(self.__interval, self.exec_callback)
        self.__timer.start()

    def upload_statistic(self):
        communicate.up_statistic(self.deviceID, self.numberList)
        self.numberList.clear()


def changeIsUpImage(deviceID):
    for t in run_threads:
        # print(t.deviceID)
        if t.deviceID == deviceID:
            r.hmset(deviceID, {'isUploadImage': 1})
            break


def changeThreshold(deviceID, attribute):
    for t in run_threads:
        # print(t.deviceID)
        if t.deviceID == deviceID:
            r.hmset(deviceID, {'threshold': attribute})
            break


def change_cv2_draw(image, headCount, plance):
    # 将opencv图像格式转换成PIL格式, 数据类型是PIL.Image.Image
    img_PIL = Image.fromarray(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
    # 字体颜色
    fillColor = (255, 0, 0)
    # 文字输出位置
    head_position = (50, 50)
    plance_position = (50, 100)
    # 输出内容
    str = '人头数:'
    str2 = '地点名:'
    draw = ImageDraw.Draw(img_PIL)
    # 字体，默认的路径/usr/share/fonts/opentype/noto/NotoSansCJK-Black.ttc，我把它拷贝过来了
    font = ImageFont.truetype('NotoSansCJK-Black.ttc', 34)
    draw.text(head_position, str + headCount, fill=fillColor, font=font)
    draw.text(plance_position, str2 + plance, fill=fillColor, font=font)
    # 转换回OpenCV格式
    out_img = cv2.cvtColor(np.asarray(img_PIL), cv2.COLOR_RGB2BGR)
    return out_img
