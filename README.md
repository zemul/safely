# safely
中国软件杯-公共地点人流量的云监管平台




cloud：
	
	html：云端前台
	
	safely.war：云端后台
	
	videoIo:视频传输模块，建议分别部署，避免视频流量影响正常业务。
	
	time_serie_ARIMA:流量预测

edge：
	
	flask：边缘端前台
	
	traffic:边缘端后台
![图片说明1](./build.png)
