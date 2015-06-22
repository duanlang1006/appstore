package com.mit.impl;

import android.content.ContentValues;
import android.database.Cursor;

import com.applite.common.Constant;
import com.google.gson.Gson;

/**
 * Created by hxd on 15-6-11.
 */
public class ImplInfo {
    private String key;
    private String downloadUrl;
    private long downloadId ;
    private String packageName;
    private String iconPath;
    private String iconUrl;
    private int status;
    private int reason;
    private int progress;
    private String title;
    private String description;
    private long totalBytes;
    private long currentBytes;
    private String localPath;
    private String mimeType;
    private long lastMod;

    public ImplInfo(String key,
                    String downloadUrl, long downloadId,
                    String packageName,
                    String iconPath, String iconUrl,
                    String title, String description) {
        this.key = key;
        this.downloadUrl = downloadUrl;
        this.downloadId = downloadId;
        this.packageName = packageName;
        this.iconPath = iconPath;
        this.iconUrl = iconUrl;
        this.title = title;
        this.description = description;
        this.status = 0;
        this.progress = 0;
        this.totalBytes = 0;
        this.currentBytes = 0;
        this.localPath = null;
        this.mimeType = null;
        this.lastMod = System.currentTimeMillis();
    }

    public String getKey() {
        return key;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getIconPath() {
        return iconPath;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getLastMod() {
        return lastMod;
    }

    public void setLastMod(long lastMod) {
        this.lastMod = lastMod;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public int getReason() {
        return reason;
    }

    public String toJson(Gson gson){
        return gson.toJson(this);
    }

    public ContentValues getContentValues(){
        ContentValues values = new ContentValues();
        values.put(ImplConfig.COLUMN_KEY,key);
        values.put(ImplConfig.COLUMN_DOWNLOADURL,downloadUrl);
        values.put(ImplConfig.COLUMN_DOWNLOADID,downloadId);
        values.put(ImplConfig.COLUMN_PACKAGENAME,packageName);
        values.put(ImplConfig.COLUMN_ICON_PATH,iconPath);
        values.put(ImplConfig.COLUMN_ICON_URL,iconUrl);
        values.put(ImplConfig.COLUMN_TITLE,title);
        values.put(ImplConfig.COLUMN_DESCRIPTION,description);
        values.put(ImplConfig.COLUMN_TOTAL_BYTES,totalBytes);
        values.put(ImplConfig.COLUMN_CURRENT_BYTES,currentBytes);
        values.put(ImplConfig.COLUMN_LOCALURI, localPath);
        values.put(ImplConfig.COLUMN_MIMETYPE,mimeType);
        values.put(ImplConfig.COLUMN_STATUS,status);
        values.put(ImplConfig.COLUMN_REASON,reason);
        values.put(ImplConfig.COLUMN_LAST_MODIFIED_TIMESTAMP,lastMod);
        return values;
    }

    public static ImplInfo from(Cursor c){
        String key = c.getString(c.getColumnIndex(ImplConfig.COLUMN_KEY));
        String downloadUrl = c.getString(c.getColumnIndex(ImplConfig.COLUMN_DOWNLOADURL));
        long downloadId = c.getLong(c.getColumnIndex(ImplConfig.COLUMN_DOWNLOADID));
        String packageName = c.getString(c.getColumnIndex(ImplConfig.COLUMN_PACKAGENAME));
        String iconPath = c.getString(c.getColumnIndex(ImplConfig.COLUMN_ICON_PATH));
        String iconUrl = c.getString(c.getColumnIndex(ImplConfig.COLUMN_ICON_URL));
        String title = c.getString(c.getColumnIndex(ImplConfig.COLUMN_TITLE));
        String description = c.getString(c.getColumnIndex(ImplConfig.COLUMN_DESCRIPTION));
        long totalBytes = c.getLong(c.getColumnIndex(ImplConfig.COLUMN_TOTAL_BYTES));
        long currentBytes = c.getLong(c.getColumnIndex(ImplConfig.COLUMN_CURRENT_BYTES));
        String localUri = c.getString(c.getColumnIndex(ImplConfig.COLUMN_LOCALURI));
        String mimeType = c.getString(c.getColumnIndex(ImplConfig.COLUMN_MIMETYPE));
        int status = c.getInt(c.getColumnIndex(ImplConfig.COLUMN_STATUS));
        int reason = c.getInt(c.getColumnIndex(ImplConfig.COLUMN_REASON));
        long lastMod = c.getLong(c.getColumnIndex(ImplConfig.COLUMN_LAST_MODIFIED_TIMESTAMP));
        ImplInfo info = new ImplInfo(key,downloadUrl,downloadId,packageName,iconPath,iconUrl,title,description);
        info.setTotalBytes(totalBytes);
        info.setCurrentBytes(currentBytes);
        info.setLocalPath(localUri);
        info.setMimeType(mimeType);
        info.setStatus(status);
        info.setLastMod(lastMod);
        info.setReason(reason);
        return info;
    }
}