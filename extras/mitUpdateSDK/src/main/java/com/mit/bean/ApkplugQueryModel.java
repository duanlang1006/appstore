package com.mit.bean;

import java.util.List;

/**
 * Created by hxd on 15-5-29.
 */
public class ApkplugQueryModel<T> implements java.io.Serializable{
    private int totalRows;
    private int page;
    private int totalPage;
    private List<T> data;

    public ApkplugQueryModel() { }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotlepage() {
        return totalPage;
    }

    public void setTotlepage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}