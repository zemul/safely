package com.anbao.service;

import com.anbao.pojo.Area;
import com.anbao.pojo.DataResult;
import com.anbao.pojo.Monitored;

import java.util.List;

public interface AreaService {
    DataResult selectAllUser(Integer page, Integer limit,String equName);
    //修改区域信息
    void updateArea(Integer aid, String name, String tel);

    void deleteArea(Area area);

    void addArea(Area area);

    List<Area> getAreaName();


    List<Monitored> getAreaDevice(String aid);
}
