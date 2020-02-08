from __future__ import division

import cv2
from src.head_detector_vgg16 import Head_Detector_VGG16
from trainer import Head_Detector_Trainer
from PIL import Image
import numpy as np
from data.dataset import preprocess
import src.array_tool as at

THRESH = 0.0075
IM_RESIZE = True

model_path = "/home/zhang/FCHD-Fully-Convolutional-Head-Detector/checkpoints/head_detector_final"
head_detector = Head_Detector_VGG16(ratios=[1], anchor_scales=[2, 4])
trainer = Head_Detector_Trainer(head_detector).cuda()
trainer.load(model_path)


def read_img(path):
    f = Image.fromarray(path)
    if IM_RESIZE:
        f = f.resize((640, 480), Image.ANTIALIAS)

    f.convert('RGB')
    img_raw = np.asarray(f, dtype=np.uint8)
    img_raw_final = img_raw.copy()
    img = np.asarray(f, dtype=np.float32)
    _, H, W = img.shape
    img = img.transpose((2, 0, 1))
    img = preprocess(img)
    _, o_H, o_W = img.shape
    scale = o_H / H
    return img, img_raw_final, scale


def detect(img):
    img, img_raw, scale = read_img(img)
    img = at.totensor(img)
    img = img[None, :, :, :]
    img = img.cuda().float()
    pred_bboxes_, _ = head_detector.predict(img, scale, mode='evaluate', thresh=THRESH)
    # for i in range(pred_bboxes_.shape[0]):
    # ymin, xmin, ymax, xmax = pred_bboxes_[i,:]
    # utils.draw_bounding_box_on_image_array(img_raw,ymin, xmin, ymax, xmax)
    print(str(pred_bboxes_.shape[0]))
    return pred_bboxes_.shape[0]


if __name__ == "__main__":
    img_path = "./6.jpg"
    frame = cv2.imread(img_path)
    detect(frame)
