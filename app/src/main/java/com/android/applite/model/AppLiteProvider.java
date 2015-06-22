/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.applite.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.android.applite.imageprocess.ThemeManager;
import com.applite.android.R;
import com.applite.util.AppliteConfig;

public class AppLiteProvider extends ContentProvider {
    private static final String TAG = "AppLiteProvider";
    private static final boolean LOGD = false;

    private static final String DATABASE_NAME = "folder.db";

    private static final int DATABASE_VERSION = 4;

    static final String AUTHORITY = "com.android.applite.settings";

    static final String TABLE_FAVORITES = "favorites";
//    static final String TABLE_DETAILS = "details";
    static final String PARAMETER_NOTIFY = "notify";

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    private static long dbInsertAndCheck(DatabaseHelper helper,
            SQLiteDatabase db, String table, String nullColumnHack, ContentValues values){
		if (!values.containsKey(AppLiteSettings.Favorites.ID)) {
			throw new RuntimeException(
					"Error: attempting to add item without specifying an id");
		}
		return db.insert(table, nullColumnHack, values);
    }

    private static void deleteId(SQLiteDatabase db, String id) {
        Uri uri = AppLiteSettings.Favorites.getContentUri(id, false);
        SqlArguments args = new SqlArguments(uri, null, null);
        db.delete(args.table, args.where, args.args);
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = dbInsertAndCheck(mOpenHelper, db, args.table, null, initialValues);
        if (rowId <= 0) return null;

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (dbInsertAndCheck(mOpenHelper, db, args.table, null, values[i]) < 0) {
                    return 0;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        sendNotify(uri);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0) sendNotify(uri);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count=-1;
		try {
			SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			count = db.update(args.table, values, args.where, args.args);
			if (count > 0) sendNotify(uri);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private final Context mContext;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            getWritableDatabase();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (LOGD) Log.d(TAG, "creating new launcher database");

            onUpgrade(db, 0, DATABASE_VERSION);

            // Populate favorites table with initial favorites
			loadFavorites(db);
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

        private void createFavoritesTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
                db.execSQL("CREATE TABLE "+TABLE_FAVORITES+" (" +
                        AppLiteSettings.Favorites.ID + " TEXT PRIMARY KEY," +
                        AppLiteSettings.Favorites.ICON_URL + " TEXT," +
                        AppLiteSettings.Favorites.APK_URL + " TEXT," +
                        AppLiteSettings.Favorites.APK_SIZE + " INTEGER NOT NULL DEFAULT 0," +
                        AppLiteSettings.Favorites.LOCAL_APK_PATH + " TEXT," +
                        AppLiteSettings.Favorites.APK_VER_CODE + " INTEGER," +
                        AppLiteSettings.Favorites.APK_VER_NAME + " TEXT," +
                        AppLiteSettings.Favorites.DATA_DOWNLOAD + " INTEGER NOT NULL DEFAULT 1," +
                        AppLiteSettings.Favorites.FEEDBACK_URL + " TEXT," +
                        AppLiteSettings.Favorites.FEEDBACK_STATUS + " INTEGER NOT NULL DEFAULT 0," +
                        AppLiteSettings.Favorites.TITLE + " TEXT," +
                        AppLiteSettings.Favorites.INTENT + " TEXT," +
                        AppLiteSettings.Favorites.ITEM_TYPE + " INTEGER NOT NULL DEFAULT 0," +
                        AppLiteSettings.Favorites.ICON + " BLOB," +
                        AppLiteSettings.Favorites.SCREEN + " INTEGER," +
                        AppLiteSettings.Favorites.CELLX + " INTEGER," +
                        AppLiteSettings.Favorites.CELLY + " INTEGER," +
                        AppLiteSettings.Favorites.SPANX + " INTEGER," +
                        AppLiteSettings.Favorites.SPANY + " INTEGER," +
                        AppLiteSettings.Favorites.EXECUTE_MILLIS + " INTEGER," +
                        AppLiteSettings.Favorites.DETAIL_URL + " TEXT," +
                        AppLiteSettings.Favorites.INTRODUCE_ICON + " BLOB," +
                        AppLiteSettings.Favorites.INTRODUCE_TEXT + " TEXT," +
                        AppLiteSettings.Favorites.DOWNLOAD_ID + " INTEGER NOT NULL DEFAULT -1" +
                        ");");
            } catch (SQLException ex) {
                Log.e(TAG, "couldn't create table favorites in database");
                throw ex;
            }
        }
        
//        private void createDetailsTable(SQLiteDatabase db) {
//            try {
//                db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETAILS);
//                db.execSQL("CREATE TABLE "+TABLE_DETAILS+" (" +
//                        AppLiteSettings.Details.ID + " TEXT PRIMARY KEY," +
//                        AppLiteSettings.Details.DETAIL_URL + " TEXT," +
//                        AppLiteSettings.Details.INTRODUCE_ICON + " BLOB," +
//                        AppLiteSettings.Details.INTRODUCE_TEXT + " TEXT" +
//                        ");");
//            } catch (SQLException ex) {
//                Log.e(TAG, "couldn't create table details in database");
//                throw ex;
//            }
//        }
        
