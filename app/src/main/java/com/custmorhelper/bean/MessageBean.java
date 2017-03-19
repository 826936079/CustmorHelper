package com.custmorhelper.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/2/17.
 *
 *
 */
public class MessageBean implements Parcelable {

    private int type;  //消息类型 : qq、微信
    private String content; //接收内容
    private String sender;  //发送者

    public MessageBean() {
    }

    public MessageBean(Parcel parcel) {
        type = parcel.readInt();
        content = parcel.readString();
        sender = parcel.readString();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(type);
        parcel.writeString(content);
        parcel.writeString(sender);
    }

    public static final Parcelable.Creator<MessageBean> CREATOR = new Parcelable.Creator<MessageBean>() {
        @Override
        public MessageBean createFromParcel(Parcel parcel) {
            return new MessageBean(parcel);
        }

        @Override
        public MessageBean[] newArray(int position) {
            return new MessageBean[position];
        }
    };

    @Override
    public String toString() {
        return "MessageBean{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}
