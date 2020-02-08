package com.anbao.service;

import java.util.List;

import com.alibaba.dubbo.common.json.JSONObject;
import com.anbao.dao.*;
import com.anbao.pojo.*;
import com.anbao.pojo.Exception;
import com.anbao.utils.JedisPoolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import  com.anbao.rabbitmq.sendFanoutGb;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.anbao.utils.JedisPoolUtils;
import redis.clients.jedis.Jedis;

@Service
public class DeviceServiceImpl implements DeviceService {
	@Autowired
	private MonitoredMapper monitoredMapper;
	@Autowired
	private UserMacMapper userMacMapper;
	@Autowired
	private FlowdataMapper flowdataMapper;
	@Autowired
	private ExceptionMapper exceptionMapper;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private  AreaMapper areaMapper;


	Jedis jedis = JedisPoolUtils.getJedis();

	public DataResult selectDiviceList(Integer page, Integer limit,String key) {/**
		//设置分页信息
		PageHelper.startPage(page, limit);
		//执行查询
		if(key!=null){

		}
		/**
		List<deviceResult> selectAllDevice = monitoredMapper.selectAllDevice();
		for(deviceResult divice : selectAllDevice){
			//获取设备mac
			String mac = divice.getMac();
			//补充mac的人流量
			 divice.setPeopleNum(JedisPoolUtils.getJedis().get(mac));
			
		}
		//创建一个返回值对象
 */
		DataResult result = new DataResult();
		/**result.setData(selectAllDevice);
		result.setMsg("");
		result.setCode(0);
		//取分页结果
		PageInfo<deviceResult> pageInfo = new PageInfo<deviceResult>(selectAllDevice);
		long total = pageInfo.getTotal();
		result.setTotal(total);
 */
		return result;
	}


	@Override
	public void insertDevice(Monitored device,String equmanager) {
		//判断该设备id是否添加?
		device.setNum(0);
		monitoredMapper.updateByPrimaryKeySelective(device);
		jedis.hset(device.getMac(),"node",device.getNode());
		jedis.hset(device.getMac(),"state","1");
		jedis.hset(device.getMac(),"threshold",device.getThreshold().toString());

		//根据equmanager用,拆分成多个uid
		String[] uids = equmanager.split(",");
		for(String uid :uids){
			UserMac usermac = new UserMac();
			usermac.setUid(uid);
			usermac.setMac(device.getMac());
			userMacMapper.insertSelective(usermac);
		}
		//给边缘发送绑定通知
		String message = device.getMac()+" "+device.getThreshold();
		sendFanoutGb.sendGb(message,0);
	}

	@Override
	public DataResult getDeviceList(Integer page, Integer limit, Integer aid, String equName) {
		//设置分页信息
		PageHelper.startPage(page, limit);
		MonitoredExample example =new MonitoredExample();
		MonitoredExample.Criteria criteria = example.createCriteria();
		criteria.andThresholdNotEqualTo(-1);
		if(aid != null){
			criteria.andAidEqualTo(aid);
		}
		if(equName !=null){
			criteria.andNodeLike("%"+equName+"%");
		}
		//执行查询
		List<Monitored> list = monitoredMapper.selectByExample(example);
		//创建一个返回值对象
		DataResult result = new DataResult();
		result.setData(list);
		result.setMsg("");
		result.setCode(0);
		//取分页结果
		PageInfo<Monitored> pageInfo = new PageInfo<Monitored>(list);
		long total = pageInfo.getTotal();
		result.setTotal(total);
		return result;
	}

	public void updateDevice(Monitored monitored,String equmanager,String oldthreshold) {
		//查询阈值是否修改
		if(!oldthreshold.equals(monitored)){
			//阈值修改.队列发送广播
			String message = monitored.getMac()+" "+monitored.getThreshold();
			sendFanoutGb.sendGb(message,0);
			jedis.hset(monitored.getMac(),"threshold",monitored.getThreshold().toString());
			jedis.hset(monitored.getMac(),"node",monitored.getNode());

		}
		monitoredMapper.updateByPrimaryKeySelective(monitored);
		//删除当前设备绑定姓名
		UserMacExample example = new UserMacExample();
		example.createCriteria().andMacEqualTo(monitored.getMac());
		userMacMapper.deleteByExample(example);
		//添加绑定
		String[] uids = equmanager.split(",");
		for(String uid :uids){
			UserMac usermac = new UserMac();
			usermac.setUid(uid);
			usermac.setMac(monitored.getMac());
			userMacMapper.insertSelective(usermac);
		}

	}

