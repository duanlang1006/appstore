package com.applite.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AppLiteSpUtils {

	private static final String APPLITE_NETWORK_CONFIG = "applite_network_config";
	private static final String KEY_UPDATE_APP_TIME = "update_app_time";//APP更新请求时间
	
	private static final String KEY_UPDATE_DATA_TIME = "update_data_time";//数据统计发送时间
	private static final String KEY_DATA_ZJBB = "data_zjbb";//装机必备
	private static final String KEY_DATA_YLZX = "data_ylzx";//娱乐中心
	private static final String KEY_DATA_DOWNLOAD = "data_download";//	下载
	private static final String KEY_DATA_TIME_OUT = "data_time_out";//	暂停
	private static final String KEY_DATA_CARRY_ON = "data_carry_on";//	继续
	private static final String KEY_DATA_DOWNLOAD_SUCCESS = "data_download_success";//	下载成功
	private static final String KEY_DATA_INSTALL_SUCCESS = "data_install_success";//	安装成功
	private static final String KEY_DATA_UNINSTALL = "data_uninstall";//	卸载
	private static final String KEY_DATA_RUN_NUMBER = "data_run_number";//	运行次数
	
	public static SharedPreferences getSharedPreferences(Context context, String prefsName) {
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }
	
	public static void setUpdateAppTime(Context context , Long time){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putLong(KEY_UPDATE_APP_TIME, time);
		editor.commit();
    }
    
    public static long getUpdateAppTime(Context context){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
    	return sp.getLong(KEY_UPDATE_APP_TIME, 0);
    }
    
    public static void setUpdateDataTime(Context context , Long time){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putLong(KEY_UPDATE_DATA_TIME, time);
		editor.commit();
    }
    
    public static long getUpdateDataTime(Context context){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
    	return sp.getLong(KEY_UPDATE_DATA_TIME, 0);
    }
    
    public static void setDataZJBB(Context context , int number){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putInt(KEY_DATA_ZJBB, number);
		editor.commit();
    }
    
    public static int getDataZJBB(Context context){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
    	return sp.getInt(KEY_DATA_ZJBB, 0);
    }
    
    public static void setDataYLZX(Context context , int number){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putInt(KEY_DATA_YLZX, number);
		editor.commit();
    }
    
    public static int getDataYLZX(Context context){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
    	return sp.getInt(KEY_DATA_YLZX, 0);
    }
    
    public static void setDataDownload(Context context , int number){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putInt(KEY_DATA_DOWNLOAD, number);
		editor.commit();
    }
    
    public static int getDataDownload(Context context){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
    	return sp.getInt(KEY_DATA_DOWNLOAD, 0);
    }
    
    public static void setDataTimeOut(Context context , int number){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putInt(KEY_DATA_TIME_OUT, number);
		editor.commit();
    }
    
    public static int getDataTimeOut(Context context){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
    	return sp.getInt(KEY_DATA_TIME_OUT, 0);
    }
    
    public static void setDataCarryOn(Context context , int number){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putInt(KEY_DATA_CARRY_ON, number);
		editor.commit();
    }
    
    public static int getDataCarryOn(Context context){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
    	return sp.getInt(KEY_DATA_CARRY_ON, 0);
    }
    
    public static void setDataDownloadSuccess(Context context , int number){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putInt(KEY_DATA_DOWNLOAD_SUCCESS, number);
		editor.commit();
    }
    
    public static int getDataDownloadSuccess(Context context){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
    	return sp.getInt(KEY_DATA_DOWNLOAD_SUCCESS, 0);
    }
    
    public static void setDataInstallSuccess(Context context , int number){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putInt(KEY_DATA_INSTALL_SUCCESS, number);
		editor.commit();
    }
    
    public static int getDataInstallSuccess(Context context){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
    	return sp.getInt(KEY_DATA_INSTALL_SUCCESS, 0);
    }
    
    public static void setDataUninstall(Context context , int number){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putInt(KEY_DATA_UNINSTALL, number);
		editor.commit();
    }
    
    public static int getDataUninstall(Context context){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
    	return sp.getInt(KEY_DATA_UNINSTALL, 0);
    }
    
    public static void setDataRunNumber(Context context , int number){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putInt(KEY_DATA_RUN_NUMBER, number);
		editor.commit();
    }
    
    public static int getDataRunNumber(Context context){
    	SharedPreferences sp = AppLiteSpUtils.getSharedPreferences(context,AppLiteSpUtils.APPLITE_NETWORK_CONFIG);
    	return sp.getInt(KEY_DATA_RUN_NUMBER, 0);
    }
	
}
