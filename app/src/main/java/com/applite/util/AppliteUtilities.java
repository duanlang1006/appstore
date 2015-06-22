package com.applite.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppliteUtilities {
    private static String softVersion = null; 
    
    
    public static String  getSoftversion(Context context){
        if (null == softVersion || softVersion.length() == 0){
            PackageManager manager = context.getPackageManager();
            try {
                PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                softVersion = info.versionName; 
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return softVersion;
    }

    public static String getDeviceInfo(Context context) {
        try{
          org.json.JSONObject json = new org.json.JSONObject();
          android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
              .getSystemService(Context.TELEPHONY_SERVICE);
      
          String device_id = tm.getDeviceId();
          
          android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
              
          String mac = wifi.getConnectionInfo().getMacAddress();
          json.put("mac", mac);
          
          if( TextUtils.isEmpty(device_id) ){
            device_id = mac;
          }
          
          if( TextUtils.isEmpty(device_id) ){
            device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
          }
          
          json.put("device_id", device_id);
          
          return json.toString();
        }catch(Exception e){
          e.printStackTrace();
        }
      return null;
    }
    
    public static String getFilenameFromUrl(String fileUrl) {
        String fileName = " /";
        if (fileUrl != null) {
            fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        }
        return fileName;
    }
    
    public static String millis2DateStr(Context context, Long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd", Locale.getDefault());
        Date ts = new Date(millis);
        String str = sdf.format(ts);
        return str;
    }
    
    public static long date2Millis(Context context, String date) {
        long millis = 0;
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd", Locale.getDefault());
        try{
            Date dt = sdf.parse(date);
            millis = dt.getTime();
        }catch(Exception e){
            
        }
        return millis;
    }
                      
}
