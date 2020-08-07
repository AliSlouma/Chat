package com.example.chat;

public class ChatInstance {
    private String mReceiver;
    private String mLastMessage;
    private String Time;

    public ChatInstance(String receiver, String lastMessage, String time) {
        mReceiver = receiver;
        mLastMessage = lastMessage;
        Time = time;
    }

    public String getReceiver() {
        return mReceiver;
    }

    public void setReceiver(String receiver) {
        mReceiver = receiver;
    }

    public String getLastMessage() {
        return mLastMessage;
    }

    public void setLastMessage(String lastMessage) {
        mLastMessage = lastMessage;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }
}
