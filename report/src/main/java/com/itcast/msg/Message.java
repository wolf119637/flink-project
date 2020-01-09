package com.itcast.msg;

/**
 * Created by angel
 */
public class Message {
    //业务逻辑
    private int count ; //消息的次数
    private Long timeStamp ; //消息的时间
    private String message ; //消息体

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "count=" + count +
                ", timeStamp=" + timeStamp +
                ", message='" + message + '\'' +
                '}';
    }
}
