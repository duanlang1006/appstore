package com.mit.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.SqlInfo;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.OtherUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hxd on 15-7-27.
 */
public class ImplDbHelper {
    public final static String TABLE_DOWNLOADINFO = "DownloadInfo";
    public final static String TABLE_IMPLINFO = "ImplInfo";

    private final static String DB_NAME = "impl.db";
    private final static int DB_VERSION = 4;

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
            for (int i = oldVersion+1; i <= newVersion;i++) {
                switch (i) {
                    case 2:
                        try {
                            dbUtils.execNonQuery("Alter table " + TABLE_IMPLINFO + " add column \"cause\" INTEGER default 0");
                        } catch (DbException e) {
                        }
                        try {
                            dbUtils.execNonQuery("Alter table " + TABLE_IMPLINFO + " add column \"size\" INTEGER default 0");
                        } catch (DbException e) {
                        }
                        try {
                            dbUtils.execNonQuery("Alter table " + TABLE_IMPLINFO + " add column \"userContinue\" INTEGER default 0");
                        } catch (DbException e) {
                        }
                        break;
                    case 3:
                        try {
                            dbUtils.execNonQuery("Alter table " + TABLE_IMPLINFO + " add column \"md5\" TEXT");
                        } catch (DbException e) {
                        }
                        try {
                            dbUtils.execNonQuery("Alter table " + TABLE_IMPLINFO + " add column \"state\" INTEGER");
                        } catch (DbException e) {
                        }
                        try {
                            dbUtils.execNonQuery("Alter table " + TABLE_IMPLINFO + " add column \"fileSavePath\" TEXT");
                        } catch (DbException e) {
                        }
                        try {
                            dbUtils.execNonQuery("Alter table " + TABLE_IMPLINFO + " add column \"current\" INTEGER");
                        } catch (DbException e) {
                        }
                        try {
                            dbUtils.execNonQuery("Alter table " + TABLE_IMPLINFO + " add column \"total\" INTEGER");
                        } catch (DbException e) {
                        }
                        try {
                            dbUtils.execNonQuery("Alter table " + TABLE_IMPLINFO + " add column \"autoResume\" INTEGER");
                        } catch (DbException e) {
                        }
                        try {
                            dbUtils.execNonQuery("Alter table " + TABLE_IMPLINFO + " add column \"autoRename\" INTEGER");
                        } catch (DbException e) {
                        }
                        break;

                    case 4:
                        Cursor c = null;
                        try {
                            c = dbUtils.execQuery("select id,state,fileSavePath,progress,fileLength,autoResume,autoRename from DownloadInfo");
                            if (null != c && c.getCount() > 0 && c.moveToFirst()) {
                                dbUtils.getDatabase().beginTransaction();
                                do {
                                    long id = c.getLong(c.getColumnIndex("id"));
                                    int state = c.getInt(c.getColumnIndex("state"));
                                    String fileSavePath = c.getString(c.getColumnIndex("fileSavePath"));
                                    long progress = c.getLong(c.getColumnIndex("progress"));
                                    long fileLength = c.getLong(c.getColumnIndex("fileLength"));
                                    int autoResume = c.getInt(c.getColumnIndex("autoResume"));
                                    int autoRename = c.getInt(c.getColumnIndex("autoRename"));
                                    dbUtils.execNonQuery("update " + TABLE_IMPLINFO + " set state=" + state + " where downloadId=" + id);
                                    dbUtils.execNonQuery("update " + TABLE_IMPLINFO + " set fileSavePath=\"" + fileSavePath + "\" where downloadId=" + id);
                                    dbUtils.execNonQuery("update " + TABLE_IMPLINFO + " set current=" + progress + " where downloadId=" + id);
                                    dbUtils.execNonQuery("update " + TABLE_IMPLINFO + " set total=" + fileLength + " where downloadId=" + id);
                                    dbUtils.execNonQuery("update " + TABLE_IMPLINFO + " set autoResume=" + autoResume + " where downloadId=" + id);
                                    dbUtils.execNonQuery("update " + TABLE_IMPLINFO + " set autoRename=" + autoRename + " where downloadId=" + id);
                                } while (c.moveToNext());
                                dbUtils.getDatabase().setTransactionSuccessful();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            dbUtils.getDatabase().endTransaction();
                            if (null != c) {
                                c.close();
                            }
                        }
                        break;
                }
            }
        }


        private static void updateDb(DbUtils db, String tableName) {
            try {
                Class<ImplInfo> c = (Class<ImplInfo>) Class.forName("com.mit.impl." + tableName);// 把要使用的类加载到内存中,并且把有关这个类的所有信息都存放到对象c中
                if (db.tableIsExist(c)) {
                    List<String> dbFildsList = new ArrayList<String>();
                    String str = "select * from " + tableName;
                    Cursor cursor = db.execQuery(str);
                    int count = cursor.getColumnCount();
                    for (int i = 0; i < count; i++) {
                        dbFildsList.add(cursor.getColumnName(i));
                    }
                    cursor.close();
                    Field f[] = c.getDeclaredFields();// 把属性的信息提取出来，并且存放到field类的对象中，因为每个field的对象只能存放一个属性的信息所以要用数组去接收
                    for (int i = 0; i < f.length; i++) {
                        String fildName = f[i].getName();
                        if (fildName.startsWith("STATUS_")
                                || fildName.startsWith("ACTION_")
                                || fildName.startsWith("CAUSE_")
                                || fildName.equals("INSTALL_SUCCEEDED")
                                || fildName.equals("DELETE_SUCCEEDED")) {
                            continue;
                        }
                        String fildType = f[i].getType().toString();
                        if (!isExist(dbFildsList, fildName)) {
                            if (fildType.equals("class java.lang.String")) {
                                db.execNonQuery("alter table " + tableName + " add " + fildName + " TEXT ");
                            } else if (fildType.equals("int") || fildType.equals("long") || fildType.equals("boolean")) {
                                db.execNonQuery("alter table " + tableName + " add " + fildName + " INTEGER ");
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        private static boolean isExist(List<String> list,String value){
            for (int i =0 ;i < list.size(); i ++ ){
                if (list.get(i).equals(value)){
                    return true;
                }
            }
            return false;
        }
    }

}
