package com.applite.bean;

/**
 * Created by LSY on 15-9-22.
 */
public class ApkBean {
    private String name;//应用名
    private String iconUrl;//图标链接
    private String packageName;//包名
    private String rDownloadUrl;//下载链接
    private long apkSize;//大小
    private int versionCode;//版本号
    private String versionName;//版本名
    private String apkMd5;//MD5码
    private String rating;//星级
    private String downloadTimes;//下载次数
    private String description;//应用介绍
    private String screenshotsUrl;//应用介绍图片
    private String developer;
    private String tag;//标签
    private String updateInfo;//更新内容
    private String updateTime;//更新时间

    @Override
    public String toString() {
        return "ApkBean{" +
                "name='" + name + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", packageName='" + packageName + '\'' +
                ", rDownloadUrl='" + rDownloadUrl + '\'' +
                ", apkSize=" + apkSize +
                ", versionCode=" + versionCode +
                ", versionName='" + versionName + '\'' +
                ", apkMd5='" + apkMd5 + '\'' +
                ", rating='" + rating + '\'' +
                ", downloadTimes='" + downloadTimes + '\'' +
                ", description='" + description + '\'' +
                ", screenshotsUrl='" + screenshotsUrl + '\'' +
                ", developer='" + developer + '\'' +
                ", tag='" + tag + '\'' +
                ", updateInfo='" + updateInfo + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getrDownloadUrl() {
        return rDownloadUrl;
    }

    public void setrDownloadUrl(String rDownloadUrl) {
        this.rDownloadUrl = rDownloadUrl;
    }

    public long getApkSize() {
        return apkSize;
    }

    public void setApkSize(long apkSize) {
        this.apkSize = apkSize;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getApkMd5() {
        return apkMd5;
    }

    public void setApkMd5(String apkMd5) {
        this.apkMd5 = apkMd5;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDownloadTimes() {
        return downloadTimes;
    }

    public void setDownloadTimes(String downloadTimes) {
        this.downloadTimes = downloadTimes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScreenshotsUrl() {
        return screenshotsUrl;
    }

    public void setScreenshotsUrl(String screenshotsUrl) {
        this.screenshotsUrl = screenshotsUrl;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
