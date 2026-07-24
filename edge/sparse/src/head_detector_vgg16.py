import torch as t
from torch import nn
from torchvision.models import vgg16

from src.region_proposal_network import RegionProposalNetwork
from src.head_detector import Head_Detector
from src.config import opt

def decom_vgg16():
    if opt.caffe_pretrain:
        # Load the caffe model
        model = vgg16(pretrained=False)
        model.load_state_dict(t.load(opt.caffe_pretrain_path))
    else:
        # Load the default model in PyTorch
        model = vgg16(pretrained=True)
    
    features = list(model.features)[:30]
    return nn.Sequential(*features)

class Head_Detector_VGG16(Head_Detector):

    feat_stride = 16
    def __init__(self, ratios=[0.5, 1, 2], anchor_scales=[8,16,32]):
        extractor = decom_vgg16()
        rpn = RegionProposalNetwork(
            512, 512,
            ratios=ratios,
            anchor_scales=anchor_scales,
            feat_stride=self.feat_stride
        )
        super(Head_Detector_VGG16, self).__init__(
            extractor,
            rpn
        )
    pass

