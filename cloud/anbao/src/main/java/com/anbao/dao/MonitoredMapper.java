package com.anbao.dao;

import com.anbao.pojo.Monitored;
import com.anbao.pojo.MonitoredExample;
import com.anbao.pojo.User;
import com.anbao.pojo.echaets.areaBarChart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MonitoredMapper {
    int countByExample(MonitoredExample example);

    int deleteByExample(MonitoredExample example);

    int deleteByPrimaryKey(String mac);

    int insert(Monitored record);

    int insertSelective(Monitored record);

    List<Monitored> selectByExample(MonitoredExample example);

    Monitored selectByPrimaryKey(String mac);

    int updateByExampleSelective(@Param("record") Monitored record, @Param("example") MonitoredExample example);

    int updateByExample(@Param("record") Monitored record, @Param("example") MonitoredExample example);

    int updateByPrimaryKeySelective(Monitored record);

    int updateByPrimaryKey(Monitored record);

    List<areaBarChart> getAreaBarchart();

    List<areaBarChart> getDeviceBarchart(@Param("aid") String aid);

    List<User> getDeviceWithUserTel(@Param("mac") String id);
}