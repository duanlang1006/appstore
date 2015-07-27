package com.mit.impl;

import android.content.Context;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.util.OtherUtils;

/**
 * Created by hxd on 15-7-27.
 */
public class ImplDbHelper {
    private final static String DB_NAME = "impl";
    private final static int DB_VERSION = 1;

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
        public void onUpgrade(DbUtils dbUtils, int i, int i2) {

        }
    }

}
