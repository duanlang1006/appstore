package com.applite.similarview;

import com.mit.impl.ImplInfo;

/**
 * Created by LSY on 15-8-12.
 */
public class SimilarBean {

    private int _id;
    private String mName;
    private String mPackageName;
    private String mImgUrl;
    private int mVersionCode;
    private String mDownloadUrl;

    private ImplInfo mImplInfo;

    @Override
    public String toString() {
        return "SimilarBean{" +
                "_id=" + _id +
                ", mName='" + mName + '\'' +
                ", mPackageName='" + mPackageName + '\'' +
                ", mImgUrl='" + mImgUrl + '\'' +
                ", mVersionCode=" + mVersionCode +
                ", mDownloadUrl='" + mDownloadUrl + '\'' +
                ", mImplInfo=" + mImplInfo +
                '}';
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

    public ImplInfo getmImplInfo() {
        return mImplInfo;
    }

    public void setmImplInfo(ImplInfo mImplInfo) {
        this.mImplInfo = mImplInfo;
    }
}
