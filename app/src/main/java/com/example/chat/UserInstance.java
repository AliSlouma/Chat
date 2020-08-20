package com.example.chat;

import androidx.annotation.NonNull;

public class UserInstance {
    private String mUId;
    private String mE_mail;
    private String mName;
    private String mStatus;
    private String mImageUri;

    public String getImageUri() {
        return mImageUri;
    }

    public void setImageUri(String imageUri) {
        mImageUri = imageUri;
    }

    public UserInstance(){}
    public UserInstance(String UId, String e_mail, String name, String status,String uri) {
        mUId = UId;
        mE_mail = e_mail;
        mName = name;
        mStatus = status;
        mImageUri = uri;
    }

    @NonNull
    @Override
    public String toString() {
        return mName + "\n" + mStatus;
    }

    public String getUId() {
        return mUId;
    }

    public void setUId(String UId) {
        mUId = UId;
    }

    public String getE_mail() {
        return mE_mail;
    }

    public void setE_mail(String e_mail) {
        mE_mail = e_mail;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }
}
