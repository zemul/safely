package com.anbao.service;

import com.anbao.dao.AreaMapper;
import com.anbao.dao.MonitoredMapper;
import com.anbao.dao.UserMapper;
import com.anbao.pojo.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AreaServiceImpl implements AreaService {

    @Autowired
    private AreaMapper areaMapper;
    @Autowired
    public UserMapper userMapper;
    @Autowired
    public MonitoredMapper monitoredMapper;

    /**
     * 查询所有边缘端
     * @param page
     * @param limit
     * @return
     */
    @Override
    public DataResult selectAllUser(Integer page, Integer limit,String equName) {
        //设置分页信息
        PageHelper.startPage(page, limit);
        //执行查询
        Area area = new Area();
        area.setAddr(equName);
        List<Area> areaList = areaMapper.getAllArea(area);
        //创建一个返回值对象
        DataResult result = new DataResult();
        result.setData(areaList);
        result.setMsg("");
        result.setCode(0);
        //取分页结果
        PageInfo<Area> pageInfo = new PageInfo<Area>(areaList);
        long total = pageInfo.getTotal();
        result.setTotal(total);
        return result;


    }

    @Override
    public void updateArea(Integer aid, String name, String tel) {
        userMapper.updateArea(name,tel,aid);
    }

    @Override
    public void deleteArea(Area area) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andTelEqualTo(area.getTel());
        userMapper.deleteByExample(userExample);
        areaMapper.deleteByPrimaryKey(area.getAid());
    }

    @Override
    public void addArea(Area area) {
        areaMapper.insert(area);
        AreaExample example = new AreaExample();
        example.createCriteria().andAddrEqualTo(area.getAddr());
        Area area1 = areaMapper.selectByExample(example).get(0);
        User user = new User();
        user.setName(area.getName());
        user.setTel(area.getTel());
        user.setState(2);
        user.setAid(area1.getAid());
        user.setUid(UUID.randomUUID().toString().replace("-", ""));
        userMapper.insertSelective(user);

    }

    @Override
    public List<Area> getAreaName() {
        AreaExample example = new AreaExample();
        List<Area> areas = areaMapper.selectByExample(example);
        return areas;
    }

    @Override
    public List<Monitored> getAreaDevice(String aid) {
        MonitoredExample example = new MonitoredExample();
        example.createCriteria().andAidEqualTo(Integer.parseInt(aid));
        return monitoredMapper.selectByExample(example);
    }
}
