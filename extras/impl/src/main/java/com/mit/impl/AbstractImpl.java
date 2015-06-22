package com.mit.impl;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;

import com.android.dsc.downloads.Downloads;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by hxd on 15-6-10.
 */
public abstract class AbstractImpl implements ImplInterface{
//    Gson gson = null;
//    SharedPreferences sp = null;
    ImplDatabaseHelper databaseHelper;

//    @Override
//    public void init(Context context) {
//        mHandler = new Handler();
//        gson = new Gson();
//        sp = context.getSharedPreferences("impl",Context.MODE_PRIVATE);
//    }

    @Override
    public boolean request(ImplAgent.ImplRequest cmd) {
//        if (null == gson){
//            gson = new Gson();
//        }

        if (null == databaseHelper){
            databaseHelper = new ImplDatabaseHelper(cmd.context);
        }
//        if (null == sp){
//            sp = cmd.context.getSharedPreferences("impl",Context.MODE_PRIVATE);
//        }
        return false;
    }

    @Override
    public void cancel(ImplAgent.ImplRequest cmd) {

    }

//    void save(String key,long downloadId,String packageName,
//              String iconDir,String iconUrl,
//              String url,
//              String title,String desc){
//        SQLiteDatabase db = databaseHelper.getWritableDatabase();
//        ImplInfo info = findInfoByKey(key);
//        if (null != info){
//            info.setKey(key);
//            info.setDownloadId(downloadId);
//            info.setDownloadUrl(url);
//            info.setPackageName(packageName);
//            info.setIconPath(iconDir);
//            info.setIconUrl(iconUrl);
//            info.setTitle(title);
//            info.setDescription(desc);
//            db.update(ImplConfig.TABLE_IMPL,
//                    info.getContentValues(),
//                    ImplConfig.KEY+"=?",
//                    new String[]{String.valueOf(key.hashCode())});
//        }else {
//            info = new ImplInfo(key,url,downloadId,packageName,iconDir,iconUrl,title,desc);
//            db.insert(ImplConfig.TABLE_IMPL,null,info.getContentValues());
//        }
////        sp.edit().putString(key,info.toJson(gson)).apply();
//    }

    void save(ImplInfo info){
        ImplInfo infoIndb = ImplConfig.findInfoByKey(databaseHelper,info.getKey());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        if (null != infoIndb){
            db.update(ImplConfig.TABLE_IMPL,
                    info.getContentValues(),
                    ImplConfig.COLUMN_KEY+"=?",
                    new String[]{info.getKey()});
        }else {
            db.insert(ImplConfig.TABLE_IMPL,null,info.getContentValues());
        }
//        sp.edit().putString(info.getKey(),info.toJson(gson)).apply();
    }

    void update(ImplInfo info){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.update(ImplConfig.TABLE_IMPL,
                info.getContentValues(),
                ImplConfig.COLUMN_KEY+"=?",
                new String[]{info.getKey()});
    }

    void remove(String key){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(ImplConfig.TABLE_IMPL,
                ImplConfig.COLUMN_KEY+"=?",
                new String[]{key});
//        sp.edit().remove(key).apply();
    }
}
