package com.anbao.dao;

import com.anbao.pojo.UserMac;
import com.anbao.pojo.UserMacExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserMacMapper {
    int countByExample(UserMacExample example);

    int deleteByExample(UserMacExample example);

    int insert(UserMac record);

    int insertSelective(UserMac record);

    List<UserMac> selectByExample(UserMacExample example);

    int updateByExampleSelective(@Param("record") UserMac record, @Param("example") UserMacExample example);

    int updateByExample(@Param("record") UserMac record, @Param("example") UserMacExample example);
}