package com.applite.bean;

/**
 * Created by android217 on 6/18/15.
 */
public class HomePageTypeBean {
    private int id;
    private String mM_IconUrl;
    private String mM_Name;
    private String mM_key;
    @Override
    public String toString() {
        return "HomePageTypeBean{" +
                "id=" + id +
                ", m_iconurl='" + mM_IconUrl + '\'' +
                ", m_name='" + mM_Name + '\'' +
                ", mM_key='" + mM_key + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getM_IconUrl() {
        return mM_IconUrl;
    }

    public void setM_IconUrl(String mIconUrl) {
        this.mM_IconUrl = mIconUrl;
    }

    public String getM_Name() {
        return mM_Name;
    }

    public void setM_Name(String mName) {
        this.mM_Name = mName;
    }

    public String getM_key() {
        return mM_key;
    }

    public void setM_key(String mKey) {
        this.mM_key = mKey;
    }

}
