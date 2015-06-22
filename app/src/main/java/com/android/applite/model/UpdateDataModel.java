package com.android.applite.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import com.applite.util.AppliteUtilities;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UpdateDataModel {
    private String update_url;
    private int update_interval;   //小时为单位
    private int recommend_hide;  //隐藏推荐栏
    private long data_timestamp;  //更新时间戳
    private List<AppData> data;
    
    private static UpdateDataModel defaultData = null;
    
    private static synchronized void initDefaultData(Context context){
        if (null == defaultData){
            int len = -1;
            byte[] data = new byte[1024];
            InputStream in = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                android.content.pm.ApplicationInfo appInfo=context.getPackageManager().getApplicationInfo(
                                        context.getPackageName(), PackageManager.GET_META_DATA);
                String filename=appInfo.metaData.getString("APPLITE_DATA");  
                in = context.getAssets().open(filename);
                while ((len = in.read(data)) != -1) {
                    out.write(data, 0, len);
                }
                String jsonString = new String(out.toByteArray(), "utf-8");
                defaultData = new Gson().fromJson(jsonString, UpdateDataModel.class);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static UpdateDataModel getDefaultData(Context context){
        if (null == defaultData){
            initDefaultData(context);
        }
        return defaultData;
    }
    
    public UpdateDataModel() {
        super();
        // TODO Auto-generated constructor stub
        update_interval = 12;
        recommend_hide = 1;
        data_timestamp = 0;
    }

    public String getUpdate_url() {
        return update_url;
    }

    public void setUpdate_url(String update_url) {
        this.update_url = update_url;
    }
    
    public int getUpdate_interval() {
        return update_interval;
    }

    public void setUpdate_interval(int update_interval) {
        this.update_interval = update_interval;
    }

    public int getRecommend_hide() {
        return recommend_hide;
    }

    public void setRecommend_hide(int recommend_hide) {
        this.recommend_hide = recommend_hide;
    }

    public long getData_timestamp() {
        return data_timestamp;
    }

    public void setData_timestamp(long data_timestamp) {
        this.data_timestamp = data_timestamp;
    }

    public List<AppData> getData() {
        return data;
    }

    public void setData(List<AppData> data) {
        this.data = data;
    }


    public static class AppData{
        private int spanX;
        private int spanY;
        private int cellY;
        private int cellX;
        private int screen;
        private String id;
        private String icon_url;
        private String app_name;
        private String detail_url;
        private String package_name;
        private String class_name;
        private String intent;
        private String apk_url;
        private long size;
        private int version_code;
        private String version_name;
        private int data_download;
        private String fb_url;
        private int item_group;
        private String introduce_text;
        private String app_apkstartdate;
        private String app_apkenddate;
        private int app_buildin;
        private String app_buildinpath;
        
        private Intent realIntent;
        
        public AppData() {
            super();
        }
        
        public AppData(Context context,Cursor c){
            try{
                screen = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.SCREEN));
                cellX = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.CELLX));
                cellY = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.CELLY));
                spanX = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.SPANX));
                spanY = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.SPANY));
                
                id = c.getString(c.getColumnIndex(AppLiteSettings.Favorites.ID));
                icon_url = c.getString(c.getColumnIndex(AppLiteSettings.Favorites.ICON_URL));
                apk_url = c.getString(c.getColumnIndex(AppLiteSettings.Favorites.APK_URL));
                size = c.getLong(c.getColumnIndex(AppLiteSettings.Favorites.APK_SIZE));
                version_code = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.APK_VER_CODE));
                version_name = c.getString(c.getColumnIndex(AppLiteSettings.Favorites.APK_VER_NAME));
                data_download = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.DATA_DOWNLOAD));
                fb_url = c.getString(c.getColumnIndex(AppLiteSettings.Favorites.FEEDBACK_URL));
                app_name = c.getString(c.getColumnIndex(AppLiteSettings.Favorites.TITLE));
                //packagename,classname 组合成intent
                intent = c.getString(c.getColumnIndex(AppLiteSettings.Favorites.INTENT));
