package com.anbao.dao;

import com.anbao.pojo.Flowdata;
import com.anbao.pojo.FlowdataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import com.anbao.pojo.flow;

public interface FlowdataMapper {
    int countByExample(FlowdataExample example);

    int deleteByExample(FlowdataExample example);

    int insert(Flowdata record);

    int insertSelective(Flowdata record);

    List<Flowdata> selectByExample(FlowdataExample example);

    int updateByExampleSelective(@Param("record") Flowdata record, @Param("example") FlowdataExample example);

    int updateByExample(@Param("record") Flowdata record, @Param("example") FlowdataExample example);

    List<flow> getHistory(@Param("mac") String mac, @Param("num") String num);
    List<flow> getBetweenDayHistory(@Param("mac") String mac, @Param("day1") String day1,@Param("day2") String day2);

    String getLastTimeFlowNum(@Param("mac")String mac);

    List<flow> getForecast(String mac);
}