package com.anbao.service;

import java.util.List;

import com.anbao.pojo.User;
import com.anbao.pojo.DataResult;
import com.anbao.pojo.manySelect;
import com.anbao.pojo.message;

public interface UserService {
	//查询保安列表

	//根据tel返回User
	public User getUserByTel(String tel);
	
	//注册用户
	public String updataUser(User user);

	//查询用户列表
	public DataResult selectAllUser(Integer page, Integer limit,Integer aid,String equName);

	//修改密码
	public void updatePassword(User user);

	void updateUser(User user);

    void deleteUser(User user);

    void insertUser(User user);

    List<manySelect> getSelectUser(Integer aid,String mac);

    void createPassword(String tel, String password);

    List<message> getUserMessage(String uid);
}
