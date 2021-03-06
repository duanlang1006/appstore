package com.applite.common;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.applite.sharedpreferences.AppliteSPUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LSY on 15-6-24.
 */
public class AppliteUtils {

    private static final String TAG = "AppliteUtils";
    public static String regEx = "[^aoeiuv]?h?[iuv]?(ai|ei|ao|ou|er|ang?|eng?|ong|a|o|e|i|u|ng|n)?";

    /**
     * 得到AndroidManifest.xml里面MetaData的Value值
     *
     * @param context
     * @return
     */
    public static String getMitMetaDataValue(Context context, String name) {
        ApplicationInfo appInfo;
        String MetaDataValue = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            MetaDataValue = appInfo.metaData.getString(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != MetaDataValue) {
            LogUtils.i(TAG, MetaDataValue);
        }
        return MetaDataValue;
    }

    /**
     * byte(字节)根据长度转成kb(千字节)和mb(兆字节)
     *
     * @param bytes
     * @return
     */
    public static String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1)
            return (returnValue + "MB");
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue + "KB");
    }

    /**
     * 判断该应用在手机中的安装情况
     *
     * @param context
     * @param packageName 要判断应用的包名
     * @param versionCode 要判断应用的版本名称
     */
    public static int isAppInstalled(Context context, String packageName, int versionCode) {
        PackageManager pm = context.getPackageManager();
        try {
            LogUtils.i(TAG, packageName);
            PackageInfo pakageinfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (versionCode == pakageinfo.versionCode) {
                LogUtils.i(TAG, "已经安装，不用更新，可以卸载该应用");
                return Constant.INSTALLED;
            } else if (versionCode > pakageinfo.versionCode) {
                LogUtils.i(TAG, "已经安装，有更新");
                return Constant.INSTALLED_UPDATE;
            } else if (versionCode < pakageinfo.versionCode) {
                LogUtils.i(TAG, "已经安装，本地版本较新，服务器需更新");
                return Constant.INSTALLED;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            LogUtils.i(TAG, "未安装该应用，可以安装");
            return Constant.UNINSTALLED;
        }
        return Constant.UNINSTALLED;
    }

    /**
     * 已安装包 直接启动
     *
     * @param context
     * @param packageName
     */
    public static void startApp(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 得到手机里面所有的APK信息
     *
     * @param context
     * @return
     */
    public static String getAllApkData(Context context) {
        JSONArray array = new JSONArray();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> pakageinfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo pi : pakageinfos) {
            try {
                JSONObject object = new JSONObject();
                String pi_packageName = pi.packageName;
                int pi_versionCode = pi.versionCode;
                object.put("apk_packagename", pi_packageName);
                object.put("apk_versioncode", pi_versionCode);
                array.put(object);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.i(TAG, "更新管理得到pakageinfos的JSON数据出错");
            }
        }
        LogUtils.i(TAG, "更新管理得到pakageinfos的JSON数据:" + array.toString());
        return array.toString();
    }

    /**
     * @param infos
     * @return
     */
    public static String encodePackages(List<PackageInfo> infos) {
        JSONArray array = new JSONArray();
        for (PackageInfo pi : infos) {
            try {
                JSONObject object = new JSONObject();
                String pi_packageName = pi.packageName;
                int pi_versionCode = pi.versionCode;
                object.put("apk_packagename", pi_packageName);
                object.put("apk_versioncode", pi_versionCode);
                array.put(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return array.toString();
    }


    /**
     * 设置文字
     *
     * @param mContext
     * @param id
     */
    public static String getString(Context mContext, int id) {
        return mContext.getResources().getString(id);
    }

    /**
     * 获取控件宽
     */
    public static int getWidth(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return (view.getMeasuredWidth());
    }

    /**
     * 获取控件高
     */
    public static int getHeight(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return (view.getMeasuredHeight());
    }

    /**
     * 设置控件所在的位置XY，并且不改变宽高， XY为绝对位置
     */
    public static void setLayout(View view, int x, int y) {
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(
                view.getLayoutParams());
        margin.setMargins(x, y, x + margin.width, y + margin.height);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                margin);
        view.setLayoutParams(layoutParams);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 保存路径
     *
     * @param filename
     * @return
     */
    public static String getAppDir(String filename) {
        String path = "";
        String state = android.os.Environment.getExternalStorageState();
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)
                && android.os.Environment.getExternalStorageDirectory().canWrite()) {
            path = android.os.Environment.getExternalStorageDirectory().getPath();
            if (!path.endsWith("/")) {
                path += "/";
            }
            path += Constant.PATH;
            if (!path.endsWith("/")) {
                path += "/";
            }
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        path += filename;
        return path;
    }

    /**
     * 判断APK包是否完整
     *
     * @param context
     * @param path
     * @return
     */
    public static boolean isPackageOk(Context context, String path) {
        return context.getPackageManager().getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES) != null;
    }

    /**
     * 加载本地图片
     *
     * @param path
     * @return
     */
    public static Bitmap getLoacalBitmap(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 是否加载网络上的图片
     *
     * @param mContext
     * @return
     */
    public static boolean isLoadNetworkBitmap(Context mContext) {
        boolean noPic = (boolean) AppliteSPUtils.get(mContext, AppliteSPUtils.NO_PICTURE, DefaultValue.defaultValueNoPic);
        if (Constant.MOBILE.equals(AppliteConfig.getNetwork(mContext)) && noPic)
            return false;
        return true;
    }

    /**
     * 删除文件
     *
     * @param fileName
     */
    public static void delFile(String fileName) {
        File file = new File(fileName);
        if (file.isFile()) {
            file.delete();
        }
        file.exists();
    }

    /**
     * 判断文件夹下是否有这个文件名
     *
     * @param PathName
     * @return
     */
    public static boolean fileIsExists(String PathName) {
        boolean ret = false;
        try {
            File f = new File(PathName);
            if (f.exists()) {
                ret = true;
            }
        } catch (Exception e) {
        }
        return ret;
    }

    /**
     * 分割字母
     *
     * @return
     */
    public static String SplitLetter(String letter) {
        String defaultLetter = letter;
        int tag = 0;
        String s = "";//分割后的结果
        int noOne = 0;//不为0时返回分割后的字母
        List<String> tokenResult = new LinkedList<String>();
        for (int i = letter.length(); i > 0; i = i - tag) {
            Pattern pat = Pattern.compile(regEx);
            Matcher matcher = pat.matcher(letter);
            boolean rs = matcher.find();
            LogUtils.i(TAG, "matcher.group():" + matcher.group());

            if (matcher.group().length() != 1) {
                noOne = noOne + 1;
            }

            s = s + matcher.group() + " ";
            System.out.println(matcher.group());
            tag = matcher.end() - matcher.start();
            tokenResult.add(letter.substring(0, 1));
            letter = letter.substring(tag);
        }
        LogUtils.i(TAG, "是否分割字母:" + noOne);
        if (noOne <= 1) {
            return defaultLetter;
        } else {
            return s;
        }
    }

//    public static OSGIServiceClient getClientOSGIService(BundleContext bundleContext, String serviceName) {
//        OSGIServiceClient service = null;
//        try {
//            service = new OSGIServiceAgent<OSGIServiceClient>(
//                    bundleContext, OSGIServiceClient.class,
//                    "(serviceName=" + serviceName + ")", //服务查询条件
//                    OSGIServiceAgent.real_time).getService();   //每次都重新查询
//        } catch (Exception e) {
//            // TODO 自动生成的 catch 块
//            e.printStackTrace();
//        }
//        return service;
//    }

//    public static Bundle putFgParams(Bundle params, String fromTag, String operate, boolean addToBackStack) {
//        params.putString("fromTag", fromTag);
//        params.putString("operate", operate);
//        params.putBoolean("addToBackStack", addToBackStack);
//        return params;
//    }
}
