package com.anbao.pojo;

public class Exception {
    private String eid;

    private String inittime;

    private String continuetime;

    private String videourl;

    private String mac;

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid == null ? null : eid.trim();
    }

    public String getInittime() {
        return inittime;
    }

    public void setInittime(String inittime) {
        this.inittime = inittime == null ? null : inittime.trim();
    }

    public String getContinuetime() {
        return continuetime;
    }

    public void setContinuetime(String continuetime) {
        this.continuetime = continuetime == null ? null : continuetime.trim();
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl == null ? null : videourl.trim();
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac == null ? null : mac.trim();
    }

    public String node;

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }
    public Integer aid;

    public Integer getAid() {
        return aid;
    }

    public void setAid(Integer aid) {
        this.aid = aid;
    }
}