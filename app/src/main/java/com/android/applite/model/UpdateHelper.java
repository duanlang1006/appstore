package com.android.applite.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;

import com.applite.util.AppliteConfig;
import com.applite.util.AppliteUtilities;
import com.applite.util.DscSettings;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

interface UpdateHelperCallback{
    void result(UpdateHelper helper,Object obj);
}

public class UpdateHelper {
    private static final String TAG = "AppLite_UpdateHelper";

    private boolean mRequesting = false;
    private UpdateHelperCallback mUpdateCallback;
    private MoEntityBase mEntity = null;
    
	private static final int UIDSTRING_LENGTH = 32;
	private final static String productRootPath = "/productinfo/";
	private final static String uuidFileName = "data_uuid";
	private final static String UNDERLINE = "_";
    
    public boolean updateImpl(Context context,String url,int category,String detail,
            UpdateHelperCallback callback){
        if (null == url || url.length() <1) return false;
        
        synchronized (this) {
            if (mRequesting){
                return false;
            }
        }

        mUpdateCallback = callback;
        
        if (null == mEntity){
            mEntity = new MoEntityBase("APPLITE_UPDATE");
        }
        String userid = AppliteConfig.getUserId(context);
        String uuid = AppliteConfig.getUUID(context);
        if (0 == userid.length() || 0 == uuid.length()){
            Cursor c = null;
            try{
                c = context.getContentResolver().query(DscSettings.CONTENT_CONFIG_URI, null, null, null, null);
                if (c!=null && c.moveToFirst()) {
                    if (0 == userid.length()){
                        userid = getAssignedUserId(context, c);
                        AppliteConfig.setUserId(context, userid);
                    }
                    if (0 == uuid.length()){
                        uuid = getDeviceUUID(context,c);
                        AppliteConfig.setUUID(context, uuid);
                    }
        		}
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if (null != c){
                    c.close();
                }
            }
        }
        mEntity.setUuid(uuid);
        mEntity.setUserId(userid);
        mEntity.setCategory(category);
        mEntity.setDetail(detail);
        mEntity.setVersionName(AppliteUtilities.getSoftversion(context));
        mEntity.setIpAddress(getIpAddress());
        mEntity.setDeviceName(getDeviceName());
        mEntity.setDeviceSwVersion(getDeviceSwVersion());
        try {
            HttpEntity httpEntity = new UrlEncodedFormEntity(mEntity.toNameValuePair(),HTTP.UTF_8);
            if (null != httpEntity){
                FinalHttp finalHttp = new FinalHttp();
                finalHttp.configTimeout(60*1000);
                finalHttp.post(url,httpEntity,
                        "application/x-www-form-urlencoded; charset=utf-8",new AjaxCallBack<Object>() {
                    @Override
                    public void onFailure(Throwable t, int errorNo, String strMsg) {
                        // TODO Auto-generated method stub
                        if (null != mUpdateCallback){
                            mUpdateCallback.result(UpdateHelper.this,null);
                        }
                        synchronized (this) {
                            mRequesting = false;                            
                        }
                        super.onFailure(t, errorNo, strMsg);
                    }

                    @Override
                    public void onSuccess(Object t) {
                        // TODO Auto-generated method stub
//                        String jsonStr = EntityUtils.toString(entity);
                        synchronized (this) {
                            mRequesting = false;                            
                        }
                        if (null != mUpdateCallback) {
                            mUpdateCallback.result(UpdateHelper.this, (String)t);
                        }
                        super.onSuccess(t);
                    }
                });
            }
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void cancelUpdate(Context context){
        mUpdateCallback = null;
        synchronized (this) {
            mRequesting = false;
        }
    }
    public String getDeviceUUID(Context context,Cursor c) {
		String uuidStr = null;
		uuidStr = readUUIDFile(context,c);
		if (!isUUIDValid(uuidStr)) {
			uuidStr = UUID.randomUUID().toString().replace("-", "");
		}
		return uuidStr;
	}



	private String getAssignedUserId(Context context,Cursor c) {
		String userid = null;

		try {
			if (null != c && c.moveToFirst()) {
				userid = c.getString(c.getColumnIndex(DscSettings.Config.USERID));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userid;
	}

	private String readUUIDFile(Context context,Cursor c) {
		String path = null;
		String uuid = null;
		path = productRootPath;
		if (!path.endsWith("/")) {
			path += "/";
		}
		path += uuidFileName;
		uuid = readFileImpl(path);

		if (!isUUIDValid(uuid)) {
			path = android.os.Environment.getExternalStorageDirectory().getPath();
			if (!path.endsWith("/")) {
				path += "/";
			}
			path += IAppInfo.extenStorageDirPath;
			if (!path.endsWith("/")) {
				path += "/";
			}
			path += uuidFileName;
			uuid = readFileImpl(path);
		}
		if (!isUUIDValid(uuid)) {
			uuid = null;
			
			try {
				if (null != c && c.moveToFirst()) {
					uuid = c.getString(c.getColumnIndex(DscSettings.Config.UUID));
					if (!isUUIDValid(uuid)) {
						uuid = null;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return uuid;
	}

	private String readFileImpl(String filePath) {
		String str = null;
		try {
			File readFile = new File(filePath);
			if (readFile.exists()) {
				FileInputStream inStream = new FileInputStream(readFile);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				byte[] buffer = new byte[512];
				int length = -1;
				while ((length = inStream.read(buffer)) != -1) {
					stream.write(buffer, 0, length);
				}
				str = stream.toString();
				stream.close();
				inStream.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}

	private boolean isUUIDValid(String uuid) {
		if (null == uuid || uuid.length() < UIDSTRING_LENGTH) {
			return false;
		} else {
			return true;
		}
	}

	private String getDeviceSwVersion() {
	    String result = "";
	    try{
    	    Class<?> cl = Class.forName("android.os.SystemProperties");
            Object invoker = cl.newInstance();
            Method m = cl.getMethod("get", String.class);
            result = (String)m.invoke(invoker, "ro.product.ly.inward.version");
	    }catch(Exception e ){
	        e.printStackTrace();
	    }
	    return Build.VERSION.INCREMENTAL + "|" + result;
	}

	private String getDeviceName() {
		String name = Build.BRAND + UNDERLINE + Build.DEVICE + UNDERLINE + Build.DISPLAY;
	    return name;
	}

	private String getIpAddress() {
		String ipAddress = "";
		if (null == ipAddress || ipAddress.length() == 0) {
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf	.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							ipAddress = inetAddress.getHostAddress().toString();
						}
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		return ipAddress;
	}
}
