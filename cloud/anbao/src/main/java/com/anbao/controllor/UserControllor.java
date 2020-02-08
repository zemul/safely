package com.anbao.controllor;

import java.io.UnsupportedEncodingException;
import java.lang.Exception;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import com.anbao.pojo.*;
import com.anbao.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.alibaba.dubbo.common.json.JSONObject;
import com.anbao.service.UserService;
import com.anbao.utils.MoblieMessageUtil;
import com.anbao.utils.Result;

import com.fasterxml.jackson.databind.util.JSONPObject;

@Controller
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping(method = RequestMethod.POST)
public class UserControllor {
	@Autowired
	private UserService userService;
	@Autowired
	private DeviceService deviceService;


	//获取保安通知
	@RequestMapping(value = "/user/message" )
	@ResponseBody
	public List<message> getUserMessage(HttpSession httpSession){
		User user = (User) httpSession.getAttribute("user");
		System.out.println(user.toString());
		return userService.getUserMessage(user.getUid());
	}



		/**
         * 下拉列表获取保安
         */
	@RequestMapping(value = "/user/getSelectUser",method = RequestMethod.GET)
	@ResponseBody
	public DataResult getSelectUser(Integer aid,String mac){
		List<manySelect> list = userService.getSelectUser(aid,mac);
		DataResult result = new DataResult();
		result.setCode(0);
		result.setMsg("success");
		result.setData(list);
		return result;

	}


	/**
	 * 添加保安
	 * @return
	 */
	@RequestMapping("/user/insertUser")
	@ResponseBody
	public void insertUser(User user){
		user.setState(3);
		user.setUid(UUID.randomUUID().toString().replace("-", ""));
		userService.insertUser(user);
	}



	/**
	 * 查询所有保安
	 * @return
	 */
	@RequestMapping("/user/getUserList")
	@ResponseBody
	public DataResult getAllUser(Integer page,Integer limit,Integer aid,String equName){
		DataResult result = userService.selectAllUser(page,limit,aid,equName);
		return result;
	}

	/**
	 * 修改保安信息
	 */
	@RequestMapping("/usesr/updateUser")
	@ResponseBody
	public Result updateUser(User user){
		userService.updateUser(user);
		return Result.ok();
	}

	/**
	 * 删除保安
	 */
	@RequestMapping("/user/deleteUser")
	@ResponseBody
	public Result deleteArea(User user){
		userService.deleteUser(user);
		return Result.ok();
	}
	
	/**
	 * 注册
	 * tel，姓名，密码，标记，mac
	 */
	@RequestMapping("/user/register")
	@ResponseBody
	public Result register(User user,String identcode,HttpSession session) throws UnsupportedEncodingException {
		//获取请求的验证码
		String code = (String)session.getAttribute("phoneCode");
		Result result = new Result();
		//判断验证是否一致
		if(code.equals(identcode)){
			//验证码正确
			//查询手机号码是否存在
			User dbuser = userService.getUserByTel(user.getTel());
			if(dbuser !=null && dbuser.getUid() != null){
				user.setUid(dbuser.getUid());
				userService.updateUser(user);
				result.setStatus(200);
				session.setAttribute("user", user);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String message = new String("道路千万条，安全第一条，监控不及时，群众两行泪");
				message = new String(message.getBytes("GBK"),"utf-8");
				deviceService.sendWebMessage(user.getUid(),sdf.format(new Date()),message);
			}else{
				//联系管理员注册
				result.setStatus(203);
			}


		}else{
			//验证码错误
			result.setStatus(204);
		}
		return result;
	}
	
	
	/**
	 * 登陆
	 */
	@RequestMapping(value="/user/login",method = RequestMethod.POST)
	@ResponseBody
	public Result login(String tel,String password,HttpSession httpSession){
		httpSession.removeAttribute("user");
		System.out.println(tel+" "+password);
		Result result = new Result();
		//用户输入的用户名和密码
	    User user = userService.getUserByTel(tel);
	    if(user != null){
	    	//手机号码存在
	    	if(user.getTel().equals(tel) && user.getPassword().equals(password)){
	    		//密码正确
				if(user.getAid()!=null){
					result.setMsg(user.getAid().toString());
				}
	    		result.setStatus(200);
	    		//用户信息在session域
	    		httpSession.setAttribute("user", user);
	    	}else if(user.getPassword()==null){
	    		//联系管理员进行注册
				result.setStatus(203);
			}
	    	else{
	    		//密码错误
	    		result.setStatus(201);
	    	}
	    	
	    }else{
	    	//手机号码不存在
	    	result.setStatus(202);
	    }
	    return result;


	}


	//创建密码
	@RequestMapping("/user/createPassword")
	@ResponseBody
	public void createPassword(String tel,String password){
		userService.createPassword(tel,password);

	}

	
	/**
	 * 修改密码
	 */
	@RequestMapping("/user/resetpassword")
	@ResponseBody
	public Result resetpassword(String tel,String password1,String password2){
		Result result = new Result();
		//用户输入的用户名和密码
	    User user = userService.getUserByTel(tel);
	    if(user != null){
	    	//手机号码存在
	    	if(user.getPassword().equals(password1)){
	    		//密码正确
	    		user.setPassword(password2);
	    		userService.updatePassword(user);
	    		result.setStatus(200);
	    		//用户信息在session域
	    	}else{
	    		//原密码错误
	    		result.setStatus(203);
	    	}
	    	
	    }
	    return result;
	}
	
	/**
	 * 更改用户信息
	 * @param tel
	 * @param email
	 * @param name
	 */
	@RequestMapping("/user/resetuserinfo")
	@ResponseBody
	public void resetuserinfo(String tel,String email,String name,HttpSession httpSession){
	    User user = userService.getUserByTel(tel);
	    user.setEmail(email);
	    user.setName(name);
	    userService.updatePassword(user);
	    httpSession.removeAttribute("user");
	    httpSession.setAttribute("user", user);
	}


	//拉去首页用户信息

	@RequestMapping(value="/user/getIndexInfo",method = RequestMethod.POST)
	@ResponseBody
	public User resetuserinfo(HttpSession httpSession){
		User user = (User) httpSession.getAttribute("user");
		return user;

	}
	
	
	
	/**
	 * 获取验证码
	 * @return
	 */
	@RequestMapping("/user/getPhoneCode")
	public void getPhoneCode(String tel,HttpSession session){
		System.out.println(tel);
		String templateCode = "SMS_142389828";
		Integer icode = (int)(Math.random()*99999)+100;  //每次调用生成一位四位数的随机数
		String code  = Integer.toString(icode);
		String templateParam = "{\"code\":\""+code+"\"}";
		System.out.println(code);
		//发送验证码
		MoblieMessageUtil.sendmsg(tel, templateParam, templateCode);
		//验证码存储在session域
		session.setAttribute("phoneCode",code);
	}
	
	/**
	 * 退出
	 */
	@RequestMapping("/user/goOut")
	public String goOut(HttpSession session){
		System.out.println("aaa");
		session.removeAttribute("user");
		return "redirect:../login.html";
	}






	
	
	
	
}
