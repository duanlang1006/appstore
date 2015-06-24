package com.applite.bean;

/**
 * Created by android217 on 6/18/15.
 */
public class HomePageTypeBean {
    private int id;
    private String mM_IconUrl;
    private String mM_Name;
    @Override
    public String toString() {
        return "HomePageBean{" +
                "id=" + id +
                ", m_iconurl='" + mM_IconUrl + '\'' +
                ", m_name='" + mM_Name + '\'' +
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

}
