package com.mit.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hxd on 15-6-18.
 */
public class ImplConfig implements BaseColumns{
    public static final String TABLE_IMPL = "impl";

    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_DOWNLOADID = "downloadId";
    public static final String COLUMN_DOWNLOADURL = "downloadUrl";
    public static final String COLUMN_PACKAGENAME = "packageName";
    public static final String COLUMN_ICON_PATH = "iconPath";
    public static final String COLUMN_ICON_URL = "iconUrl";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TOTAL_BYTES = "totalBytes";
    public static final String COLUMN_CURRENT_BYTES = "currentByts";
    public static final String COLUMN_LOCALURI = "localUri";
    public static final String COLUMN_MIMETYPE = "mimeType";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_REASON = "reason";
    public static final String COLUMN_LAST_MODIFIED_TIMESTAMP = "timestamp";


    public static String statusClause(String operator, int value) {
        return COLUMN_STATUS + operator + "'" + value + "'";
    }

    public static String joinStrings(String joiner, Iterable<String> parts) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String part : parts) {
            if (!first) {
                builder.append(joiner);
            }
            builder.append(part);
            first = false;
        }
        return builder.toString();
    }

    public static ImplInfo findInfoByDownloadId(ImplDatabaseHelper dbHelper,long id){
        ImplInfo info = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select * from "+ImplConfig.TABLE_IMPL+" where "+ImplConfig.COLUMN_DOWNLOADID + " = " + id;
        Cursor c = db.rawQuery(sql,null);
        try{
            c.moveToFirst();
            info = ImplInfo.from(c);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (null != c){
                c.close();
            }
        }
        return info;
    }

    public static ImplInfo findInfoByPackageName(ImplDatabaseHelper dbHelper,String pkgName){
        ImplInfo info = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select * from "+ImplConfig.TABLE_IMPL+" where "+ImplConfig.COLUMN_PACKAGENAME + " = " + pkgName;
        Cursor c = db.rawQuery(sql,null);
        try{
            c.moveToFirst();
            info = ImplInfo.from(c);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (null != c){
                c.close();
            }
        }
        return info;
    }

    public static ImplInfo findInfoByKey(ImplDatabaseHelper dbHelper,String key){
        ImplInfo info = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select * from "+ImplConfig.TABLE_IMPL+" where "+ImplConfig.COLUMN_KEY + " = " + key;
        Cursor c = db.rawQuery(sql,null);
        try{
            c.moveToFirst();
            info = ImplInfo.from(c);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (null != c){
                c.close();
            }
        }
        return info;
    }

    public static List<ImplInfo> findInfoByKeyBatch(ImplDatabaseHelper dbHelper,String[] keys){
        List<ImplInfo> infoList = new ArrayList<ImplInfo>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(ImplConfig.TABLE_IMPL, null, getWhereClauseForKeys(keys), getWhereArgsForKeys(keys), null, null, null);
        try{
            c.moveToFirst();
            do {
                ImplInfo info = ImplInfo.from(c);
                if (null != info){
                    infoList.add(info);
                }
            }while(c.moveToNext());
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (null != c){
                c.close();
            }
        }
        return infoList;
    }

    static String getWhereClauseForKeys(String[] keys) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) {
                whereClause.append("OR ");
            }
            whereClause.append(ImplConfig.COLUMN_KEY);
            whereClause.append(" = '?' ");
        }
        whereClause.append(")");
        return whereClause.toString();
    }

    static String[] getWhereArgsForKeys(String[] keys) {
        String[] whereArgs = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            whereArgs[i] = keys[i];
        }
        return whereArgs;
    }
}
