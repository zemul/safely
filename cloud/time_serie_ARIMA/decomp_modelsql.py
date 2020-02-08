#-*- coding:utf-8 -*-
'''
软件杯人流量预测
'''

import numpy as np
from test_stationarity import *
from statsmodels.tsa.seasonal import seasonal_decompose
from statsmodels.tsa.arima_model import ARIMA
from datetime import timedelta
import time


#engine = create_engine('mysql+pymysql://root:123@localhost:3306/anbao')
import pymysql
db = pymysql.connect("localhost", "root", "123", "anbao")

class ModelDecomp(object):
    def __init__(self,mac, test_size=144):
        self.ts = self.read_data(mac)
        self.test_size = test_size
        self.train_size = len(self.ts)
        self.train = self.ts[:len(self.ts)]
        draw_ts(self.train)
        self.test = self.ts[-test_size:]

    def read_data(self,mac):
        sql = '''
              select time,avg from flowdata where mac='%s' AND time >=(NOW() - interval 100 day)
              '''%(mac)
        data = pd.read_sql_query(sql,db)
        #时间序列
        data = data.set_index('time')
        data.index = pd.to_datetime(data.index)
        #整个文件ts流量列
        ts = data['avg']
        draw_ts(ts)
        return ts


    def decomp(self, freq):
        '''
        对时间序列进行分解
        :param freq: 周期
        '''
        decomposition = seasonal_decompose(self.train, freq=freq, two_sided=False)
        self.trend = decomposition.trend
        self.seasonal = decomposition.seasonal
        self.residual = decomposition.resid
        # decomposition.plot()
        # plt.show()

        d = self.residual.describe()
        delta = d['75%'] - d['25%']

        self.low_error, self.high_error = (d['25%'] - 1 * delta, d['75%'] + 1 * delta)


    def trend_model(self, order):

        self.trend.dropna(inplace=True)

        self.trend_model = ARIMA(self.trend, order).fit(disp=-1, method='css')

        return self.trend_model

    def add_season(self):
        '''
        添加周期数据和残差数据
        '''
        self.train_season = self.seasonal
        values = []

        for i, t in enumerate(self.pred_time_index):
            trend_part = self.trend_pred[i]

            # 相同时间的数据均值
            season_part = self.train_season[
                self.train_season.index.time == t.time()
                ].mean()

            # 趋势+周期+误差界限
            predict = trend_part + season_part
            values.append(int(predict))
        self.final_pred = pd.Series(values, index=self.pred_time_index, name='predict')

    def predict_new(self):
        '''
        预测新数据
        '''
        n = self.test_size+18

        self.pred_time_index= pd.date_range(start=self.train.index[-1], periods=n+1, freq='10min')[1:]
        self.trend_pred= self.trend_model.forecast(n)[0]
        self.add_season()


def evaluate(mac):
    md = ModelDecomp(test_size=144, mac=mac)
    md.decomp(freq=144)
    md.trend_model(order=(1, 1, 3))
    md.predict_new()



if __name__ == '__main__':
    sql = '''
                SELECT mac,count(*) FROM flowdata  where   DATE_SUB(CURDATE(), INTERVAL 100 DAY) <= date(time) GROUP BY mac;
                  '''
    cursor = db.cursor()
    cursor.execute(sql)
    data = cursor.fetchall()
    buffer = []
    #mac，数据量
    for row in data:
        mac = row[0]
        sum = row[1]
        if(sum>=288):
            list = evaluate(mac)
            for i, v in list.items():
                time = i.to_pydatetime()
                buffer.append((time, v, mac))
            cursor.executemany("delete from forecast where mac= (%s)", mac)
            db.commit()
            cursor.executemany("insert into forecast(time,avg,mac) value(%s,%s,%s)", buffer)
            #清空缓存
            buffer = []
            db.commit()

    cursor.close()
    db.close















