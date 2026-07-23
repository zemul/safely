package com.anbao.service;

import java.util.List;

import com.anbao.dao.AreaMapper;
import com.anbao.pojo.*;
import com.anbao.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anbao.dao.UserMapper;
import com.anbao.pojo.UserExample.Criteria;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private AreaMapper areaMapper;

	public User getUserByTel(String tel) {
		//´´½¨Ìõ¼þ²éÑ¯¶ÔÏó
		UserExample example = new UserExample();
		//ÉèÖÃ²éÑ¯Ìõ¼þ
		example.createCriteria().andTelEqualTo(tel);
		List<User> userList = userMapper.selectByExample(example);
		if(userList!=null && userList.size()>=1){
			User user = userList.get(0);
			if(user.getAid()!=null){
				Area area = areaMapper.selectByPrimaryKey(user.getAid());
				user.setAddr(area.getAddr());
			}
			return user;
		}else return null;
	}

	//×¢²áÓÃ»§
	public String updataUser(User user) {
		//2±íÊ¾¹ÜÀíÔ±
		userMapper.updateByPrimaryKeySelective(user);
		return "ok";
	}

	//²éÑ¯ËùÓÐ±£°²£¬state=2,page=µ±Ç°Ò³£¬limitÃ¿Ò³ÏÔÊ¾ÌõÊý
	public DataResult selectAllUser(Integer page, Integer limit,Integer aid,String equName) {
		//ÉèÖÃ·ÖÒ³ÐÅÏ¢
		PageHelper.startPage(page, limit);
		//Ö´ÐÐ²éÑ¯
		List<User> list = userMapper.selectAllUser(aid,equName);
		//´´½¨Ò»¸ö·µ»ØÖµ¶ÔÏó
		DataResult result = new DataResult();
		result.setData(list);
		result.setMsg("");
		result.setCode(0);
		//È¡·ÖÒ³½á¹û
		PageInfo<User> pageInfo = new PageInfo<User>(list);
		long total = pageInfo.getTotal();
		result.setTotal(total);
		return result;

	}

	
	//ÐÞ¸Ä×ÊÁÏ
	public void updatePassword(User user) {
		UserExample example = new UserExample();
		example.createCriteria().andTelEqualTo(user.getTel());
		userMapper.updateByExampleSelective(user, example);
	}

	@Override
	public void updateUser(User user) {
		userMapper.updateByPrimaryKeySelective(user);
	}

	@Override
	public void deleteUser(User user) {
		userMapper.deleteByPrimaryKey(user.getUid());
	}

	@Override
	public void insertUser(User user) {
		if (user.getPassword() != null) {
			user.setPassword(PasswordUtil.hash(user.getPassword()));
		}
		userMapper.insertSelective(user);
	}

	@Override
	public List<manySelect> getSelectUser(Integer aid,String mac) {
	    //ÏÈ»ñÈ¡ÇøÓòµÄ±£°²£¬
        //ÔÙ»ñÈ¡macÎ¬»¤µÄ±£°²£¬
        //Ñ­»·±éÀú£¬½«macµÄ±£°²£¬Ñ¡ÔñÎªselect

		//²éÑ¯ÇøÓòËùÓÐ±£°²

		List<manySelect> users = userMapper.getSelectUser(aid,mac);
		if(mac!=null){
			//²éÑ¯µ±Ç°Éè±¸¹ÜÀíÔ±
			List<manySelect> deviceUsers = userMapper.getSelectDeviceUser(aid,mac);
			for(manySelect duser :deviceUsers){
				for(manySelect user : users){
					if(duser.getValue().equals(user.getValue())){
						user.setSelected("selected");
						user.setDisabled("");
					}
				}
			}
		}
        return users;
    }

	@Override
	public void createPassword(String tel, String password) {
		UserExample example = new UserExample();
		example.createCriteria().andTelEqualTo(tel);
		List<User> users = userMapper.selectByExample(example);
		User user = users.get(0);
		user.setPassword(PasswordUtil.hash(password));
		userMapper.updateByPrimaryKeySelective(user);
	}

	@Override
	public List<message> getUserMessage(String uid) {
		return userMapper.getUserMessage(uid);
	}
}
