package com.anbao.controllor;

import com.anbao.pojo.Monitored;
import com.anbao.service.DeviceService;
import com.anbao.utils.JedisPoolUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("Start")
public class Start implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private DeviceService deviceService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("spring容易初始化完毕================================================");
        Jedis jedis = JedisPoolUtils.getJedis();
        //将所用设备的状态和阈值存入redis
        List<Monitored> list = deviceService.selectRedisInfo();
        Map<String,String> valus = new HashMap<>();
        for(Monitored device :list){
            if(StringUtils.isNotBlank(device.getState())
                    && StringUtils.isNotBlank(device.getThreshold().toString())
                    && StringUtils.isNotBlank(device.getNode().toString())
            ){
                valus.put("state",device.getState() );
                valus.put("node",device.getNode() );
                valus.put("threshold",device.getThreshold().toString() );
                jedis.hmset(device.getMac(),valus);
                valus.clear();
            }
        }
        //监听边缘端
        new Server();

        




    }
}

