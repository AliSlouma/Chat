package com.example.chat;

public class MessageInstance {
    private String mMessage;
    private String mTime;
    private String mDate;
    private boolean mSeen;
    private String mSender;



    private String mSenderID;

    public MessageInstance(String mMessage, String mTime, String mDate, boolean mSeen, String mSender, String mSenderID) {
        this.mMessage = mMessage;
        this.mTime = mTime;
        this.mDate = mDate;
        this.mSeen = mSeen;
        this.mSender = mSender;
        this.mSenderID = mSenderID;
    }



    public MessageInstance() {
    }
    public String getmSenderID() {
        return mSenderID;
    }

    public void setmSenderID(String mSenderID) {
        this.mSenderID = mSenderID;
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
