package com.applite.bean;

import java.util.List;

/**
 * Created by yuzhimin on 6/30/15.
 */
public class SubjectData {
    private String s_key;
    private String s_name;
    private String s_datatype;
    private int step;               //步长
    private List<SpecialTopicData> specialtopic_data; //专题数据
    private List<HomePageApkData> data;     //apk数据


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
