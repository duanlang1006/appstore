package com.mit.applite.search.bean;

import com.applite.bean.ApkBean;

import java.util.List;

/**
 * Created by LSY on 15-5-27.
 */
public class SearchBean {
    private int app_key;
    private List<ApkBean> search_info;
    private boolean istoend;

    @Override
    public String toString() {
        return "SearchBean{" +
                "app_key=" + app_key +
                ", search_info=" + search_info +
                ", istoend=" + istoend +
                '}';
    }

    public int getApp_key() {
        return app_key;
    }

    public void setApp_key(int app_key) {
        this.app_key = app_key;
    }

    public List<ApkBean> getSearch_info() {
        return search_info;
    }

    public void setSearch_info(List<ApkBean> search_info) {
        this.search_info = search_info;
    }

    public boolean getIstoend() {
        return istoend;
    }

    public void setIstoend(boolean istoend) {
        this.istoend = istoend;
    }
}
