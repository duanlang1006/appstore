package com.mit.applite.search.bean;

/**
 * Created by LSY on 15-5-27.
 */
public class HotWordBean {
    private int _id;
    private String mName;
    private String mImgUrl;
    private String mType;
    private String mPackageName;

    @Override
    public String toString() {
        return "HotWordBean{" +
                "_id=" + _id +
                ", mName='" + mName + '\'' +
                ", mImgUrl='" + mImgUrl + '\'' +
                ", mType='" + mType + '\'' +
                ", mPackageName='" + mPackageName + '\'' +
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

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }
}
