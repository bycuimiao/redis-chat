package com.bycuimiao.demo;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = -900197651222500071L;
    private Integer id ;
    //时间戳
    private Long time;
    //用户code
    private String userCode;
    //聊天室code
    private String chatCode;
    //内容
    private String msg;

    public Message() {
    }

    public Message(Integer id, Long time, String userCode, String msg) {
        this.id = id;
        this.time = time;
        this.userCode = userCode;
        this.msg = msg;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getChatCode() {
        return chatCode;
    }

    public void setChatCode(String chatCode) {
        this.chatCode = chatCode;
    }
}
