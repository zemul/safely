package com.anbao.dao;

import com.anbao.pojo.User;
import com.anbao.pojo.UserExample;
import java.util.List;

import com.anbao.pojo.manySelect;
import com.anbao.pojo.message;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    List<User> selectAllUser(@Param("aid") Integer aid,@Param("equName") String equName);

    void updateArea( String name, String tel,Integer aid);
    int countByExample(UserExample example);

    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(String uid);

    int insert(User record);

    int insertSelective(User record);

    List<User> selectByExample(UserExample example);

    User selectByPrimaryKey(String uid);

    int updateByExampleSelective(@Param("record") User record, @Param("example") UserExample example);

    int updateByExample(@Param("record") User record, @Param("example") UserExample example);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    List<manySelect> getSelectUser(@Param("aid") Integer aid, @Param("mac") String mac);
    List<manySelect> getSelectDeviceUser(@Param("aid")  Integer aid,String mac);

    List<User> getSelectManagerUser(@Param("mac")String mac);

    void sendWebMessage(String uid, String message, String time);

    List<message> getUserMessage(String uid);
}