package com.anbao.dao;

import com.anbao.pojo.Exception;
import com.anbao.pojo.ExceptionExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExceptionMapper {
    int countByExample(ExceptionExample example);

    int deleteByExample(ExceptionExample example);

    int deleteByPrimaryKey(String eid);

    int insert(Exception record);

    int insertSelective(Exception record);

    List<Exception> selectByExample(ExceptionExample example);
    List<Exception> selectKeyByExample( @Param("day1")String day1, @Param("day2")String day2, @Param("aid")String aid);


    Exception selectByPrimaryKey(String eid);

    int updateByExampleSelective(@Param("record") Exception record, @Param("example") ExceptionExample example);

    int updateByExample(@Param("record") Exception record, @Param("example") ExceptionExample example);

    int updateByPrimaryKeySelective(Exception record);

    int updateByPrimaryKey(Exception record);

    Integer getDeviceExceptionNumMonth(@Param("mac") String mac);

    Integer countExceptionSumByAid(@Param("aid")Integer aid);

}