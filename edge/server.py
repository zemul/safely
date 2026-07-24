import os, time
import global_var
import communicate
import threading
from device import Device
import multiprocessing as mp


# 边缘端若重启后重新初始化
def edge_init():
    sql = 'select deviceID,deviceUrl,deviceName from device'
    results = global_var.mysqlHelper.get_all(sql)
    for result in results:
        # 设备id 网络摄像头url 设备别名
        temp = result[0] + "@" + result[1] + "@" + result[2]
        global_var.r.lpush("not_run", temp)


if __name__ == "__main__":
    # mp.set_start_method('spawn')
    edge_init()
    # rabbitMQ 接受广播以修改设备线程
    p = threading.Thread(target=communicate.change_device)
    p.start()
    while True:
        # 监听是否有新设备
        not_run = global_var.r.lpop("not_run")
        if not_run is not None:
            run = not_run.decode().split('@')
            # print(run[2])
            t1 = Device(run[0], run[1],run[2])
            global_var.run_threads.append(t1)
            # print(t1.deviceID)
            t1.start()
        else:
            time.sleep(10)
