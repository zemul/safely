package com.anbao.service;

import com.anbao.dao.FlowdataMapper;
import com.anbao.dao.MonitoredMapper;
import com.anbao.pojo.Flowdata;
import com.anbao.pojo.FlowdataExample;
import com.anbao.pojo.echaets.areaBarChart;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.anbao.pojo.flow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class EchartsServiceImpl implements EchartsService {
    @Autowired
    private FlowdataMapper flowdataMapper;
    @Autowired
    private MonitoredMapper monitoredMapper;

    @Override
    public List<flow> getHistory(String mac, String num) {
        System.out.println(num);
        List<flow> history = flowdataMapper.getHistory(mac,num);
        System.out.println(history);
        return  history;
    }


    //between day historyList
    public List<flow> getBetweenDayHistory(String mac, String day1,String day2) {
        if(day1!=day2){
            //对结束日期+1
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = sdf.parse(day2);
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.DATE,1);
                Date data2 = c.getTime();
                day2 = sdf.format(data2);
                System.out.println(day2);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        List<flow> history = flowdataMapper.getBetweenDayHistory(mac,day1,day2);
        return  history;
    }

    @Override
    public List<areaBarChart> getAreaBarchart() {
        return  monitoredMapper.getAreaBarchart();
    }

    @Override
    public List<areaBarChart> getDeviceBarchart(String aid) {
        return monitoredMapper.getDeviceBarchart(aid);

    }

    @Override
    public List<flow> getForecast(String mac) {
        return flowdataMapper.getForecast(mac);
    }

}
