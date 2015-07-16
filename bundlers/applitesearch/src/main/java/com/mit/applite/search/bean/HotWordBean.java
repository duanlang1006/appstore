package com.mit.applite.search.bean;

/**
 * Created by LSY on 15-5-27.
 */
public class HotWordBean {
    private int _id;
    private String mName;//应用名或专题名
    private String mImgUrl;
    private int mType;//判断是应用还是专题    0是应用   1是专题
    private String mPackageName;//应用包名或专题key

    //专题特有
    private int mStep;//部长
    private String mDataType;//数据类型

    @Override
    public String toString() {
        return "HotWordBean{" +
                "_id=" + _id +
                ", mName='" + mName + '\'' +
                ", mImgUrl='" + mImgUrl + '\'' +
                ", mType=" + mType +
                ", mPackageName='" + mPackageName + '\'' +
                ", mStep=" + mStep +
                ", mDataType='" + mDataType + '\'' +
                '}';
    }

    public String getmPackageName() {
        return mPackageName;
    }

    public void setmPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
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

    public String getmImgUrl() {
        return mImgUrl;
    }

    public void setmImgUrl(String mImgUrl) {
        this.mImgUrl = mImgUrl;
    }

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public int getmStep() {
        return mStep;
    }

    public void setmStep(int mStep) {
        this.mStep = mStep;
    }

    public String getmDataType() {
        return mDataType;
    }

    public void setmDataType(String mDataType) {
        this.mDataType = mDataType;
    }
}
