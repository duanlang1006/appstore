package com.mit.appliteupdate.bean;


/**
 * Created by LSY on 15-6-23.
 */
public class ApkData {
    private String name;
    private String iconUrl;
    private String packageName;
    private String rDownloadUrl;
    private long apkSize;
    private int versionCode;
    private String versionName;
    private String apkMd5;
//    private String mShowText;

    //    public String getmShowText() {
//        return mShowText;
//    }
//
//    public void setmShowText(String mShowText) {
//        this.mShowText = mShowText;
//    }

    @Override
    public String toString() {
        return "DataBean{" +
                "mName='" + name + '\'' +
                ", mImgUrl='" + iconUrl + '\'' +
                ", mPackageName='" + packageName + '\'' +
                ", mUrl='" + rDownloadUrl + '\'' +
                ", mSize=" + apkSize +
                ", mVersionCode=" + versionCode +
                ", mVersionName='" + versionName + '\'' +
                ", mMD5='" + apkMd5 + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getrDownloadUrl() {
        return rDownloadUrl;
    }

    public long getApkSize() {
        return apkSize;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getApkMd5() {
        return apkMd5;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setrDownloadUrl(String rDownloadUrl) {
        this.rDownloadUrl = rDownloadUrl;
    }

    public void setApkSize(long apkSize) {
        this.apkSize = apkSize;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setApkMd5(String apkMd5) {
        this.apkMd5 = apkMd5;
    }
}
