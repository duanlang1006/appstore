package com.mit.impl;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
* Created by hxd on 15-6-11.
*/
public class ImplDatabaseHelper extends SQLiteOpenHelper {
    private static final boolean LOGD = true;
    private static final String TAG = "ImplDatabase";
    private static final String DATABASE_NAME = "impl.db";
    private static final int DATABASE_VERSION = 1;

    private final Context mContext;

    public ImplDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (LOGD) Log.d(TAG, "creating new launcher database");
        onUpgrade(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (LOGD) Log.d(TAG, "onUpgrade triggered");
        int version = oldVersion + 1;
        if (oldVersion < 1){
            version = 1;
        }

        if (LOGD) Log.d(TAG, "onUpgrade triggered,"+version+"->"+newVersion);
        for (; version <= newVersion; version++) {
            upgradeTo(db, version);
        }
    }

    private void createImplTable(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + ImplConfig.TABLE_IMPL);
            db.execSQL("CREATE TABLE " + ImplConfig.TABLE_IMPL + " (" +
                    ImplConfig._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    ImplConfig.COLUMN_KEY + " TEXT NOT NULL," +
                    ImplConfig.COLUMN_DOWNLOADID + " INTEGER NOT NULL DEFAULT 0," +
                    ImplConfig.COLUMN_DOWNLOADURL + " TEXT," +
                    ImplConfig.COLUMN_PACKAGENAME + " TEXT," +
                    ImplConfig.COLUMN_ICON_PATH + " TEXT," +
                    ImplConfig.COLUMN_ICON_URL + " TEXT," +
                    ImplConfig.COLUMN_TITLE + " TEXT," +
                    ImplConfig.COLUMN_DESCRIPTION + " TEXT," +
                    ImplConfig.COLUMN_TOTAL_BYTES + " INTEGER NOT NULL DEFAULT 0," +
                    ImplConfig.COLUMN_CURRENT_BYTES + " INTEGER NOT NULL DEFAULT 0," +
                    ImplConfig.COLUMN_LOCALURI + " TEXT," +
                    ImplConfig.COLUMN_STATUS + " INTEGER NOT NULL DEFAULT 0," +
                    ImplConfig.COLUMN_REASON + " INTEGER NOT NULL DEFAULT 0," +
                    ImplConfig.COLUMN_LAST_MODIFIED_TIMESTAMP + " BIGINT," +
                    ImplConfig.COLUMN_MIMETYPE + " TEXT" +
                    ");");
            db.execSQL("CREATE INDEX keyIndex ON " + ImplConfig.TABLE_IMPL + " ("
                    + ImplConfig.COLUMN_KEY + ");");
        } catch (SQLException ex) {
            Log.e(TAG, "couldn't create table favorites in database");
            throw ex;
        }
    }

    private void upgradeTo(SQLiteDatabase db, int version) {
        switch (version) {
            case 0:
            case 1:
                createImplTable(db);
                break;
            default:
                throw new IllegalStateException("Don't know how to upgrade to " + version);
        }
    }
}
