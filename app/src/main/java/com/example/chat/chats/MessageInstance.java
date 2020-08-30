package com.example.chat.chats;

public class MessageInstance {
    private String mMessage;
    private String mTime;
    private String mDate;
    private boolean mSeen;
    private String mSender;
    private String mReceiverPhoto;
    private String mSenderID;
    private String mType;
    private String mChatPhoto;
    public MessageInstance(String mMessage, String mTime, String mDate, boolean mSeen, String mSender,
                           String mReceiverPhoto, String mSenderID , String mType, String mChatPhoto) {
        this.mMessage = mMessage;
        this.mTime = mTime;
        this.mDate = mDate;
        this.mSeen = mSeen;
        this.mSender = mSender;
        this.mReceiverPhoto = mReceiverPhoto;
        this.mSenderID = mSenderID;
        this.mType = mType;
        this.mChatPhoto = mChatPhoto;
    }

    public String getmChatPhoto() {
        return mChatPhoto;
    }

    public void setmChatPhoto(String mChatPhoto) {
        this.mChatPhoto = mChatPhoto;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public String getmReceiverPhoto() {
        return mReceiverPhoto;
    }

    public void setmReceiverPhoto(String mReceiverPhoto) {
        this.mReceiverPhoto = mReceiverPhoto;
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
