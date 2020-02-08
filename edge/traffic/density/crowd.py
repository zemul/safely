# coding:utf-8

from __future__ import print_function

import numpy as np
from cv2 import dnn
import os,sys


cm_path = sys.path[0]+"/density/"

def detect(frame):
    blob = dnn.blobFromImage(frame, 1, (1024, 768), (0, 0, 0),True)
    net = dnn.readNetFromCaffe(cm_path + 'density.prototxt', cm_path + 'density.caffemodel')

    net.setInput(blob)
    density = net.forward()

    density = density/1000.0

    person_num = np.sum(density[:])
    return int(person_num)
