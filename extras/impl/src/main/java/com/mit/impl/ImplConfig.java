package com.mit.impl;

import android.content.Context;
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

}
