package com.mit.mitupdatesdk;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.mit.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by LSY on 15-6-3.
 */
public class DscInfo {

    private static final String TAG = "DscInfo";
    private static final int UIDSTRING_LENGTH = 32;
    private final static String productRootPath = "/productinfo/";
    private final static String uuidFileName = "data_uuid";
    public final static String extenStorageDirPath = ".android/";
    public static final String MUUID = "uuid";
    public static final String MUSERID = "user_id";

    static final String AUTHORITY = "com.android.dsc.settings";
    public static final Uri CONTENT_CONFIG_URI = Uri.parse("content://" + AUTHORITY + "/config");

    /**
     * 百度服务的UUID  USERID
     *
     * @return
     */
    public static String getDscInfoStr(Context context) {
        String userid = null;
        String uuid = null;
        Cursor c = null;
        try {
            c = context.getContentResolver().query(CONTENT_CONFIG_URI, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                userid = getBdUserId(context, c);
                uuid = getDeviceUUID(context, c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != c) {
                c.close();
            }
        }

        JSONObject dsc_info = new JSONObject();
        try {
            dsc_info.put("dsc_uuid", uuid);
            dsc_info.put("bduserid", userid);
            LogUtils.i(TAG, "百度服务的信息:" + dsc_info);
            return dsc_info.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "dsc_info-JSON异常");
        }
        return null;
    }

    public static String getDeviceUUID(Context context, Cursor c) {
        String uuidStr = null;
        uuidStr = readUUIDFile(context, c);
        if (!isUUIDValid(uuidStr)) {
            uuidStr = UUID.randomUUID().toString().replace("-", "");
        }
        return uuidStr;
    }

    private static String readUUIDFile(Context context, Cursor c) {
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
            path += extenStorageDirPath;
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
                    uuid = c.getString(c.getColumnIndex(MUUID));
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

    private static String getBdUserId(Context context, Cursor c) {
        String userid = null;

        try {
            if (null != c && c.moveToFirst()) {
                userid = c.getString(c.getColumnIndex(MUSERID));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userid;
    }

    private static String readFileImpl(String filePath) {
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

    private static boolean isUUIDValid(String uuid) {
        if (null == uuid || uuid.length() < UIDSTRING_LENGTH) {
            return false;
        } else {
            return true;
        }
    }

}
