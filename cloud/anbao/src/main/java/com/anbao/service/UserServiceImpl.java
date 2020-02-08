package com.anbao.service;

import java.util.List;

import com.anbao.dao.AreaMapper;
import com.anbao.pojo.*;
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
		//创建条件查询对象
		UserExample example = new UserExample();
		//设置查询条件
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

	//注册用户
	public String updataUser(User user) {
		//2表示管理员
		userMapper.updateByPrimaryKeySelective(user);
		return "ok";
	}

	//查询所有保安，state=2,page=当前页，limit每页显示条数
	public DataResult selectAllUser(Integer page, Integer limit,Integer aid,String equName) {
		//设置分页信息
		PageHelper.startPage(page, limit);
		//执行查询
		List<User> list = userMapper.selectAllUser(aid,equName);
		//创建一个返回值对象
		DataResult result = new DataResult();
		result.setData(list);
		result.setMsg("");
		result.setCode(0);
		//取分页结果
		PageInfo<User> pageInfo = new PageInfo<User>(list);
		long total = pageInfo.getTotal();
		result.setTotal(total);
		return result;

	}

	
	//修改资料
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
		userMapper.insertSelective(user);
	}

	@Override
	public List<manySelect> getSelectUser(Integer aid,String mac) {
	    //先获取区域的保安，
        //再获取mac维护的保安，
        //循环遍历，将mac的保安，选择为select

		//查询区域所有保安

		List<manySelect> users = userMapper.getSelectUser(aid,mac);
		if(mac!=null){
			//查询当前设备管理员
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
		user.setPassword(password);
		userMapper.updateByPrimaryKeySelective(user);
	}

	@Override
	public List<message> getUserMessage(String uid) {
		return userMapper.getUserMessage(uid);
	}
}
