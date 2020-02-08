package com.anbao.controllor;

import com.anbao.pojo.Area;
import com.anbao.pojo.DataResult;
import com.anbao.pojo.Monitored;
import com.anbao.pojo.User;
import com.anbao.service.AreaService;
import com.anbao.utils.JedisPoolUtils;
import com.anbao.utils.Result;
import org.mortbay.util.ajax.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@Controller
@RequestMapping(method = RequestMethod.POST)

public class AreaControllor {

    @Autowired
    private AreaService areaService;

    /**
     * 查询所有边缘addr字段，aid，addr
     */
    @RequestMapping("/area/getAreaName")
    @ResponseBody
    public  Result getAllArea(HttpSession sesion){
        List<Area> list = areaService.getAreaName();
        User user = (User)sesion.getAttribute("user");
        Result result = new Result(list,user);
        return result;
    }

    //查询区域的设备
    @RequestMapping("/area/getAreaDevice")
    @ResponseBody
    public  Result getAreaDevice(String aid){
        List<Monitored> list = areaService.getAreaDevice(aid);
        Result result = new Result(list,list);
        return result;
    }


    /**
     * 获取区域列表
     * @param page
     * @param limit
     * @return
     */
    @RequestMapping("/area/getAreaList")
    @ResponseBody

    public DataResult getAllUser(Integer page, Integer limit,String equName){
        System.out.println(equName);
        DataResult result = areaService.selectAllUser(page,limit,equName);
        return result;
    }

    /**
     * 修改区域信息
     */
    @RequestMapping("/area/updateArea")
    @ResponseBody
    public Result updateArea(Integer aid,String name,String tel){
        areaService.updateArea(aid,name,tel);
        return Result.ok();
    }
    /**
     * 删除区域
     */
    @RequestMapping("/area/deleteArea")
    @ResponseBody
    public Result deleteArea(Area area){
        areaService.deleteArea(area);
        return Result.ok();
    }
    /**
     * 添加区域
     */
    @RequestMapping("/area/addArea")
    @ResponseBody
    public Result addArea(Area area){
        System.out.println(area);
        areaService.addArea(area);
        return Result.ok();
    }
}
