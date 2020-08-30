package com.example.chat;

import androidx.annotation.NonNull;

public class ChatInstance {
    private String mReceiver;
    private String mReceiverUID;
    private String mLastMessage;
    private String Time;
    private String friendPhoto;
    private boolean seen;

    public ChatInstance(String mReceiver, String mReceiverUID, String mLastMessage, String time, String friendPhoto) {
        this.mReceiver = mReceiver;
        this.mReceiverUID = mReceiverUID;
        this.mLastMessage = mLastMessage;
        Time = time;
        this.friendPhoto = friendPhoto;
    }

    public ChatInstance() {
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getFriendPhoto() {
        return friendPhoto;
    }

    public void setFriendPhoto(String friendPhoto) {
        this.friendPhoto = friendPhoto;
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
