package com.interview.ai.service.rtc.component.shengwang.model;

public class Puller {
    private String channelId;
    private Integer uid;
    private String token;
    private String appid;
    private int channelSource;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public int getChannelSource() {
        return channelSource;
    }

    public void setChannelSource(int channelSource) {
        this.channelSource = channelSource;
    }
}
