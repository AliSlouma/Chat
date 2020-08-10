package com.example.chat;

import androidx.annotation.NonNull;

public class ChatInstance {
    private String mReceiver;
    private String mReceiverUID;
    private String mLastMessage;
    private String Time;

    public ChatInstance(String receiver, String receiverUID, String lastMessage, String time) {
        mReceiver = receiver;
        mReceiverUID = receiverUID;
        mLastMessage = lastMessage;
        Time = time;
    }

    @NonNull
    @Override
    public String toString() {
       return mReceiver + "\n" + mLastMessage;
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

    public String getReceiverUID() {
        return mReceiverUID;
    }

    public void setReceiverUID(String receiverUID) {
        mReceiverUID = receiverUID;
    }
}
