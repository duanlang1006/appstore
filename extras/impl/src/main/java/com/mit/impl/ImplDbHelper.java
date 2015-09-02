package com.mit.impl;

import android.content.Context;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.SqlInfo;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.OtherUtils;

/**
 * Created by hxd on 15-7-27.
 */
public class ImplDbHelper {
    public final static String TABLE_DOWNLOADINFO = "DownloadInfo";
    public final static String TABLE_IMPLINFO = "ImplInfo";

    private final static String DB_NAME = "impl.db";
    private final static int DB_VERSION = 3;

    static DbUtils db = null;
    public static DbUtils getDbUtils(Context appContext) {
        if (db == null) {
            String dbdir = OtherUtils.getDiskCacheDir(appContext, "databases");
            db = DbUtils.create(appContext,dbdir,DB_NAME,DB_VERSION,new ImplUpgradeListener());
        }
        return db;
    }

    static class ImplUpgradeListener implements  DbUtils.DbUpgradeListener{
        @Override
        public void onUpgrade(DbUtils dbUtils, int oldVersion, int newVersion) {
            ImplLog.d("impl_db","onUpgrade,"+oldVersion+","+newVersion);
            switch(newVersion){
                case 2:
                    try {
                        dbUtils.execNonQuery("Alter table "+TABLE_IMPLINFO+" add column \"cause\" INTEGER default 0");
                    } catch (DbException e) {}
                    try {
                        dbUtils.execNonQuery("Alter table "+TABLE_IMPLINFO+" add column \"size\" INTEGER default 0");
                    } catch (DbException e) {}
                    try {
                        dbUtils.execNonQuery("Alter table "+TABLE_IMPLINFO+" add column \"userContinue\" INTEGER default 0");
                    } catch (DbException e) {}
                    break;
                case 3:
                    try {
                        dbUtils.execNonQuery("Alter table "+TABLE_IMPLINFO+" add column \"md5\" TEXT");
                    } catch (DbException e) {}
                    try {
                        dbUtils.execNonQuery("Alter table "+TABLE_IMPLINFO+" add column \"state\" INTEGER");
                    } catch (DbException e) {}
                    try {
                        dbUtils.execNonQuery("Alter table "+TABLE_IMPLINFO+" add column \"fileSavePath\" TEXT");
                    } catch (DbException e) {}
                    try {
                        dbUtils.execNonQuery("Alter table "+TABLE_IMPLINFO+" add column \"current\" INTEGER");
                    } catch (DbException e) {}
                    try {
                        dbUtils.execNonQuery("Alter table "+TABLE_IMPLINFO+" add column \"total\" INTEGER");
                    } catch (DbException e) {}
                    try {
                        dbUtils.execNonQuery("Alter table "+TABLE_IMPLINFO+" add column \"autoResume\" INTEGER");
                    } catch (DbException e) {}
                    try {
                        dbUtils.execNonQuery("Alter table "+TABLE_IMPLINFO+" add column \"autoRename\" INTEGER");
                    } catch (DbException e) {}
                    break;
            }
        }
    }

}
