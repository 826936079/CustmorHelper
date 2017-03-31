package com.custmorhelper.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/2/17.
 *
 *
 */
public class MessageBean implements Parcelable {

    private long id;
    private String fromName; //发送人
    private String receiveContent; //接收内容
    private long receiveTime; //接收时间
    private String sendContent;  //回复消息
    private long sendTime;  //回复时间
    private int messageType;  //消息类型 : QQ, 微信

    public static final int MSG_QQ = 0;
    public static final int MSG_WECHAT = 1;

    public MessageBean() {
    }

    public MessageBean(long id, String fromName, String receiveContent, long receiveTime, String sendContent, long sendTime, int messageType) {
        this.id = id;
        this.fromName = fromName;
        this.receiveContent = receiveContent;
        this.receiveTime = receiveTime;
        this.sendContent = sendContent;
        this.sendTime = sendTime;
        this.messageType = messageType;
    }

    protected MessageBean(Parcel in) {
        id = in.readLong();
        fromName = in.readString();
        receiveContent = in.readString();
        receiveTime = in.readLong();
        sendContent = in.readString();
        sendTime = in.readLong();
        messageType = in.readInt();
    }

    public static final Creator<MessageBean> CREATOR = new Creator<MessageBean>() {
        @Override
        public MessageBean createFromParcel(Parcel in) {
            return new MessageBean(in);
        }

        @Override
        public MessageBean[] newArray(int size) {
            return new MessageBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(fromName);
        parcel.writeString(receiveContent);
        parcel.writeLong(receiveTime);
        parcel.writeString(sendContent);
        parcel.writeLong(sendTime);
        parcel.writeInt(messageType);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getReceiveContent() {
        return receiveContent;
    }

    public void setReceiveContent(String receiveContent) {
        this.receiveContent = receiveContent;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getSendContent() {
        return sendContent;
    }

    public void setSendContent(String sendContent) {
        this.sendContent = sendContent;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
}
