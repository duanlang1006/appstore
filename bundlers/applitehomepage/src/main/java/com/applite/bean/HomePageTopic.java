package com.applite.bean;

/**
 * Created by yuzhimin on 6/25/15.
 */
public class HomePageTopic {
    private int id;
    private String mT_IconUrl;
    private String mT_Info;
    private String mT_key;
    @Override
    public String toString() {
        return "HomePageTopic{" +
                "id=" + id +
                ", t_iconurl='" + mT_IconUrl + '\'' +
                ", t_info='" + mT_Info + '\'' +
                ", t_key='" + mT_key + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getS_IconUrl() {
        return mT_IconUrl;
    }

    public void setS_IconUrl(String mIconUrl) {
        this.mT_IconUrl = mIconUrl;
    }

    public String getS_Info() {
        return mT_Info;
    }

    public void setS_Info(String mName) {
        this.mT_Info = mName;
    }

    public String getS_key() {
        return mT_key;
    }

    public void setS_key(String mKey) {
        this.mT_key = mKey;
    }
}
