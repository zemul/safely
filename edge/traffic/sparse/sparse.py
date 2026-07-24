# coding:utf-8
"""
稀疏场景人头检测 (FCHD - Faster-RCNN 变体)
输入视频帧 → Region Proposal → 人头边界框 → 计数
"""

from __future__ import division

import os
import cv2
import numpy as np
from PIL import Image

# 检测阈值
THRESH = 0.0075
# 输入图片缩放到统一尺寸
INPUT_SIZE = (640, 480)

# 模型路径（从环境变量或相对路径获取）
_model_path = os.environ.get(
    "FCHD_MODEL_PATH",
    os.path.join(os.path.dirname(os.path.abspath(__file__)), "checkpoints", "head_detector_final")
)

# 延迟加载：避免无 GPU 时 import 就崩溃
_head_detector = None
_trainer = None
_use_cuda = False


def _init_model():
    """延迟初始化模型（首次调用时加载）"""
    global _head_detector, _trainer, _use_cuda

    if _trainer is not None:
        return True

    try:
        import torch
        from src.head_detector_vgg16 import Head_Detector_VGG16
        from trainer import Head_Detector_Trainer

        _use_cuda = torch.cuda.is_available()
        _head_detector = Head_Detector_VGG16(ratios=[1], anchor_scales=[2, 4])
        _trainer = Head_Detector_Trainer(_head_detector)

        if _use_cuda:
            _trainer = _trainer.cuda()

        if os.path.exists(_model_path):
            _trainer.load(_model_path)
            print(f"[FCHD] 模型加载完成 (CUDA: {_use_cuda})")
            return True
        else:
            print(f"[FCHD] 模型文件不存在: {_model_path}")
            _trainer = None
            return False
    except Exception as e:
        print(f"[FCHD] 模型初始化失败: {e}")
        _trainer = None
        return False


def _preprocess(frame):
    """
    预处理视频帧

    Args:
        frame: BGR numpy array (OpenCV 格式)

    Returns:
        (tensor, scale) 或 None
    """
    import src.array_tool as at
    from data.dataset import preprocess

    # OpenCV BGR → PIL RGB
    img_pil = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
    img_pil = img_pil.resize(INPUT_SIZE, Image.LANCZOS)

    img = np.asarray(img_pil, dtype=np.float32)
    H, W, _ = img.shape  # (H, W, C)

    # 转为 (C, H, W)
    img = img.transpose((2, 0, 1))
    img = preprocess(img)
    _, o_H, o_W = img.shape
    scale = o_H / H

    # 转 tensor, 增加 batch 维度
    img_tensor = at.totensor(img)[None, :, :, :]

    if _use_cuda:
        img_tensor = img_tensor.cuda().float()
    else:
        img_tensor = img_tensor.float()

    return img_tensor, scale


def detect(frame):
    """
    人头检测

    Args:
        frame: BGR 格式的 numpy 数组 (OpenCV 帧)

    Returns:
        int: 检测到的人头数量，失败返回 -1
    """
    try:
        if not _init_model():
            return -1

        img_tensor, scale = _preprocess(frame)
        pred_bboxes, _ = _head_detector.predict(img_tensor, scale, mode='evaluate', thresh=THRESH)
        return int(pred_bboxes.shape[0])
    except Exception as e:
        print(f"[FCHD] 检测失败: {e}")
        return -1
