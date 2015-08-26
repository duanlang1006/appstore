package com.mit.appliteupdate.bean;

import com.mit.impl.ImplInfo;

/**
 * Created by LSY on 15-6-23.
 */
public class DataBean {
    private String mName;
    private String mImgUrl;
    private String mPackageName;
    private String mUrl;
    private long mSize;
    private int mVersionCode;
    private String mVersionName;
    private String mMD5;
//    private String mShowText;

    //    public String getmShowText() {
//        return mShowText;
//    }
//
//    public void setmShowText(String mShowText) {
//        this.mShowText = mShowText;
//    }

    @Override
    public String toString() {
        return "DataBean{" +
                "mName='" + mName + '\'' +
                ", mImgUrl='" + mImgUrl + '\'' +
                ", mPackageName='" + mPackageName + '\'' +
                ", mUrl='" + mUrl + '\'' +
                ", mSize=" + mSize +
                ", mVersionCode=" + mVersionCode +
                ", mVersionName='" + mVersionName + '\'' +
                ", mMD5='" + mMD5 + '\'' +
                '}';
    }

    public String getmMD5() {
        return mMD5;
    }

    public void setmMD5(String mMD5) {
        this.mMD5 = mMD5;
    }

    public String getmVersionName() {
        return mVersionName;
    }

    public void setmVersionName(String mVersionName) {
        this.mVersionName = mVersionName;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmImgUrl() {
        return mImgUrl;
    }

    public void setmImgUrl(String mImgUrl) {
        this.mImgUrl = mImgUrl;
    }

    public String getmPackageName() {
        return mPackageName;
    }

    public void setmPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public long getmSize() {
        return mSize;
    }

    public void setmSize(long mSize) {
        this.mSize = mSize;
    }

    public int getmVersionCode() {
        return mVersionCode;
    }

    public void setmVersionCode(int mVersionCode) {
        this.mVersionCode = mVersionCode;
    }
}
