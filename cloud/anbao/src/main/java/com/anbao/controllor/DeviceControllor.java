package com.anbao.controllor;

import java.io.*;
import java.lang.Exception;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.anbao.dao.UserMacMapper;
import com.anbao.pojo.*;
import com.anbao.rabbitmq.MessageConsumer;
import com.anbao.rabbitmq.sendFanoutGb;
import com.anbao.service.UserService;
import com.anbao.service.UserServiceImpl;
import com.anbao.utils.JedisPoolUtils;
import com.anbao.utils.MoblieMessageUtil;
import com.anbao.utils.Result;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.anbao.service.DeviceService;
import redis.clients.jedis.Jedis;

@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping(method = RequestMethod.POST)
@Controller
public class DeviceControllor {

	@Autowired
	private DeviceService deviceService;


	/**
	 * 下拉列表未绑定
	 */
	@RequestMapping(value = "/device/getSelectDevice")
	@ResponseBody
	public List<Monitored> getSelectDevice(Integer aid){
		List<Monitored> list = deviceService.getSelectDevice(aid);
		return list;

	}






	/**
	 * 异常记录
	 * @return
	 */
	@RequestMapping("/exception")
	@ResponseBody
	public DataResult peopleException(Integer page,Integer limit,String day1,String day2,String aid){
		return deviceService.getpeopleException(page,limit,day1,day2,aid);
	}

	/**
	 * 设备通信
	 * @return
	 */
	@RequestMapping("/normalup")
	@ResponseBody
	public void insertflowdata(Flowdata flow){
		deviceService.insertflowdata(flow);
	}





	/**
	 *
	 *
	 * flag=login&idAAAAAA
	 * return  eg“30”
	 */
	@RequestMapping(value = "/upState",method = RequestMethod.GET)//----------------------------------------
	@ResponseBody
	public Integer  deviceLogin(String id,String flag) throws UnsupportedEncodingException {
		//0离线，1上线，2超过阈值
		//set    status=1
		//return    threshold
		if(flag.equals("2")){
			Jedis jedis = JedisPoolUtils.getJedis();
			String node = jedis.hget(id, "node");
			String threshold = jedis.hget(id, "threshold");
			jedis.hset(id,"state",flag);
			List<User> telList =deviceService.getDeviceWithUserTel(id);
			//发送网站通知
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			//给改设备绑定user发送短信
			String templateCode = "SMS_167397079";
			String templateParam = "{\"node_mac\":\""+node+"\",\"node_sum\":\""+threshold+"\"}";
			String s1 = new String("人流量达".getBytes("gbk"),"utf-8");
			String s2 = new String("人，请及时处理！".getBytes("gbk"),"utf-8");
			String message= node+s1+threshold +s2;

			if(telList!=null &&telList.size()>=1){
				for(User user : telList){
					deviceService.sendWebMessage(user.getUid(),sdf.format(new Date()),message);
					MoblieMessageUtil.sendmsg(user.getTel(),templateParam,templateCode);
				}


			}


		}
		return deviceService.deviceLogin(id,flag);
	}



	/**
	 * 绑定设备
	 * @return
	 */
	@RequestMapping("/device/Device")
	@ResponseBody
	public void insertDevice(Monitored device,String equmanager){
		//写入字段到设备表
		device.setState("1");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		device.setCreatetime(sdf.format(date));
		deviceService.insertDevice(device,equmanager);
	}


	/**
	 * 边缘端添加设备
	 * @return
	 */
	@RequestMapping("/addequipment")
	@ResponseBody
	public void addequipmen(@RequestParam("equ_uuid") String mac, @RequestParam("equ_othername") String node,@RequestParam("equ_aid") Integer aid){
		//写入字段到设备表

		System.out.println(mac+" " +node + " " +aid);
		deviceService.addequipmen(mac,node,aid);
	}

	/**
	 * 查询设备
	 * @return
	 */
	@RequestMapping("/device/getDeviceList")
	@ResponseBody
	public DataResult getDeviceList(Integer page,Integer limit,Integer aid,String equName){
		DataResult result = deviceService.getDeviceList(page,limit,aid,equName);
		return result;
	}

