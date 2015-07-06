package com.applite.bean;

/**
 * Created by LSY on 15-7-1.
 */
public class ScreenBean {
    private String mName;
    private String mPackageName;
    private String mImgUrl;
    private String mVersionName;
    private int mVersionCode;
    private String mDownloadUrl;
    private long mSize;

    @Override
    public String toString() {
        return "ScreenBean{" +
                "mName='" + mName + '\'' +
                ", mPackageName='" + mPackageName + '\'' +
                ", mImgUrl='" + mImgUrl + '\'' +
                ", mVersionName='" + mVersionName + '\'' +
                ", mVersionCode=" + mVersionCode +
                ", mDownloadUrl='" + mDownloadUrl + '\'' +
                ", mSize='" + mSize + '\'' +
                '}';
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmPackageName() {
        return mPackageName;
    }

    public void setmPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getmImgUrl() {
        return mImgUrl;
    }

    public void setmImgUrl(String mImgUrl) {
        this.mImgUrl = mImgUrl;
    }

    public String getmVersionName() {
        return mVersionName;
    }

    public void setmVersionName(String mVersionName) {
        this.mVersionName = mVersionName;
    }

    public int getmVersionCode() {
        return mVersionCode;
    }

    public void setmVersionCode(int mVersionCode) {
        this.mVersionCode = mVersionCode;
    }

    public String getmDownloadUrl() {
        return mDownloadUrl;
    }

    public void setmDownloadUrl(String mDownloadUrl) {
        this.mDownloadUrl = mDownloadUrl;
    }

    public long getmSize() {
        return mSize;
    }

    public void setmSize(long mSize) {
        this.mSize = mSize;
    }
}
