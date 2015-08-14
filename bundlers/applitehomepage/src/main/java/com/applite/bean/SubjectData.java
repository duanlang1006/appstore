package com.applite.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuzhimin on 6/30/15.
 */
public class SubjectData implements Parcelable{
    private String s_key;
    private String s_name;
    private String s_datatype;      //指定用哪个布局
    private int step;               //步长
    private List<SpecialTopicData> specialtopic_data; //专题数据
    private List<HomePageApkData> data;     //apk数据


    public SubjectData() {
        s_key = null;
        s_name = null;
        s_datatype = null;
        step = 10;
        specialtopic_data = null;
        data = null;
    }

    public SubjectData(Parcel in) {
        s_key = in.readString();
        s_name = in.readString();
        s_datatype = in.readString();
        step = in.readInt();
        specialtopic_data = in.readArrayList(SpecialTopicData.class.getClassLoader());
        data = in.readArrayList(HomePageApkData.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(s_key);
        dest.writeString(s_name);
        dest.writeString(s_datatype);
        dest.writeInt(step);
        dest.writeList(specialtopic_data);
        dest.writeList(data);
    }

    public static final Parcelable.Creator<SubjectData> CREATOR = new Parcelable.Creator<SubjectData>() {
        @Override
        public SubjectData createFromParcel(Parcel in) {
            return new SubjectData(in);
        }

        @Override
        public SubjectData[] newArray(int size) {
            return new SubjectData[size];
        }
    };

    public String getS_key() {
        return s_key;
    }

    public String getS_datatype() {
        return s_datatype;
    }

    public List<SpecialTopicData> getSpecialtopic_data() {
        return specialtopic_data;
    }

    public List<HomePageApkData> getData() {
        return data;
    }

    public String getS_name() {
        return s_name;
    }

    public void setS_key(String s_key) {
        this.s_key = s_key;
    }

    public void setS_name(String s_name) {
        this.s_name = s_name;
    }

    public void setS_datatype(String s_datatype) {
        this.s_datatype = s_datatype;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setSpecialtopic_data(List<SpecialTopicData> specialtopic_data) {
        this.specialtopic_data = specialtopic_data;
    }

    public void setData(List<HomePageApkData> data) {
        this.data = data;
    }

    public int getStep() {
        return step;
    }

    @Override
    public String toString()
    {
        return "HomePageDataBean {" + '\'' +
                ", s_key=" + s_key + '\'' +
                ", s_name=" + s_name + '\'' +
                ", s_datatype=" + s_datatype + '\'' +
                ", specialtopic_data=" + specialtopic_data + '\'' +
                ", data=" + data + '\'' +
                "}";
    }
}
