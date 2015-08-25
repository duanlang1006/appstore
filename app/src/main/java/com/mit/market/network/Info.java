package com.mit.market.network;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Created by caijian on 15-8-25.
 */
public class Info {
    Context context;
    PackageManager pm;

    public Info(Context context) {
        this.context = context;
        pm = context.getPackageManager();
    }

    /*
     * 获取程序 图标
     */
    public Drawable getAppIcon(String packname) throws PackageManager.NameNotFoundException {
        ApplicationInfo info = pm.getApplicationInfo(packname, 0);
        return info.loadIcon(pm);
    }
}
