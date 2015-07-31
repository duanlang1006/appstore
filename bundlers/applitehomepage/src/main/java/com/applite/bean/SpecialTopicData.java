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
    @Override
    public String toString(){
        return "SpecialTopicData {" + '\'' +
                ", t_iconurl=" +t_iconurl + '\'' +
                ", t_key=" + t_key + '\'' +
                ", t_info=" + t_info + '\'' +
                "}";
    }

    public SpecialTopicData() {
        t_iconurl = null;
        t_key = null;
        t_info = null;
    }

    public SpecialTopicData(Parcel in) {
        t_iconurl = in.readString();
        t_key = in.readString();
        t_info = in.readString();
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
}
