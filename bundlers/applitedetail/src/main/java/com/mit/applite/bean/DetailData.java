package com.mit.applite.bean;

import com.applite.bean.ApkBean;
import com.applite.similarview.SimilarBean;

import java.util.List;

/**
 * Created by LSY on 15-9-22.
 */
public class DetailData {
    private int app_key;
    private List<ApkBean> detail_info;
    private List<SimilarBean> similar_info;

    public int getApp_key() {
        return app_key;
    }

    public void setApp_key(int app_key) {
        this.app_key = app_key;
    }

    public List<ApkBean> getDetail_info() {
        return detail_info;
    }

    public void setDetail_info(List<ApkBean> detail_info) {
        this.detail_info = detail_info;
    }

    public List<SimilarBean> getSimilar_info() {
        return similar_info;
    }

    public void setSimilar_info(List<SimilarBean> similar_info) {
        this.similar_info = similar_info;
    }
}
