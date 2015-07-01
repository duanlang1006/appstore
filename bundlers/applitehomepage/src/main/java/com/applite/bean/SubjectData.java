package com.applite.bean;

import java.util.List;

/**
 * Created by yuzhimin on 6/30/15.
 */
public class SubjectData {
    private String s_key;
    private String s_name;
    private String s_datatype;
    private List<SpecialTopicData> specialtopic_data;
    private List<HomePageApkData> data;

    public String getS_Key() {
        return s_key;
    }
    public void setS_Key(String mKey) {
        this.s_key = mKey;
    }

    public String getS_DataType() {
        return s_datatype;
    }
    public void setS_DataType(String mDataType) {
        this.s_datatype = mDataType;
    }

    public String getS_name() {
        return s_name;
    }
    public void setS_name(String mName) {
        this.s_name = mName;
    }

    public List<HomePageApkData> getHomePageApkData() {
        return data;
    }

    public void setHomePageApkData(List<HomePageApkData> mData) {
        this.data = mData;
    }

    public List<SpecialTopicData> getSpecialTopicData() {
        return specialtopic_data;
    }
    public void setSpecialTopicData(List<SpecialTopicData> mTopicData) {
        this.specialtopic_data = mTopicData;
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
