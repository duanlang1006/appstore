package com.applite.bean;

import com.applite.common.Constant;

/**
 * Created by LSY on 15-5-22.
 */
public class HomePageBean {
    private int id;
    private String mCategorySub;
    private String mPackageName;
    private String mrDownloadUrl;
    private String mIconUrl;
    private String mName;
    private String mCategoryMain;
    private String mVersionName;
    private String mRating;
    private String mDownloadTimes;
    private String mBoxLabel;
    private String mBrief;
    private String mApkSize;
    private String mM_IconUrl;
    private String mM_Name;
    private int mVersionCode;
    private int status;
    private int reason;
    private String localUri;
    private String mediaType;
    private long currentBytes;
    private long totalBytes;
    private String mDownloadNumber;
    @Override
    public String toString() {
        return "HomePageBean{" +
                "id=" + id +
                ", packageName='" + mPackageName + '\'' +
                ", name='" + mName + '\'' +
                ", iconUrl='" + mIconUrl + '\'' +
                ", rDownloadUrl='" + mrDownloadUrl + '\'' +
                ", categorysub='" + mCategorySub + '\'' +
                ", apkSize='" + mApkSize + '\'' +
                ", brief='" + mBrief + '\'' +
                ", rating='" + mRating + '\'' +
                ", categorymain='" + mCategoryMain + '\'' +
                ", versionName='" + mVersionName + '\'' +
                ", mVersionCode='" + mVersionCode + '\'' +
                ", boxLabel='" + mBoxLabel + '\'' +
                ", downloadTimes='" + mDownloadTimes + '\'' +
                ", mDownloadNumber='" + mDownloadNumber + '\'' +
                ", m_iconurl='" + mM_IconUrl + '\'' +
                ", m_name='" + mM_Name + '\'' +
                '}';
    }

    public HomePageBean() {
        status = Constant.STATUS_INIT;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackagename() {
        return mPackageName;
    }

    public void setPackagename(String packagename) {
        this.mPackageName = packagename;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getImgurl() {
        return mIconUrl;
    }

    public void setImgurl(String imgurl) {
        this.mIconUrl = imgurl;
    }

    public String getUrl() {
        return mrDownloadUrl;
    }

    public void setUrl(String url) {
        this.mrDownloadUrl = url;
    }


    public String getApkSize() {
        return mApkSize;
    }

    public void setApkSize(String size) {
        this.mApkSize = size;
    }

    public String getCategorySub() {
        return mCategorySub;
    }

    public void setCategorySub(String categorySub) {
        this.mCategorySub = categorySub;
    }

    public String getCategoryMain() {
        return mCategoryMain;
    }

    public void setCategoryMain(String categoryMain) {
        this.mCategoryMain = categoryMain;
    }

    public String getBrief() {
        return mBrief;
    }

    public void setBrief(String brief) {
        this.mBrief = brief;
    }

    public String getBoxLabel() {
        return mBoxLabel;
    }
    public void setBoxLabel(String boxLabel) {
        this.mBoxLabel = boxLabel;
    }

    public String getRating() {
        return mRating;
    }
    public void setRating(String rating) {
        this.mRating = rating;
    }

    public String getVersionName() {
        return mVersionName;
    }
    public void setVersionName(String versionName) {
        this.mVersionName = versionName;
    }
    public int getmVersionCode() {
        return mVersionCode;
    }

    public void setmVersionCode(int mVersionCode) {
        this.mVersionCode = mVersionCode;
    }
    public String getDownloadTimes() {
        return mDownloadTimes;
    }
    public void setDownloadTimes(String downloadTimes) {
        this.mDownloadTimes = downloadTimes;
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