	@Override
	public void deleteDevice(Monitored device) {
		jedis.del(device.getMac());
		monitoredMapper.deleteByPrimaryKey(device.getMac());
		UserMacExample example = new UserMacExample();
		example.createCriteria().andMacEqualTo(device.getMac());
		userMacMapper.deleteByExample(example);
	}



	@Override
	public void deviceout(String mac) {
		Monitored monitored = monitoredMapper.selectByPrimaryKey(mac);
		monitored.setState("2");
		monitoredMapper.updateByPrimaryKeySelective(monitored);
	}

    @Override
    public void addequipmen(String mac, String node, Integer aid) {
		Monitored m = new Monitored();
		m.setMac(mac);
		m.setNode(node);
		m.setAid(aid);
		m.setThreshold(-1);
		m.setState("0");
		monitoredMapper.insertSelective(m);
		jedis.hset(mac,"state","0");
		jedis.hset(mac,"node",node);
		jedis.hset(mac,"threshold","-1");
    }




	@Override
	public void insertflowdata(Flowdata flow) {
		flowdataMapper.insertSelective(flow);
	}

	@Override
	public DataResult getpeopleException(Integer page, Integer limit, String day1, String day2, String aid) {
		//设置分页信息
		PageHelper.startPage(page, limit);
		//执行查询
		List<Exception> list = exceptionMapper.selectKeyByExample(day1,day2,aid);
		//创建一个返回值对象
		DataResult result = new DataResult();
		result.setData(list);
		result.setMsg("");
		result.setCode(0);
		//取分页结果
		PageInfo<Exception> pageInfo = new PageInfo<Exception>(list);
		long total = pageInfo.getTotal();
		result.setTotal(total);
		return result;

	}

	@Override
	public Integer deviceLogin(String id, String flag) {
		System.out.println(flag+" " +id);
		if(id=="" || id ==null){
			return -1;
		}
		Monitored monitored = monitoredMapper.selectByPrimaryKey(id);
		if(monitored == null){
			//addequipmen(id);
			//没有该设备
			return -1;
		}
		monitored.setState(flag);
		monitoredMapper.updateByPrimaryKeySelective(monitored);
		JSONObject object = new JSONObject();

		return monitored.getThreshold();
	}

	@Override
	public List<Monitored> selectRedisInfo() {
		MonitoredExample example=new MonitoredExample();
		return monitoredMapper.selectByExample(example);
	}

	@Override
	public List<User> getDeviceWithUserTel(String id) {
		return  monitoredMapper.getDeviceWithUserTel(id);
	}

	@Override
	public void sendWebMessage(String uid, String time, String message) {
		userMapper.sendWebMessage(uid,message,time);
	}

	@Override
	public List<Monitored> getSelectDevice(Integer aid) {
		MonitoredExample example = new MonitoredExample();
		example.createCriteria().andThresholdEqualTo(-1);
		return monitoredMapper.selectByExample(example);
	}

	@Override
	//根据mac获取设备信息
    public Monitored selectmacDeviceInfo(String mac) {
		Monitored monitored = monitoredMapper.selectByPrimaryKey(mac);
		return monitored;


	}

	@Override
	public String getLastTimeFlowNum(String mac) {
		FlowdataExample example = new FlowdataExample();
		return flowdataMapper.getLastTimeFlowNum(mac);
	}


	//设备当月预警数
	@Override
	public Integer getDeviceExceptionNumMonth(String mac) {
		return exceptionMapper.getDeviceExceptionNumMonth(mac);
	}

	@Override
	//查询设备管理员tel，name
	public List<User> getSelectManagerUser(String mac) {
		return userMapper.getSelectManagerUser(mac);
	}

	@Override
	public Integer getAreaSum() {
		AreaExample example = new AreaExample();
		return areaMapper.countByExample(example);
	}

	@Override
	public Integer getNodeSum(Integer aid) {
		MonitoredExample example = new MonitoredExample();
		example.createCriteria().andAidEqualTo(aid);
		return monitoredMapper.countByExample(example);
	}

	@Override
	public Integer getPeopleSum(Integer aid) {
		if(aid!=null){
			UserExample example = new UserExample();
			example.createCriteria().andAidEqualTo(aid);
			return userMapper.countByExample(example);
		}else{
			UserExample example = new UserExample();
			return userMapper.countByExample(example);
		}
	}

	@Override
	public Integer getMonthExceptionSum(Integer aid) {
		if(aid!=null){
			return exceptionMapper.countExceptionSumByAid(aid);
		}else{
			ExceptionExample example = new ExceptionExample();
			return exceptionMapper.countByExample(example);
		}
	}


}
