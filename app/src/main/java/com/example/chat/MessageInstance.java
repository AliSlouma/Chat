package com.example.chat;

public class MessageInstance {
    private String mMessage;
    private String mTime;
    private String mDate;
    private boolean mSeen;
    private String mSender;

    public MessageInstance(String message, String time,String date, boolean seen, String sender) {
        mMessage = message;
        mTime = time;
        mSeen = seen;
        mSender = sender;
        mDate = date;
    }

    public MessageInstance() {
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public boolean isSeen() {
        return mSeen;
    }

    public void setSeen(boolean seen) {
        mSeen = seen;
    }

    public String getSender() {
        return mSender;
    }

    public void setSender(String sender) {
        this.mSender = sender;
    }
    public String getDate() {
        return mDate;
    }

    public void setDate(String mDate) {
        this.mDate = mDate;
    }
}
