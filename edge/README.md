[1]这是边缘端业务逻辑及算法部分（稀疏场景检测/密集场景检测）
[2]请安装mysql和redis并修改myconfig配置文件并安装相关环境依赖
[3]算法识别效果视频已放入tempVideo文件夹。
[4]稀疏场景算法的模型文件已传至百度网盘 
链接：https://pan.baidu.com/s/1K6HLh2QhJrCaYCf7M8IORA 
提取码：oxk3
请修改 sparse/src/config.py文件里的caffe_pretrain_path 和model_save_path 路径为自己跌实际路径 
将detect放入sparse/checkpoints 文件夹下
将vgg16_caffe.pth 放入sparse/data/pretrained_model 文件夹下
[5]部署详情请参考：部署文档