//                intent = Intent.parseUri(intentDesc, 0);
                detail_url = c.getString(c.getColumnIndex(AppLiteSettings.Favorites.DETAIL_URL));
                introduce_text = c.getString(c.getColumnIndex(AppLiteSettings.Favorites.INTRODUCE_TEXT));
                long period_start = c.getLong(c.getColumnIndex(AppLiteSettings.Favorites.PERIOD_START));
                long period_end = c.getLong(c.getColumnIndex(AppLiteSettings.Favorites.PERIOD_END));
                app_apkstartdate = AppliteUtilities.millis2DateStr(context, period_start);
                app_apkenddate = AppliteUtilities.millis2DateStr(context, period_end);
                app_buildin = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.DEFAULT_PENDING));
                app_buildinpath =c.getString(c.getColumnIndex(AppLiteSettings.Favorites.BUILDIN_PATH));
                item_group = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.ITEM_GROUP));
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
        ContentValues getContentValues(Context context) {
            ContentValues values = new ContentValues();
            values.put(AppLiteSettings.Favorites.ID, id);
            values.put(AppLiteSettings.Favorites.SCREEN, screen);
            values.put(AppLiteSettings.Favorites.CELLX, cellX);
            values.put(AppLiteSettings.Favorites.CELLY, cellY);
            values.put(AppLiteSettings.Favorites.SPANX, spanX);
            values.put(AppLiteSettings.Favorites.SPANY, spanY);
            values.put(AppLiteSettings.Favorites.ICON_URL, icon_url);
            values.put(AppLiteSettings.Favorites.APK_URL, apk_url);
            values.put(AppLiteSettings.Favorites.APK_SIZE, size);
            values.put(AppLiteSettings.Favorites.APK_VER_CODE, version_code);
            values.put(AppLiteSettings.Favorites.APK_VER_NAME, version_name);
            values.put(AppLiteSettings.Favorites.DATA_DOWNLOAD, data_download);
            values.put(AppLiteSettings.Favorites.FEEDBACK_URL, fb_url);
            values.put(AppLiteSettings.Favorites.TITLE, app_name);
            Intent intent = getRealIntent();
            values.put(AppLiteSettings.Favorites.INTENT, (null==intent)?null:intent.toUri(0));
            values.put(AppLiteSettings.Favorites.DETAIL_URL, detail_url);
            values.put(AppLiteSettings.Favorites.INTRODUCE_TEXT, introduce_text);
            values.put(AppLiteSettings.Favorites.ITEM_GROUP, item_group);
            values.put(AppLiteSettings.Favorites.PERIOD_START, AppliteUtilities.date2Millis(context, app_apkstartdate));
            values.put(AppLiteSettings.Favorites.PERIOD_END, AppliteUtilities.date2Millis(context, app_apkenddate));
            values.put(AppLiteSettings.Favorites.DEFAULT_PENDING,app_buildin);
            values.put(AppLiteSettings.Favorites.BUILDIN_PATH, app_buildinpath);
            return values;
        }
        
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getIcon_url() {
            return icon_url;
        }
        public void setIcon_url(String icon_url) {
            this.icon_url = icon_url;
        }
        public String getApp_name() {
            return app_name;
        }
        public void setApp_name(String app_name) {
            this.app_name = app_name;
        }
        public String getDetail_url() {
            return detail_url;
        }
        public void setDetail_url(String detail_url) {
            this.detail_url = detail_url;
        }
        public String getPackage_name() {
            return package_name;
        }
        public void setPackage_name(String package_name) {
            this.package_name = package_name;
        }
        public String getClass_name() {
            return class_name;
        }
        public void setClass_name(String class_name) {
            this.class_name = class_name;
        }
        public String getApk_url() {
            return apk_url;
        }
        public void setApk_url(String apk_url) {
            this.apk_url = apk_url;
        }
        public long getSize() {
            return size;
        }
        public void setSize(long size) {
            this.size = size;
        }
        public int getVersion_code() {
            return version_code;
        }
        public void setVersion_code(int version_code) {
            this.version_code = version_code;
        }
        public String getVersion_name() {
            return version_name;
        }
        public void setVersion_name(String version_name) {
            this.version_name = version_name;
        }
        public int getData_download() {
            return data_download;
        }
        public void setData_download(int data_download) {
            this.data_download = data_download;
        }
        public String getFb_url() {
            return fb_url;
        }
        public void setFb_url(String fb_url) {
            this.fb_url = fb_url;
        }
        public int getItem_group() {
            return item_group;
        }
        public void setItem_group(int item_group) {
            this.item_group = item_group;
        }
        public String getIntroduce_text() {
            return (null == introduce_text)?introduce_text
                        :introduce_text.replace("<br />", "\r\n").replace("<br/>", "\r\n");
        }
        public void setIntroduce_text(String introduce_text) {
            this.introduce_text = introduce_text;
        }
        public String getApp_apkstartdate() {
            return app_apkstartdate;
        }
        public void setApp_apkstartdate(String app_apkstartdate) {
            this.app_apkstartdate = app_apkstartdate;
        }
        public String getApp_apkenddate() {
            return app_apkenddate;
        }
        public void setApp_apkenddate(String app_apkenddate) {
            this.app_apkenddate = app_apkenddate;
        }
        public int getApp_buildin() {
            return app_buildin;
        }
        public void setApp_buildin(int app_buildin) {
            this.app_buildin = app_buildin;
        }
        public String getApp_buildinpath() {
            return app_buildinpath;
        }
        public void setApp_buildinpath(String app_buildinpath) {
            this.app_buildinpath = app_buildinpath;
        }
        public String getIntent() {
            return intent;
        }
        public void setIntent(String intent) {
            this.intent = intent;
        }
        public int getSpanX() {
            return spanX;
        }
        public void setSpanX(int spanX) {
            this.spanX = spanX;
        }
        public int getSpanY() {
            return spanY;
        }
        public void setSpanY(int spanY) {
            this.spanY = spanY;
        }
        public int getCellY() {
            return cellY;
        }
        public void setCellY(int cellY) {
            this.cellY = cellY;
        }
        public int getCellX() {
            return cellX;
        }
        public void setCellX(int cellX) {
            this.cellX = cellX;
        }
        public int getScreen() {
            return screen;
        }
        public void setScreen(int screen) {
            this.screen = screen;
        }
        
        public Intent getRealIntent(){
            if (null != realIntent) return realIntent;
            try{
                if (null == intent || intent.length()<=0){
                    realIntent = new Intent(Intent.ACTION_MAIN);
                    realIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    realIntent.setClassName(package_name, class_name);
                    realIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    intent = realIntent.toUri(0);
                }else{
                    realIntent = Intent.parseUri(intent, 0);
                    realIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return realIntent;
        }

        public void setRealIntent(Intent realIntent) {
            this.realIntent = realIntent;
        }
    }
}
