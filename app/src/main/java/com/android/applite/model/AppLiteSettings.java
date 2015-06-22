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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Settings related utilities.
 */
public class AppLiteSettings {
    public static final class Favorites implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                AppLiteProvider.AUTHORITY + "/" + AppLiteProvider.TABLE_FAVORITES +
                "?" + AppLiteProvider.PARAMETER_NOTIFY + "=true");

        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://" +
                AppLiteProvider.AUTHORITY + "/" + AppLiteProvider.TABLE_FAVORITES +
                "?" + AppLiteProvider.PARAMETER_NOTIFY + "=false");
        
        public static Uri getContentUri(String id, boolean notify) {
            return Uri.parse("content://" + AppLiteProvider.AUTHORITY +
                    "/" + AppLiteProvider.TABLE_FAVORITES + "/" + id + "?" +
                    AppLiteProvider.PARAMETER_NOTIFY + "=" + notify);
        }
        
        public static final String ID = "id";
        public static final String ICON_URL = "iconUrl";
        public static final String APK_URL = "apkUrl";
        public static final String APK_SIZE = "apkSize";
        public static final String LOCAL_APK_PATH = "localIconPath";
        public static final String APK_VER_CODE = "versionCode";
        public static final String APK_VER_NAME = "versionName";
        public static final String DATA_DOWNLOAD = "dataDownload";
        public static final String FEEDBACK_URL = "feedbackUrl";
        public static final String FEEDBACK_STATUS = "feedbackStatus";
        public static final String TITLE = "title";
        public static final String INTENT = "intent";
        public static final String ITEM_TYPE = "itemType";
        public static final String ICON = "icon";
        public static final String SCREEN = "screen";
        public static final String CELLX = "cellX";
        public static final String CELLY = "cellY";
        public static final String SPANX = "spanX";
        public static final String SPANY = "spanY";
        public static final String EXECUTE_MILLIS = "executeMillis";
        public static final String DETAIL_URL = "detailUrl";
        public static final String INTRODUCE_ICON = "introduceIcon";
        public static final String INTRODUCE_TEXT = "introduceText";
        public static final String DOWNLOAD_ID = "downloadId";
        public static final String NEW_FLAG = "newFlag";
        public static final String ITEM_GROUP = "itemGroup";
        //by hxd add field
        public static final String PERIOD_START = "periodStart";        //有效期开始时间，转换成毫秒
        public static final String PERIOD_END = "periodEnd";            //有效期结束时间，转换成毫秒
        public static final String DEFAULT_PENDING = "defaultPending";  //是否默认是pending状态
        public static final String BUILDIN_PATH = "buildinPath";            //固化apk的路径
    }
}
