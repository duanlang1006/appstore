package com.mit.bean;

/**
 * Created by LSY on 15-5-22.
 */
public class GuideBean {
    private int id;
    private String packagename;
    private String name;
    private String imgurl;
    private String url;
    private int mVersionCode;
    private int mShowPosition;

    @Override
    public String toString() {
        return "GuideBean{" +
                "id=" + id +
                ", packagename='" + packagename + '\'' +
                ", name='" + name + '\'' +
                ", imgurl='" + imgurl + '\'' +
                ", url='" + url + '\'' +
                ", mVersionCode=" + mVersionCode +
                ", mShowPosition=" + mShowPosition +
                '}';
    }

    public int getmShowPosition() {
        return mShowPosition;
    }

    public void setmShowPosition(int mShowPosition) {
        this.mShowPosition = mShowPosition;
    }

    public int getmVersionCode() {
        return mVersionCode;
    }

    public void setmVersionCode(int mVersionCode) {
        this.mVersionCode = mVersionCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackagename() {
        return packagename;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
