package com.mit.appliteupdate.bean;


import com.applite.bean.ApkBean;
import com.applite.similarview.SimilarBean;

import java.util.List;

/**
 * Created by LSY on 15-6-23.
 */
public class UpdateData {
    private int app_key;
    private List<ApkBean> installed_update_list;
    private List<SimilarBean> similar_info;

    public int getApp_key() {
        return app_key;
    }

    public List<ApkBean> getInstalled_update_list() {
        return installed_update_list;
    }

    public List<SimilarBean> getSimilar_info() {
        return similar_info;
    }

    public void setApp_key(int app_key) {
        this.app_key = app_key;
    }

    public void setInstalled_update_list(List<ApkBean> installed_update_list) {
        this.installed_update_list = installed_update_list;
    }

    public void setSimilar_info(List<SimilarBean> similar_info) {
        this.similar_info = similar_info;
    }
}
