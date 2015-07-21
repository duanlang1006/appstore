package com.mit.impl;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hxd on 15-6-10.
 */
public abstract class AbstractImpl implements ImplInterface{
    static ImplDatabaseHelper databaseHelper;

    @Override
    public boolean request(ImplAgent.ImplRequest cmd) {
        if (null == databaseHelper){
            databaseHelper = new ImplDatabaseHelper(cmd.context);
        }
        return false;
    }

    @Override
    public void cancel(ImplAgent.ImplRequest cmd) {

    }

    void save(ImplInfo info){
        ImplInfo infoIndb = findInfoByKey(info.getKey());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        if (null != infoIndb){
            db.update(ImplConfig.TABLE_IMPL,
                    info.getContentValues(),
                    ImplConfig.COLUMN_KEY+"=?",
                    new String[]{info.getKey()});
        }else {
            db.insert(ImplConfig.TABLE_IMPL,null,info.getContentValues());
        }
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


    ImplInfo findInfoByDownloadId(long id){
        ImplInfo info = null;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor c = null;
        try{
            c = db.query(ImplConfig.TABLE_IMPL,
                    null,
                    ImplConfig.COLUMN_DOWNLOADID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,null,null);
            if (null != c && c.getCount() > 0 && c.moveToFirst()) {
                info = ImplInfo.from(c);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (null != c){
                c.close();
            }
        }
        return info;
    }

    ImplInfo findInfoByPackageName(String pkgName){
        ImplInfo info = null;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor c = null;
        try{
            c = db.query(ImplConfig.TABLE_IMPL,
                    null,
                    ImplConfig.COLUMN_PACKAGENAME + " = ?",
                    new String[]{pkgName},
                    null,null,null);
            if (null != c && c.getCount() > 0 && c.moveToFirst()) {
                info = ImplInfo.from(c);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (null != c){
                c.close();
            }
        }
        return info;
    }

    ImplInfo findInfoByKey(String key){
        ImplInfo info = null;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor c = null;
        try{
            c = db.query(ImplConfig.TABLE_IMPL,
                    null,
                    ImplConfig.COLUMN_KEY + " = ?",
                    new String[]{key},
                    null, null, null);
            if (null != c && c.getCount() > 0 && c.moveToFirst()) {
                info = ImplInfo.from(c);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (null != c){
                c.close();
            }
        }
        return info;
    }

    List<ImplInfo> findInfoByKeyBatch(String[] keys){
        List<ImplInfo> infoList = new ArrayList<ImplInfo>();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String clause = getWhereClauseForKeys(keys);
        String[] args = getWhereArgsForKeys(keys);
        Cursor c = null;
        try{
            c = db.query(ImplConfig.TABLE_IMPL, null, clause, args, null, null, null);
            if (null != c && c.getCount() > 0 && c.moveToFirst()) {
                do {
                    ImplInfo info = ImplInfo.from(c);
                    if (null != info) {
                        infoList.add(info);
                    }
                } while (c.moveToNext());
            }
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
            whereClause.append(" = ? ");
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
