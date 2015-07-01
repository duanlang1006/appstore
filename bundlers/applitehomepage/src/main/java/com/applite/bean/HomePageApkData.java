package com.applite.bean;

/**
 * Created by yuzhimin on 6/30/15.
 */
public class HomePageApkData {

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

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public String getRDownloadUrl() {
        return rDownloadUrl;
    }

    public void setRDownloadUrl(String url) {
        this.rDownloadUrl = url;
    }


    public String getApkSize() {
        return apkSize;
    }

    public void setApkSize(String size) {
        this.apkSize = size;
    }

    public String getCategorySub() {
        return categorysub;
    }

    public void setCategorySub(String categorySub) {
        this.categorysub = categorySub;
    }

    public String getCategoryMain() {
        return categorymain;
    }

    public void setCategoryMain(String categoryMain) {
        this.categorymain = categoryMain;
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public String getBoxLabel() {
        return boxLabel;
    }
    public void setBoxLabel(String boxLabel) {
        this.boxLabel = boxLabel;
    }

    public String getRating() {
        return rating;
    }
    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getVersionName() {
        return versionName;
    }
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }
    public void setVersionCode(int mVersionCode) {
        this.versionCode = mVersionCode;
    }

    public String getDownloadTimes() {
        return downloadTimes;
    }
    public void setDownloadTimes(String downloadTimes) {
        this.downloadTimes = downloadTimes;
    }

    public String getDownloadNumber() {
        return mDownloadNumber;
    }

    public void setDownloadNumber(String mDownloadNumber) {
        this.mDownloadNumber = mDownloadNumber;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getLocalUri() {
        return localUri;
    }

    public int getReason() {
        return reason;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }
}
