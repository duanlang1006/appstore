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

import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.android.applite.imageprocess.ThemeManager;
import com.android.applite.plugin.AppLiteApplication;
import com.applite.android.R;

/**
 * Cache of application icons.  Icons can be made from any thread.
 */
public class IconCache {
    private static final String TAG = "Launcher.IconCache";

    private static final int INITIAL_ICON_CACHE_CAPACITY = 64;

    private static class CacheEntry {
        public Bitmap icon;
        public String title;
    }
    private final Bitmap mDefaultIcon;
    private final Bitmap mMaskIcon;
//    private final Bitmap mDetailIcon;
    private final Context mContext;
    private final PackageManager mPackageManager;
    /*
     * HashMap有两个参数影响其性能：初始容量和加载因子。默认初始容量是16，加载因子是0.75。当哈希表中的条目数超出了加载因子与当前容量的乘积时，通过调用 rehash 方法将容量翻倍 
     */
    private final HashMap<ComponentName, CacheEntry> mCache = new HashMap<ComponentName, CacheEntry>(INITIAL_ICON_CACHE_CAPACITY);
    private int mIconDpi;

    public IconCache(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        int density = context.getResources().getDisplayMetrics().densityDpi;
        if (AppLiteApplication.isScreenLarge()) {
            if (density == DisplayMetrics.DENSITY_LOW) {
                mIconDpi = DisplayMetrics.DENSITY_MEDIUM;
            } else if (density == DisplayMetrics.DENSITY_MEDIUM) {
                mIconDpi = DisplayMetrics.DENSITY_HIGH;
            } else if (density == DisplayMetrics.DENSITY_HIGH) {
                mIconDpi = DisplayMetrics.DENSITY_XHIGH;
            } else if (density == DisplayMetrics.DENSITY_XHIGH) {
                // We'll need to use a denser icon, or some sort of a mipmap
                mIconDpi = DisplayMetrics.DENSITY_XHIGH;
            }
        } else {
            mIconDpi = context.getResources().getDisplayMetrics().densityDpi;
        }
        // need to set mIconDpi before getting default icon
        mDefaultIcon = makeDefaultIcon();
        mMaskIcon = makeMaskIcon(mDefaultIcon);
        
//        mDetailIcon = makeDetailIcon();
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
            d = resources.getDrawableForDensity(iconId,mIconDpi);
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
		c.setBitmap(null);
        b = ThemeManager.getInstance(mContext).getIconBitmap(b, true, "default");
        return b;
    }
    
    private Bitmap makeMaskIcon(Bitmap ref) {
        Bitmap b = Bitmap.createBitmap(ref.getWidth(), ref.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0,  0 | Paint.FILTER_BITMAP_FLAG| Paint.DITHER_FLAG));
        canvas.drawColor(Color.argb(180, 0, 0, 0));
        Paint paint = new Paint();
        paint.setAntiAlias(true); // 消除锯齿
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        canvas.drawBitmap(ref, 0,0, paint);
        return b;
    }

    private Bitmap makeDetailIcon() {
        Drawable d = getFullResIcon(mContext.getResources(),R.drawable.buffer);
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        c.setBitmap(null);
        return b;
    }

    /**
     * Remove any records for the supplied ComponentName.
     */
    public void remove(ComponentName componentName) {
        synchronized (mCache) {
            mCache.remove(componentName);
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

//    /**
//     * Fill in "application" with the icon and label for "info."
//     */
//    public void getTitleAndIcon(ApplicationInfo application, ResolveInfo info,
//            HashMap<Object, CharSequence> labelCache) {
//        synchronized (mCache) {
//            CacheEntry entry = cacheLocked(application.componentName, info, labelCache);
//
//            application.title = entry.title;
//            application.iconBitmap = entry.icon;
//        }
//    }
    /**
     *已安装的获取清单文件中的ｉcon,没有安装的获取默认图片 
     */
    public Bitmap getIcon(Intent intent,ApplicationInfo appInfo) {
        synchronized (mCache) {
        	/*
        	 * 系统开机后会扫描所有.apk文件，保存到内存里
        	 * 第一部分是系统apk文件，目录位于/system/app；第二部分就是你安装后的app，系统会拷贝一份放在/data/app目录下。即开机只安装这两个目录下的。
        	 * 所以 resolveInfo只有在安装的情况不为null
        	 */
            final ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent,0);
            ComponentName component = intent.getComponent();
            CacheEntry entry = null;
            if (null != resolveInfo && null != component){
                entry = cacheLocked(component, resolveInfo, null);
            }else if (null != appInfo.iconBitmap){
                entry = cacheLocked(appInfo,null);
            }
            if (null == entry){
                return mDefaultIcon;
            }else{
                return entry.icon;
            }
        }
    }
    
//    public Bitmap getDetailIcon(){
//        return mDetailIcon;
//    }

    public boolean isDefaultIcon(Bitmap icon) {
        return mDefaultIcon == icon;
    }
    
    public Bitmap getMaskIcon(){
        return mMaskIcon;
    }

    static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }
    /**
     * ResolveInfo就是解析intent过程中返回的信息
     */
    private CacheEntry cacheLocked(ComponentName componentName, ResolveInfo info, HashMap<Object, CharSequence> labelCache) {
        CacheEntry entry = mCache.get(componentName);
        if (entry == null) {
            entry = new CacheEntry();

            mCache.put(componentName, entry);

            ComponentName key = getComponentNameFromResolveInfo(info);
            //containsKey()判断KEY是否存在
            if (labelCache != null && labelCache.containsKey(key)) {
                entry.title = labelCache.get(key).toString();
            } else {
                entry.title = info.loadLabel(mPackageManager).toString();
                if (labelCache != null) {
                    labelCache.put(key, entry.title);
                }
            }
            if (entry.title == null) {
                entry.title = info.activityInfo.name;
            }
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getFullResIcon(info);
            entry.icon = ThemeManager.getInstance(mContext)
                            .getIconBitmap(bitmapDrawable.getBitmap(), false, info.activityInfo.name);
        }
        return entry;
    }
    private CacheEntry cacheLocked(ApplicationInfo appInfo,HashMap<Object, CharSequence> labelCache) {
        ComponentName componentName = appInfo.getComponentName();
        CacheEntry entry = mCache.get(componentName);
        if (entry == null) {
            entry = new CacheEntry();
            mCache.put(componentName, entry);

            ComponentName key = new ComponentName(componentName.getPackageName(), appInfo.getTitle());
            if (labelCache != null && labelCache.containsKey(key)) {
                entry.title = labelCache.get(key).toString();
            } else {
                entry.title = appInfo.getTitle();
                if (labelCache != null) {
                    labelCache.put(key, entry.title);
                }
            }
            if (entry.title == null) {
                entry.title = appInfo.getTitle();
            }

            entry.icon = ThemeManager.getInstance(mContext)
                    .getIconBitmap(appInfo.iconBitmap, true, appInfo.getTitle());
        }
        return entry;
    }

    public HashMap<ComponentName,Bitmap> getAllIcons() {
        synchronized (mCache) {
            HashMap<ComponentName,Bitmap> set = new HashMap<ComponentName,Bitmap>();
            for (ComponentName cn : mCache.keySet()) {
                final CacheEntry e = mCache.get(cn);
                set.put(cn, e.icon);
            }
            return set;
        }
    }
}
