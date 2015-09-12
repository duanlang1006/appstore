package com.applite.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by caijian on 15-9-9.
 */

/**
 * 本应用数据清除管理器
 */
public class DataCleanManager {
    /**
     * 清除本应用内部缓存
     *
     * @param context
     */
    public static void cleanInternalCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
    }

    /**
     * 清除本应用外部缓存
     *
     * @param context
     */
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }

    /**
     * 清除本应用内外所有缓存
     *
     * @param context
     */
    public static void cleanAllCache(Context context) {
        cleanInternalCache(context);
        cleanExternalCache(context);
    }

    /**
     * 删除方法:这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理
     *
     * @param directory
     */
    public static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
}



