package com.mit.applite.search.bean;

/**
 * Created by LSY on 15-5-27.
 */
public class SearchBean {
    private int _id;
    private String mName;
    private String mPackageName;
    private String mImgUrl;
    private String mVersionName;
    private int mVersionCode;
    private String mDownloadUrl;
    private String mDownloadNumber;
    private String mApkSize;
    private String mXing;

    private String mShowButtonText;

    public String getmShowButtonText() {
        return mShowButtonText;
    }

    public void setmShowButtonText(String mShowButtonText) {
        this.mShowButtonText = mShowButtonText;
    }

    @Override
    public String toString() {
        return "SearchBean{" +
                "_id=" + _id +
                ", mName='" + mName + '\'' +
                ", mPackageName='" + mPackageName + '\'' +
                ", mImgUrl='" + mImgUrl + '\'' +
                ", mVersionName='" + mVersionName + '\'' +
                ", mVersionCode='" + mVersionCode + '\'' +
                ", mDownloadUrl='" + mDownloadUrl + '\'' +
                ", mDownloadNumber='" + mDownloadNumber + '\'' +
                ", mApkSize='" + mApkSize + '\'' +
                ", mXing='" + mXing + '\'' +
                '}';
    }

    public String getmVersionName() {
        return mVersionName;
    }

    public void setmVersionName(String mVersionName) {
        this.mVersionName = mVersionName;
    }

    public String getmXing() {
        return mXing;
    }

    public void setmXing(String mXing) {
        this.mXing = mXing;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
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

    public String getmDownloadNumber() {
        return mDownloadNumber;
    }

    public void setmDownloadNumber(String mDownloadNumber) {
        this.mDownloadNumber = mDownloadNumber;
    }

    public String getmApkSize() {
        return mApkSize;
    }

    public void setmApkSize(String mApkSize) {
        this.mApkSize = mApkSize;
    }
}
