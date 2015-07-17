package com.applite.bean;

import com.mit.impl.ImplInfo;

/**
 * Created by yuzhimin on 6/30/15.
 */
public class HomePageApkData {
    private String key;
    private String packageName;
    private String name;
    private String categorymain;
    private String categorysub;
    private String iconUrl;
    private String rating;
    private String versionName;
    private int versionCode;
    private String boxLabel;
    private String downloadTimes;
    private String rDownloadUrl;
    private String apkSize;
    private String brief;
    private String mDownloadNumber;
    private ImplInfo implInfo;

    @Override
    public String toString() {
        return "HomePageApkData{" +
                ", key='" + key + '\'' +
                ", packageName='" + packageName + '\'' +
                ", name='" + name + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", rDownloadUrl='" + rDownloadUrl + '\'' +
                ", categorysub='" + categorysub + '\'' +
                ", apkSize='" + apkSize + '\'' +
                ", brief='" + brief + '\'' +
                ", rating='" + rating + '\'' +
                ", categorymain='" + categorymain + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode='" + versionCode + '\'' +
                ", boxLabel='" + boxLabel + '\'' +
                ", downloadTimes='" + downloadTimes + '\'' +
                ", mDownloadNumber='" + mDownloadNumber + '\'' +
                '}';
    }

    public String getKey() {
        return key;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getCategorymain() {
        return categorymain;
    }

    public String getCategorysub() {
        return categorysub;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getRating() {
        return rating;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getBoxLabel() {
        return boxLabel;
    }

    public String getDownloadTimes() {
        return downloadTimes;
    }

    public String getrDownloadUrl() {
        return rDownloadUrl;
    }

    public String getApkSize() {
        return apkSize;
    }

    public String getBrief() {
        return brief;
    }

    public ImplInfo getImplInfo() {
        return implInfo;
    }

    public String getmDownloadNumber() {
        return mDownloadNumber;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategorymain(String categorymain) {
        this.categorymain = categorymain;
    }

    public void setCategorysub(String categorysub) {
        this.categorysub = categorysub;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setBoxLabel(String boxLabel) {
        this.boxLabel = boxLabel;
    }

    public void setDownloadTimes(String downloadTimes) {
        this.downloadTimes = downloadTimes;
    }

    public void setrDownloadUrl(String rDownloadUrl) {
        this.rDownloadUrl = rDownloadUrl;
    }

    public void setApkSize(String apkSize) {
        this.apkSize = apkSize;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public void setmDownloadNumber(String mDownloadNumber) {
        this.mDownloadNumber = mDownloadNumber;
    }

    public void setImplInfo(ImplInfo implInfo) {
        this.implInfo = implInfo;
    }
}
