package com.anbao.pojo;

import java.util.List;

public class gailanResult {
    private Object managerList;
    private Monitored monitored;
    private Integer deviceExceptionNumMonth;
    private String peopleNum;


    public gailanResult(Object managerList, Monitored monitored, Integer deviceExceptionNumMonth, String peopleNum) {
        this.managerList = managerList;
        this.monitored = monitored;
        this.deviceExceptionNumMonth = deviceExceptionNumMonth;
        this.peopleNum = peopleNum;
    }

    public Object getManagerList() {
        return managerList;
    }

    public void setManagerList(Object managerList) {
        this.managerList = managerList;
    }

    public Monitored getMonitored() {
        return monitored;
    }

    public void setMonitored(Monitored monitored) {
        this.monitored = monitored;
    }

    public Integer getDeviceExceptionNumMonth() {
        return deviceExceptionNumMonth;
    }

    public void setDeviceExceptionNumMonth(Integer deviceExceptionNumMonth) {
        this.deviceExceptionNumMonth = deviceExceptionNumMonth;
    }

    public String getPeopleNum() {
        return peopleNum;
    }

    public void setPeopleNum(String peopleNum) {
        this.peopleNum = peopleNum;
    }
}
