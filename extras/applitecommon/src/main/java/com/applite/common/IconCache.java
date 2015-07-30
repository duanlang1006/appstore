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

package com.applite.common;

import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.applite.theme.ThemeManager;

/**
 * Cache of application icons.  Icons can be made from any thread.
 */
public class IconCache {
    private static final String TAG = "IconCache";
    private static final int INITIAL_ICON_CACHE_CAPACITY = 64;
    private final Bitmap mDefaultIcon;
    private final Context mContext;
    private final PackageManager mPackageManager;
    private final HashMap<String, Bitmap> mCache = new HashMap<String, Bitmap>(INITIAL_ICON_CACHE_CAPACITY);

    private static IconCache mInstance = null;
    private static synchronized void initInstance(Context context){
        if (null == mInstance ){
            mInstance = new IconCache(context);
        }
    }

    public static IconCache getInstance(Context context){
        if (null == mInstance){
            initInstance(context);
        }
        return mInstance;
    }


    private IconCache(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mDefaultIcon = makeDefaultIcon();
    }
    /**
     * 获取完整的默认图片
     * @return
     */
    public Drawable getFullResDefaultActivityIcon() {
    	// 修改前android.R.drawable.sym_def_app_icon
        return getFullResIcon(mContext.getResources(),R.drawable.buffer);
    }
    /**
     * 这里的iconId指的不是资源中的ｉd,是ResolveInfo
     */
    public Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            d = resources.getDrawable(iconId);
        } catch (Resources.NotFoundException e) {
            d = null;
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(String packageName, int iconId) {
        Resources resources;
        try {
            resources = mPackageManager.getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    private Drawable getFullResIcon(ResolveInfo info) {
        Resources resources;
        try {
            resources = mPackageManager.getResourcesForApplication(info.activityInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
        	//info.activityInfo 获取 ActivityInfo对象，即<activity>或<receiver >节点信息
        	//如果activityinfo有ｉcon返回packageItemInfo的ｉcon id,否则返回Application的ｉcon id
            int iconId = info.activityInfo.getIconResource();
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    private Bitmap makeDefaultIcon() {
        Drawable d = getFullResDefaultActivityIcon();
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
		// c.setBitmap(null);
        b = ThemeManager.getInstance(mContext).getIconBitmap(b, true, "default");
        return b;
    }

    /**
     * Remove any records for the supplied ComponentName.
     */
    public void remove(String packageName) {
        synchronized (mCache) {
            mCache.remove(packageName);
        }
    }

    /**
     * Empty out the cache.
     */
    public void flush() {
        synchronized (mCache) {
            mCache.clear();
        }
    }

    public Bitmap getIcon(String packageName,Bitmap bitmap) {
        synchronized (mCache) {
            Bitmap ret = null;
            Intent intent = mPackageManager.getLaunchIntentForPackage(packageName);
            if (null != intent){
                final ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
                if (null != resolveInfo ) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) getFullResIcon(resolveInfo);
                    ret = cacheLocked(packageName, bitmapDrawable.getBitmap());
                }
            }
            if (null == ret && null != bitmap){
                ret = cacheLocked(packageName,bitmap);
            }
            if (null == ret){
                ret = mDefaultIcon;
            }
            return ret;
        }
    }
    
    public boolean isDefaultIcon(Bitmap icon) {
        return mDefaultIcon == icon;
    }

    private Bitmap cacheLocked(String packageName,Bitmap icon) {
        Bitmap  ret = mCache.get(packageName);
        if (ret == null) {
            ret = ThemeManager.getInstance(mContext).getIconBitmap(icon, true, packageName);
            mCache.put(packageName, ret);
        }
        return ret;
    }

}
