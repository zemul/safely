package com.anbao.service;

import com.anbao.pojo.*;

import java.util.List;

public interface DeviceService {
	void insertDevice(Monitored device,String equmanager);
	DataResult getDeviceList(Integer page,Integer limit,Integer aid,String equName);
	void updateDevice(Monitored device,String equmanager,String oldthreshold);
	void deleteDevice(Monitored device);

	void deviceout(String id);

	void addequipmen(String mac, String node, Integer aid);




	void insertflowdata(Flowdata flow);


	Monitored selectmacDeviceInfo(String mac);

	String getLastTimeFlowNum(String mac);

	Integer getDeviceExceptionNumMonth(String mac);


	List<User> getSelectManagerUser(String mac);

    Integer getAreaSum();
	Integer getNodeSum(Integer aid);

	Integer getPeopleSum(Integer aid);

	Integer getMonthExceptionSum(Integer aid);

    DataResult getpeopleException(Integer page, Integer limit, String day1, String day2, String aid);

    Integer deviceLogin(String id, String flag);

	List<Monitored> selectRedisInfo();

	List<User> getDeviceWithUserTel(String id);

	void sendWebMessage(String uid, String format, String message);

	List<Monitored> getSelectDevice(Integer aid);
}
