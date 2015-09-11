package com.applite.bean;

/**
 * Created by LSY on 15-9-11.
 */
public class PopupWindowBean {
    //去专题的数据
    private String s_key;
    private String s_name;
    private String s_datatype;
    private int step;

    //去详情的数据
    private String mName;
    private String mPackageName;
    private String mIconUrl;

    //显示时间,链接,名字
    private String mPopImgUrl;
    private String mPopImgName;
    private long mPopStartTime;
    private long mPopEndTime;

    @Override
    public String toString() {
        return "PopupWindowBean{" +
                "s_key='" + s_key + '\'' +
                ", s_name='" + s_name + '\'' +
                ", s_datatype='" + s_datatype + '\'' +
                ", step=" + step +
                ", mName='" + mName + '\'' +
                ", mPackageName='" + mPackageName + '\'' +
                ", mIconUrl='" + mIconUrl + '\'' +
                ", mPopImgUrl='" + mPopImgUrl + '\'' +
                ", mPopImgName='" + mPopImgName + '\'' +
                ", mPopStartTime=" + mPopStartTime +
                ", mPopEndTime=" + mPopEndTime +
                '}';
    }

    public String getmPopImgName() {
        return mPopImgName;
    }

    public void setmPopImgName(String mPopImgName) {
        this.mPopImgName = mPopImgName;
    }

    public String getS_key() {
        return s_key;
    }

    public void setS_key(String s_key) {
        this.s_key = s_key;
    }

    public String getS_name() {
        return s_name;
    }

    public void setS_name(String s_name) {
        this.s_name = s_name;
    }

    public String getS_datatype() {
        return s_datatype;
    }

    public void setS_datatype(String s_datatype) {
        this.s_datatype = s_datatype;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
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

    public String getmIconUrl() {
        return mIconUrl;
    }

    public void setmIconUrl(String mIconUrl) {
        this.mIconUrl = mIconUrl;
    }

    public String getmPopImgUrl() {
        return mPopImgUrl;
    }

    public void setmPopImgUrl(String mPopImgUrl) {
        this.mPopImgUrl = mPopImgUrl;
    }

    public long getmPopStartTime() {
        return mPopStartTime;
    }

    public void setmPopStartTime(long mPopStartTime) {
        this.mPopStartTime = mPopStartTime;
    }

    public long getmPopEndTime() {
        return mPopEndTime;
    }

    public void setmPopEndTime(long mPopEndTime) {
        this.mPopEndTime = mPopEndTime;
    }
}