        private void upgradeTo(SQLiteDatabase db, int version) {
            switch (version) {
                case 0:
                case 1:
                    createFavoritesTable(db);
//                    createDetailsTable(db);
                    break;
                case 2:
                    addColumn(db, TABLE_FAVORITES, AppLiteSettings.Favorites.NEW_FLAG," INTEGER NOT NULL DEFAULT 1");
                    break;
                case 3:
                    addColumn(db, TABLE_FAVORITES, AppLiteSettings.Favorites.ITEM_GROUP," INTEGER NOT NULL DEFAULT 0");
                    ContentValues values = new ContentValues();
                    values.put(AppLiteSettings.Favorites.ITEM_GROUP, IAppInfo.CatgoryYlzx);
                    db.update(TABLE_FAVORITES, values, AppLiteSettings.Favorites.ITEM_GROUP+" is NULL", null);
                    break;
                case 4:
                    addColumn(db, TABLE_FAVORITES, AppLiteSettings.Favorites.PERIOD_START," INTEGER NOT NULL DEFAULT 0");
                    addColumn(db, TABLE_FAVORITES, AppLiteSettings.Favorites.PERIOD_END," INTEGER NOT NULL DEFAULT 0");
                    addColumn(db, TABLE_FAVORITES, AppLiteSettings.Favorites.DEFAULT_PENDING," INTEGER NOT NULL DEFAULT 0");
                    addColumn(db, TABLE_FAVORITES, AppLiteSettings.Favorites.BUILDIN_PATH," TEXT");
                	break;
                default:
                    throw new IllegalStateException("Don't know how to upgrade to " + version);
            }
        }
        
        /**
         * Loads the default set of favorite packages from an xml file.
         *
         * @param db The database to write the values into
         * @param filterContainerId The specific container id of items to load
         */
        private int loadFavorites(SQLiteDatabase db) {
            int index = 0;
            UpdateDataModel defaultData = UpdateDataModel.getDefaultData(mContext);
        	try {
				AppliteConfig.setUpdateServer(mContext,defaultData.getUpdate_url());
				AppliteConfig.setUpdateInterval(mContext,defaultData.getUpdate_interval());
                AppliteConfig.setRecommendHidden(mContext,defaultData.getRecommend_hide()==1);
                AppliteConfig.setDataTimestamp(mContext, defaultData.getData_timestamp());

				int size = defaultData.getData().size();
				final ContentValues[] valuesArray = new ContentValues[size];
				for (int i = 0; i < size; i++) {
				    valuesArray[i] = new ApplicationInfo(mContext,defaultData.getData().get(i)).getContentValues();
				}
				index = bulkAdd(db,valuesArray);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            return index;
        }

        
        private int bulkAdd(SQLiteDatabase db, ContentValues[] values) {
            db.beginTransaction();
            try {
                int numValues = values.length;
                for (int i = 0; i < numValues; i++) {
                    if (dbInsertAndCheck(this, db,TABLE_FAVORITES, null, values[i]) < 0) {
                        return 0;
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return values.length;
        }
        
        
        private void addColumn(SQLiteDatabase db, String dbTable, String columnName,
                String columnDefinition) {
            db.execSQL("ALTER TABLE " + dbTable + " ADD COLUMN " + columnName + " "
                + columnDefinition);
        }
    }
    
    /**
     * Build a query string that will match any row where the column matches
     * anything in the values list.
     */
    static String buildOrWhereString(String column, int[] values) {
        StringBuilder selectWhere = new StringBuilder();
        for (int i = values.length - 1; i >= 0; i--) {
            selectWhere.append(column).append("=").append(values[i]);
            if (i > 0) {
                selectWhere.append(" OR ");
            }
        }
        return selectWhere.toString();
    }

    static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "id='" + url.getPathSegments().get(1)+"'";
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
}
