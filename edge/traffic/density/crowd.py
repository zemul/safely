# coding:utf-8
"""
密集场景人群密度估计 (CSRNet)
输入视频帧 → 生成密度图 → 像素值积分 → 人数估计
"""

import numpy as np
from cv2 import dnn
import os
import sys

# 模型路径
_model_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)))
_prototxt = os.path.join(_model_dir, 'density.prototxt')
_caffemodel = os.path.join(_model_dir, 'density.caffemodel')

# 模型只加载一次
_net = None

def _get_net():
    global _net
    if _net is None:
        if not os.path.exists(_prototxt) or not os.path.exists(_caffemodel):
            raise FileNotFoundError(f"CSRNet 模型文件缺失: {_model_dir}")
        _net = dnn.readNetFromCaffe(_prototxt, _caffemodel)
        print("[CSRNet] 模型加载完成")
    return _net


# 密度图归一化系数（与模型训练时一致）
DENSITY_SCALE = 1000.0


def detect(frame):
    """
    密度估计检测

    Args:
        frame: BGR 格式的 numpy 数组 (OpenCV 帧)

    Returns:
        int: 估计人数，失败返回 -1
    """
    try:
        net = _get_net()
        blob = dnn.blobFromImage(frame, 1.0, (1024, 768), (0, 0, 0), True)
        net.setInput(blob)
        density = net.forward()
        density = density / DENSITY_SCALE
        person_num = int(np.sum(density))
        return max(0, person_num)  # 人数不为负
    except Exception as e:
        print(f"[CSRNet] 检测失败: {e}")
        return -1
