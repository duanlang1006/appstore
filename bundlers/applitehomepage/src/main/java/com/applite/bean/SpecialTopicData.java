package com.applite.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yuzhimin on 6/30/15.
 */
public class SpecialTopicData implements Parcelable{
    public String t_iconurl;
    public String t_key;
    public String t_info;
    public int t_skiptype;
    public String tt_packageName;
    public String tt_name;
    public String tt_iconUrl;

    public int getT_skiptype(){
        return t_skiptype;
    }

    public String getTt_packageName() {
        return tt_packageName;
    }

    public String getTt_name() {
        return tt_name;
    }

    public String getTt_iconUrl() {
        return tt_iconUrl;
    }

    public void setT_skiptype(int t_skiptype) {
        this.t_skiptype = t_skiptype;
    }

    public void setTt_packageName(String tt_packageName) {
        this.tt_packageName = tt_packageName;
    }

    public void setTt_name(String tt_name) {
        this.tt_name = tt_name;
    }

    public void setTt_iconUrl(String tt_iconUrl) {
        this.tt_iconUrl = tt_iconUrl;
    }

    public String getT_iconurl() {
        return t_iconurl;
    }

    public String getT_key() {
        return t_key;
    }

    public String getT_info() {
        return t_info;
    }

    public void setT_iconurl(String t_iconurl) {
        this.t_iconurl = t_iconurl;
    }

    public void setT_key(String t_key) {
        this.t_key = t_key;
    }

    public void setT_info(String t_info) {
        this.t_info = t_info;
    }

    public SpecialTopicData() {
        t_iconurl = null;
        t_key = null;
        t_info = null;
        t_skiptype = 0;
        tt_packageName = null;
        tt_name = null;
        tt_iconUrl = null;
    }

    public SpecialTopicData(Parcel in) {
        t_iconurl = in.readString();
        t_key = in.readString();
        t_info = in.readString();
        t_skiptype = in.readInt();
        tt_packageName = in.readString();
        tt_name = in.readString();
        tt_iconUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(t_iconurl);
        dest.writeString(t_key);
        dest.writeString(t_info);
        dest.writeInt(t_skiptype);
        dest.writeString(tt_packageName);
        dest.writeString(tt_name);
        dest.writeString(tt_iconUrl);
    }

    public static final Parcelable.Creator<SpecialTopicData> CREATOR = new Parcelable.Creator<SpecialTopicData>() {
        @Override
        public SpecialTopicData createFromParcel(Parcel in) {
            return new SpecialTopicData(in);
        }

        @Override
        public SpecialTopicData[] newArray(int size) {
            return new SpecialTopicData[size];
        }
    };

    @Override
    public String toString(){
        return "SpecialTopicData {" + '\'' +
                ", t_iconurl=" +t_iconurl + '\'' +
                ", t_key=" + t_key + '\'' +
                ", t_info=" + t_info + '\'' +
                ", t_skiptype=" + t_skiptype + '\'' +
                ", tt_packageName=" + tt_packageName + '\'' +
                ", tt_name=" + tt_name + '\'' +
                ", tt_iconUrl=" + tt_iconUrl + '\'' +

                "}";
    }
}