	/**
	 * 修改设备
	 */
	@RequestMapping("/device/updateDevice")
	@ResponseBody
	public Result updateDevice(Monitored device,String equmanager,String oldthreshold){
		deviceService.updateDevice(device,equmanager,oldthreshold);
		return Result.ok();
	}

	/**
	 * 删除设备
	 */
	@RequestMapping("/device/deleteDevice")
	@ResponseBody
	public Result deleteDevice(Monitored device){
		deviceService.deleteDevice(device);
		return Result.ok();
	}


	//根据mac获取设备信息
	@RequestMapping("/device/selectDeviceInfo")
	@ResponseBody
	public gailanResult selectmacDeviceInfo(String mac){
		//设备信息√
		// 安保人员，人流量√，
		// 本月预警数，设备当前状态√，中心拥挤度
		//设备信息，提取当前状态，
		//0离线，1上线，2超过阈值
		Monitored  monitored =deviceService.selectmacDeviceInfo(mac);
		//判断设备状态，
			//异常，从redis获取实时人流量
			//正常，从数据库获取最近10分钟记录
		//当前人流量
		String peopleNum =null;
		if(monitored.getState().equals("1") || monitored.getState().equals("0") ){
			//正常
			peopleNum=deviceService.getLastTimeFlowNum(mac);//------redis
			if(peopleNum==null || peopleNum==""){
				peopleNum="0";
			}
		}else if(monitored.getState().equals("2")){
			//异常
			//由MessageConsumer处理，根据mac获取session，写入数据

		}
		//获取本月预警数
		Integer deviceExceptionNumMonth = deviceService.getDeviceExceptionNumMonth(mac);
		//获取中心拥挤度
		//获取管理人员
		List<User> userList = deviceService.getSelectManagerUser(mac);
		gailanResult result = new gailanResult(userList,monitored,deviceExceptionNumMonth,peopleNum);
		return result;

	}

	//首页拉取，区域数，设备数，保安数，预警数
	//根据mac获取设备信息
	@RequestMapping("/device/indexInfo")
	@ResponseBody
	public indexPojo indexInfo(Integer aid){
		indexPojo pojo = new indexPojo();
		//保安数，预警数
		pojo.setPeopleSum(deviceService.getPeopleSum(aid));
		pojo.setMonthExceptionSum(deviceService.getMonthExceptionSum(aid));
		if(aid!=null && aid !=0){
			pojo.setNodeSum(deviceService.getNodeSum(aid));
		}
		else{
			pojo.setAreaSum(deviceService.getAreaSum());

		}
		return pojo;
	}


	//添加设备请求监控图片
	@RequestMapping("/device/askImage")
	@ResponseBody
	public void askImage(String mac){
		System.out.println(mac);
		sendFanoutGb.sendGb(mac,1);
	}

	//接收上传图片
	@RequestMapping("/device/getImage")
	@ResponseBody
	public void getImage(HttpServletRequest request, HttpServletResponse response)throws IOException {
		try {
			//1、创建磁盘文件项工厂
			//作用：设置缓存文件的大小  设置临时文件存储的位置
			ServletContext servletContext = request.getSession().getServletContext();
				DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(1024*1024*2);
			//2、创建文件上传的核心类
			ServletFileUpload upload = new ServletFileUpload(factory);
			//设置上传文件的名称的编码
			upload.setHeaderEncoding("UTF-8");
			//ServletFileUpload的API
			boolean multipartContent = upload.isMultipartContent(request);//判断表单是否是文件上传的表单
			if(multipartContent){
				//是文件上传的表单
				//***解析request获得文件项集合
				List<FileItem> parseRequest = upload.parseRequest(request);
				if(parseRequest!=null){
					for(FileItem item : parseRequest){
						//判断是不是一个普通表单项
						boolean formField = item.isFormField();
						if(!formField){
							//文件上传项
							//文件的名
							String fileName = item.getName();
							System.out.println(fileName);
							//获得上传文件的内容
							InputStream in = item.getInputStream();
							OutputStream out = new FileOutputStream(new File(servletContext.getRealPath("images/image/"+fileName)));
							IOUtils.copy(in,out);
							in.close();
							out.close();
							//删除临时文件
							item.delete();

						}
					}
				}

			}else{
				//不是文件上传表单
				//使用原始的表单数据的获得方式 request.getParameter();
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
	}

	
}
