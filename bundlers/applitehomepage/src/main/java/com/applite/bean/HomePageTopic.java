package com.applite.bean;

/**
 * Created by yuzhimin on 6/25/15.
 */
public class HomePageTopic {
    private int id;
    private String mS_IconUrl;
    private String mS_Name;
    private String mS_key;
    @Override
    public String toString() {
        return "HomePageTopic{" +
                "id=" + id +
                ", s_iconurl='" + mS_IconUrl + '\'' +
                ", s_name='" + mS_Name + '\'' +
                ", s_key='" + mS_key + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getS_IconUrl() {
        return mS_IconUrl;
    }

    public void setS_IconUrl(String mIconUrl) {
        this.mS_IconUrl = mIconUrl;
    }

    public String getS_Name() {
        return mS_Name;
    }

    public void setS_Name(String mName) {
        this.mS_Name = mName;
    }

    public String getS_key() {
        return mS_key;
    }

    public void setS_key(String mKey) {
        this.mS_key = mKey;
    }
}
