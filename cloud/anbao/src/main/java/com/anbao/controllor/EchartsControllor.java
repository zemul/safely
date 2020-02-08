package com.anbao.controllor;

import com.anbao.pojo.Flowdata;
import com.anbao.pojo.echaets.areaBarChart;
import com.anbao.service.EchartsService;
import com.anbao.utils.Result;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.anbao.pojo.flow;
import com.anbao.pojo.xydata;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping(method = RequestMethod.POST)
public class EchartsControllor  {
    @Autowired
    private EchartsService echartsService;

    @RequestMapping("/echarts/history")
    @ResponseBody
    public xydata getHistory(String mac,String num) throws IOException {

        List time = new ArrayList();
        List data = new ArrayList();
        List data2 = new ArrayList();
        List<flow> history = echartsService.getHistory(mac,num);
        for(flow f: history){

            time.add(f.getAvg());
            data.add(f.getData().substring(5,f.getData().length()-3));
            data2.add(f.getMax());
        }
        xydata xy = new xydata();
        xy.setXlist(time);
        xy.setYlist(data);
        xy.setZlist(data2);
        return xy;
    }

    //正常设备图表，最近24h，未来24h合并展示
    @RequestMapping(value = "/echarts/normal")
    @ResponseBody
    public Result getnormal(String mac)  {
        //最近24小时人流量
        List<flow> history = echartsService.getHistory(mac,"24h");
        //未来24小时预测数据
        List<flow> forecast = echartsService.getForecast(mac);
        return new Result(history,forecast);
    }



    @RequestMapping("/echarts/BetweenDayHistory")
    @ResponseBody
    public xydata getHistory(String mac,String day1,String day2) throws IOException {
        System.out.println(day1);
        mac="AAABBBCCC";
        System.out.println(day2);
        List time = new ArrayList();
        List data = new ArrayList();
        List<flow> history = echartsService.getBetweenDayHistory(mac,day1,day2);
        for(flow f: history){
            time.add(f.getAvg());
            data.add(f.getData().substring(5,f.getData().length()-3));
        }
        xydata xy = new xydata();
        xy.setXlist(time);
        xy.setYlist(data);
        return xy;
    }

    //查询边缘端报警记录，柱状图，高低排序
    @RequestMapping("/echarts/getAreaBarchart")
    @ResponseBody
    public List<areaBarChart> getAreaBarchart() throws IOException {
        List<areaBarChart> barchart = echartsService.getAreaBarchart();
        return barchart;
    }

    //查询区域报警记录，柱状图，高低排序
    @RequestMapping(value = "/echarts/getDeviceBarchart")
    @ResponseBody
    public List<areaBarChart> getDeviceBarchart(String aid)  {
        List<areaBarChart> barchart = echartsService.getDeviceBarchart(aid);
        return barchart;
    }

}
