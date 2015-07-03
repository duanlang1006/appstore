package com.applite.bean;

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
    private int reason;
    private String localUri;
    private String mediaType;
    private long currentBytes;
    private long totalBytes;
    private int status;
    private String mDownloadNumber;

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

    public int getReason() {
        return reason;
    }

    public String getLocalUri() {
        return localUri;
    }

    public String getMediaType() {
        return mediaType;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public int getStatus() {
        return status;
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

    public void setReason(int reason) {
        this.reason = reason;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setmDownloadNumber(String mDownloadNumber) {
        this.mDownloadNumber = mDownloadNumber;
    }
}
