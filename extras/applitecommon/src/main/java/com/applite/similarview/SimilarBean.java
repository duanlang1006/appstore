package com.applite.similarview;

/**
 * Created by LSY on 15-8-12.
 */
public class SimilarBean {

    private String _id;
    private String name;
    private String packageName;
    private String iconUrl;
    private int versionCode;
    private String rDownloadUrl;

    @Override
    public String toString() {
        return "SimilarBean{" +
                "_id=" + _id +
                ", mName='" + name + '\'' +
                ", mPackageName='" + packageName + '\'' +
                ", mImgUrl='" + iconUrl + '\'' +
                ", mVersionCode=" + versionCode +
                ", mDownloadUrl='" + rDownloadUrl + '\'' +
                '}';
    }

    public String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getrDownloadUrl() {
        return rDownloadUrl;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setrDownloadUrl(String rDownloadUrl) {
        this.rDownloadUrl = rDownloadUrl;
    }
}
