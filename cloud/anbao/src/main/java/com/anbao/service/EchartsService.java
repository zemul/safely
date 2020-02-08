package com.anbao.service;

import com.anbao.pojo.Flowdata;

import java.util.List;

import com.anbao.pojo.echaets.areaBarChart;
import com.anbao.pojo.flow;
public interface EchartsService {

    List<flow> getHistory(String mac,String num);

    List<flow> getBetweenDayHistory(String mac, String day1, String day2);

    List<areaBarChart> getAreaBarchart();

    List<areaBarChart> getDeviceBarchart(String aid);

    List<flow> getForecast(String mac);
}
