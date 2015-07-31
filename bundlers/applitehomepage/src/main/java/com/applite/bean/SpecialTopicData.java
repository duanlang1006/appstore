package com.applite.bean;


public class SpecialTopicData {
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

    public String getT_packagename(){
        return tt_packageName;
    }

    public String getT_name(){
        return tt_name;
    }

    public String getT_iconurl(){
        return tt_iconUrl;
    }

    @Override
    public String toString()
    {
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
