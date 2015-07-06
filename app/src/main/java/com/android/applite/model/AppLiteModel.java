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

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.android.applite.model.AllAppsList.AppSet;
import com.android.dsc.downloads.DownloadManager;
import com.applite.android.R;
import com.applite.util.AppliteConfig;
import com.google.gson.Gson;
import com.mit.market.MitMarketActivity;

/**
 * Maintains in-memory state of the Launcher. It is expected that there should
 * be only one AppLiteModel object held in a static. Also provide APIs for
 * updating the database state for the Launcher.
 */
public class AppLiteModel{
    static final boolean DEBUG_LOADERS = false;
    static final String TAG = "AppLite_Model";
    
    static final int OP_NONE = 0;
    static final int OP_ADD = 1;
    static final int OP_UPDATE = 2;
    static final int OP_REMOVE = 3; // uninstlled
    static final int OP_UNAVAILABLE = 4; // external media unmounted

    private final Context mContext;
    private int mBatchSize; // 0 is all apps at once
    private int mAllAppsLoadDelay; // milliseconds between batches
    private boolean mAllAppsLoaded;
    private AllAppsList mAllApps;
    private IconCache mIconCache;
    
    private final Object mLock = new Object();
    private DeferredHandler mHandler = new DeferredHandler();
    private LoaderTask mLoaderTask;
    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    private WeakReference<IModelCallback> mCallbacks;
    private static AppLiteModel mInstance = null;
    private UpdateHelper mUpdateHelper;
    private DownloadManager mDownloadManager;
    
    /**
     * Receives notifications whenever the user favorites have changed.
     */
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            forceReload();
        }
    };
//    private ContentObserver mDownloadObserver = new ContentObserver(new Handler()){
//        @Override
//        public void onChange(boolean selfChange, Uri uri) {
//            updateFromDownloadProvider();
//        }
//    };
//    private Runnable mDownloadRunnable = new Runnable() {
//        @Override
//        public void run() {
//            sWorker.removeCallbacks(mDownloadRunnable);
//
//            final AppSet appSet = mAllApps.getOfflineSet();
//            int currItemGroup = AppliteConfig.getProject(mContext);
//            for (IAppInfo appInfo : appSet.data){
//                ApplicationInfo info = (ApplicationInfo)appInfo;
//                if (info.downloadId > 0 
//                        && (currItemGroup == info.getItemGroup() || IAppInfo.CatgoryNone == currItemGroup)){
//                    info.requestUpdateDownload();
//                    mAllApps.update(appInfo);
//                }
//            }
//            
//            final IModelCallback callbacks = mCallbacks != null ? mCallbacks.get() : null;
//            if (appSet.modified.size() > 0) {
//                final ArrayList<IAppInfo> modified = mAllApps.getOfflineSet().modified;
//                appSet.modified = new ArrayList<IAppInfo>();
//                mHandler.post(new Runnable() {
//                    public void run() {
//                        IModelCallback cb = mCallbacks != null ? mCallbacks.get() : null;
//                        if (callbacks == cb && cb != null) {
//                            callbacks.bindUpdated(appSet.recommend,modified);
//                        }
//                    }
//                });
//            }
//        }
//    };
    //comparer
    public static final Comparator<IAppInfo> APP_NAME_COMPARATOR
            = new Comparator<IAppInfo>() {
        public final int compare(IAppInfo a, IAppInfo b) {
            ApplicationInfo aa = (ApplicationInfo)a;
            ApplicationInfo bb = (ApplicationInfo)b;
//            int result = sCollator.compare(a.firstExecuteTime, b.firstExecuteTime);
            int result = 0;
            if (aa.firstExecuteTime< bb.firstExecuteTime){
                result = 1;
            }else if (aa.firstExecuteTime > bb.firstExecuteTime){
                result = -1;
            }
            if (result == 0) {
                result = aa.getComponentName().compareTo(bb.getComponentName());
            }
            return result;
        }
    };
    
    public static final Comparator<IAppInfo> APP_ONLINE_COMPARATOR
        = new Comparator<IAppInfo>() {
        public final int compare(IAppInfo a, IAppInfo b) {
            ApplicationInfo aa = (ApplicationInfo)a;
            ApplicationInfo bb = (ApplicationInfo)b;
            int result = 0;

            if (aa.newFlag && !bb.newFlag){
                result = -1;
            }else if (!aa.newFlag && bb.newFlag){
                result = 1;
            }
            
            if (result == 0){
                if (aa.displayCount == bb.displayCount){
                    result = 0;
                }else {
                    result = (aa.displayCount< bb.displayCount)?-1:1;
                }
            }
            return result;
        }
    };
    
    
    /**
     * Runnable for the thread that loads the contents of the launcher:
     *   - workspace icons
     *   - widgets
     *   - all apps icons
     */
    private class LoaderTask implements Runnable {
//        private Context mContext;
        private boolean mIsLaunching;
        private boolean mStopped;

        LoaderTask(Context context, boolean isLaunching) {
//            mContext = context;
            mIsLaunching = isLaunching;
        }

        boolean isLaunching() {
            return mIsLaunching;
        }

        public void run() {
            // Optimize for end-user experience: if the Launcher is up and // running with the
            // All Apps interface in the foreground, load All Apps first. Otherwise, load the

            keep_running: {
                // Elevate priority when Home launches for the first time to avoid
                // starving at boot time. Staring at a blank home is not cool.
                synchronized (mLock) {
                    if (DEBUG_LOADERS) Log.d(TAG, "Setting thread priority to " +
                            (mIsLaunching ? "DEFAULT" : "BACKGROUND"));
                    android.os.Process.setThreadPriority(mIsLaunching
                            ? Process.THREAD_PRIORITY_DEFAULT : Process.THREAD_PRIORITY_BACKGROUND);
                }
                loadAndBindAllApps();

                if (mStopped) {
                    break keep_running;
                }

                // Restore the default thread priority after we are done loading items
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                }
            }

            // Clear out this reference, otherwise we end up holding it until all of the
            // callback runnables are done.
//            mContext = null;

            synchronized (mLock) {
                // If we are still the last one to be scheduled, remove ourselves.
                if (mLoaderTask == this) {
                    mLoaderTask = null;
                }
            }
            updateFromDownloadProvider();
        }

        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }

        /**
         * Gets the callbacks object.  If we've been stopped, or if the launcher object
         * has somehow been garbage collected, return null instead.  Pass in the IModelCallback
         * object that was around when the deferred message was scheduled, and if there's
         * a new IModelCallback object around then also return null.  This will save us from
         * calling onto it with data that will be ignored.
         */
        IModelCallback tryGetCallbacks(IModelCallback oldCallbacks) {
            synchronized (mLock) {
                if (mStopped) {
                    return null;
                }

                if (mCallbacks == null) {
                    return null;
                }

                final IModelCallback callbacks = mCallbacks.get();
                if (callbacks != oldCallbacks) {
                    return null;
                }
                if (callbacks == null) {
                    Log.w(TAG, "no mCallbacks");
                    return null;
                }

                return callbacks;
            }
        }

        private void loadAndBindAllApps() {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindAllApps mAllAppsLoaded=" + mAllAppsLoaded);
            }
            if (!mAllAppsLoaded) {
                loadAllAppsByBatch();
                synchronized (LoaderTask.this) {
                	if (mStopped) {
                        return;
                    }
                    mAllAppsLoaded = true;
                }
            } else {
                onlyBindAllApps();
            }
        }

        private void onlyBindAllApps() {
            final IModelCallback oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher (onlyBindAllApps)");
                return;
            }

            // shallow copy
            mHandler.post(new Runnable() {
                public void run() {
                    int count = 0;
                    final long t = SystemClock.uptimeMillis();
                    final IModelCallback callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        for (AppSet appSet:mAllApps.getSets()){
                            count += appSet.data.size();
                            final ArrayList<IAppInfo> data = (ArrayList<IAppInfo>) appSet.data.clone();
                            callbacks.bindAll(appSet.recommend,data);
                        }
                    }
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound all " + count + " apps from cache in "
                                + (SystemClock.uptimeMillis()-t) + "ms");
                    }
                }
            });

        }

//        private void loadAllAppsByBatchBackup() {
//            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
//
//            // Don't use these two variables in any of the callback runnables.
//            // Otherwise we hold a reference to them.
//            final IModelCallback oldCallbacks = mCallbacks.get();
//            if (oldCallbacks == null) {
//                // This launcher has exited and nobody bothered to tell us.  Just bail.
//                Log.w(TAG, "LoaderTask running with no launcher (loadAllAppsByBatch)");
//                return;
//            }
//            
//            
//            final Cursor c = mContext.getContentResolver().query(
//                    AppLiteSettings.Favorites.CONTENT_URI, null, null, null, 
//                    AppLiteSettings.Favorites.EXECUTE_MILLIS + " DESC");
//            
//            try {
//                while (!mStopped && c.moveToNext()) {
//                    try {
//                        mAllApps.add(new IAppInfo(mContext,c));
//                    } catch (Exception e) {
//                        Log.w(TAG, "Desktop items loading interrupted:", e);
//                    }
//                }
//            } finally {
//                c.close();
//            }
//            onlyBindAllApps();
//        }

        private void loadAllAppsByBatch() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final IModelCallback oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher (loadAllAppsByBatch)");
                return;
            }

            Cursor c = null;
            int N = Integer.MAX_VALUE;
            int startIndex;
            int i=0;
            int batchSize = -1;
            long now = System.currentTimeMillis();
            final int project = AppliteConfig.getProject(mContext);
            try{
            while (i < N && !mStopped) {
                if (i == 0) {
                    mAllApps.clear();
                    final long qiaTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                    if (IAppInfo.CatgoryNone == project) {
							c = mContext.getContentResolver().query(
									AppLiteSettings.Favorites.CONTENT_URI,
									null, null, null, AppLiteSettings.Favorites.EXECUTE_MILLIS+" DESC");
					}else {
					    String sql = "("+AppLiteSettings.Favorites.ITEM_GROUP + " = "+project+" OR "
                                + AppLiteSettings.Favorites.ITEM_GROUP + " = "+IAppInfo.CatgoryNone+" ) AND "
                                + "(" + AppLiteSettings.Favorites.PERIOD_START + " < "+now+" AND "
                                + AppLiteSettings.Favorites.PERIOD_END + " > "+now + " OR "
                                + AppLiteSettings.Favorites.ITEM_TYPE + " <> " + IAppInfo.AppOnline + ")";
							c = mContext.getContentResolver().query(
							        AppLiteSettings.Favorites.CONTENT_URI,
							        null,sql,null,
							        AppLiteSettings.Favorites.EXECUTE_MILLIS+" DESC");
					}
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "load provider(category="+project+") took "
                                + (SystemClock.uptimeMillis()-qiaTime) + "ms");
                    }
                    if (null == c) {
                        return;
                    }
                    N = c.getCount();//获得总的数据项数
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "queryIntentActivities got " + N + " apps");
                    }
                    if (N == 0) {
                        // There are no apps?!?
                    	return;
                    }
//                    if (IAppInfo.CatgoryNone == project) {
//                    	batchSize = N;
//					}else{
						if (mBatchSize == 0) {
							batchSize = N;
						} else {//一次加载app的个数
							batchSize = mBatchSize;
						}
//					}
                }

                final long t2 = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                startIndex = i;
                for (int j=0; i<N && j<batchSize; j++) {
                    // This builds the icon bitmaps.
                    c.moveToNext();
                    mAllApps.add(new ApplicationInfo(mContext,c));
                    i++;
                }

                final boolean first = i <= batchSize;
                final IModelCallback callbacks = tryGetCallbacks(oldCallbacks);

                mHandler.post(new Runnable() {
                    public void run() {
                        int count = 0;
                        final long t = SystemClock.uptimeMillis();
                        if (callbacks != null) {
                            for (AppSet appSet : mAllApps.getSets()){
                                if (appSet.added.size()>0){
                                    final ArrayList<IAppInfo> addedFinal = appSet.added;
                                    appSet.added = new ArrayList<IAppInfo>();
                                    if (first) {
                                        callbacks.bindAll(appSet.recommend,addedFinal);
                                        count += addedFinal.size();
                                    } else {
                                        callbacks.bindAdded(appSet.recommend,addedFinal);
                                        count += addedFinal.size();
                                    }
                                }
                            }
                            if (DEBUG_LOADERS) {
                                Log.d(TAG, "bound " + count + " apps in "
                                    + (SystemClock.uptimeMillis() - t) + "ms");
                            }
                        } else {
                            Log.i(TAG, "not binding apps: no Launcher activity");
                        }
                    }
                });

                if (DEBUG_LOADERS) {
                    Log.d(TAG, "batch of " + (i-startIndex) + " icons processed in "
                            + (SystemClock.uptimeMillis()-t2) + "ms");
                }

                if (mAllAppsLoadDelay > 0 && i < N) {
                    try {
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "sleeping for " + mAllAppsLoadDelay + "ms");
                        }
                        Thread.sleep(mAllAppsLoadDelay);
                    } catch (InterruptedException exc) { }
                }
            }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if (null != c){
                    c.close();
                }
            }

            if (DEBUG_LOADERS) {
                Log.d(TAG, "cached all " + N + " apps in "
                        + (SystemClock.uptimeMillis()-t) + "ms"
                        + (mAllAppsLoadDelay > 0 ? " (including delay)" : ""));
            }
        }
    }
    
    private void updateFromDownloadProvider(){
//        if (sWorkerThread.getThreadId() == Process.myTid()) {
//            mDownloadRunnable.run();
//        } else {
//            sWorker.post(mDownloadRunnable);
//        }
    }
    
    public static AppLiteModel getInstance(Context context){
        if (null == mInstance){
            mInstance = new AppLiteModel(context);
        }
        return mInstance;
    }
    
    private AppLiteModel(Context context) {
        mContext = context;
        mIconCache = new IconCache(context);
        mAllApps = new AllAppsList(mIconCache);

        final Resources res = mContext.getResources();
        mAllAppsLoadDelay = res.getInteger(R.integer.config_allAppsBatchLoadDelay);
        mBatchSize = res.getInteger(R.integer.config_allAppsBatchSize);
        
		// Register for changes to the favorites
        ContentResolver resolver = mContext.getContentResolver();
        resolver.registerContentObserver(AppLiteSettings.Favorites.CONTENT_URI, true, mFavoritesObserver);

        //Register for changes to the favorites
//        mContext.getContentResolver().registerContentObserver(
//                Uri.parse("content://com.android.dsc.downloads/my_downloads"),
//                true, mDownloadObserver);
        
        mUpdateHelper = new UpdateHelper();
		mDownloadManager = DownloadManager.getInstance(context);
		mDownloadManager.setAccessAllDownloads(true);
		
		makeDir();
    }
    
    public void destory(){
        ContentResolver resolver = mContext.getContentResolver();
        resolver.unregisterContentObserver(mFavoritesObserver);
//        resolver.unregisterContentObserver(mDownloadObserver);
    }
    
    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(IModelCallback callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<IModelCallback>(callbacks);
        }
    }

    public void forceReload() {
        synchronized (mLock) {
            // Stop any existing loaders first, so they don't set mAllAppsLoaded or
            // mWorkspaceLoaded to true later
            stopLoaderLocked();
            mAllAppsLoaded = false;
        }
        // Do this here because if the launcher activity is running it will be restarted.
        // If it's not running startLoaderFromBackground will merely tell it that it needs
        // to reload.
        startLoaderFromBackground();
    }

    /**
     * When the launcher is in the background, it's possible for it to miss paired
     * configuration changes.  So whenever we trigger the loader from the background
     * tell the launcher that it needs to re-run the loader when it comes back instead
     * of doing it now.
     */
    public void startLoaderFromBackground() {
        boolean runLoader = false;
        if (mCallbacks != null) {
            IModelCallback callbacks = mCallbacks.get();
            if (callbacks != null) {
                // Only actually run the loader if they're not paused.
                if (!callbacks.setLoadOnResume()) {
                    runLoader = true;
                }
            }
        }
        if (runLoader) {
            startLoader(mContext, false);
        }
    }

    // If there is already a loader task running, tell it to stop.
    // returns true if isLaunching() was true on the old task
    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }

    public void startLoader(Context context, boolean isLaunching) {
        synchronized (mLock) {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "startLoader isLaunching=" + isLaunching);
            }

            // Don't bother to start the thread if we know it's not going to do anything
            if (mCallbacks != null && mCallbacks.get() != null) {
                // If there is already one running, tell it to stop.
                // also, don't downgrade isLaunching if we're already running
                isLaunching = isLaunching || stopLoaderLocked();
                mLoaderTask = new LoaderTask(context, isLaunching);
                sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                sWorker.post(mLoaderTask);
            }
        }
    }

    public void stopLoader() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                mLoaderTask.stopLocked();
            }
        }
    }

    public boolean isAllAppsLoaded() {
        return mAllAppsLoaded;
    }

    void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        final String action = intent.getAction();
        NotificationManager manager;
        if(Intent.ACTION_TIME_CHANGED.equals(action)){
            forceReload();
        }
        if (DEBUG_LOADERS) Log.d(TAG, "onReceive action=" + action);
        if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            if (packageName == null || packageName.length() == 0) {
                // they sent us a bad intent
                return;
            }
            IAppInfo info = mAllApps.getIAppInfo(packageName);
            if (null != info){
                info.request(intent);
            }else{
                Log.d(TAG,action+",package:"+packageName+",not found");
            }
            
        }else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            IAppInfo app = mAllApps.getIAppInfoWithDownloadId(id);
            if (null != app){
                app.request(intent);
            }
        }else if ("com.installer.system.install.result".equals(action)
                || "com.installer.system.delete.result".equals(action)){
            final String packageName = intent.getStringExtra("name");
            if (packageName == null || packageName.length() == 0) {
                // they sent us a bad intent
                return;
            }
            IAppInfo info = mAllApps.getIAppInfo(packageName);
            if (null != info){
                info.request(intent);
            }else{
                Log.d(TAG,action+",package:"+packageName+",not found");
            }
        }else if ("com.dataservice.broadcast".equals(action)) {
            /*
            *字段说明
            *"SBUR,ACTION,packagename,title,desc,icon_url,intent"
            *ACTION : android.intent.action.MAIN
            *packagename : com.applite.android
            *title : title
            *desc : desc
            *icon_url :
            *intent : intent
            */
            String stringValue = intent.getStringExtra("intent");
            Log.d(TAG, "onReceive stringValue : " + stringValue);
            int mRequestCode = 1;
            String[] mString = null;
            mString = stringValue.split(",");
            for(int i=0;i<mString.length;i++){
                Log.d(TAG, "onReceive mString[" + i + "] : " + mString[i]);
            }

            String mAction = null;
            String mPackageName = null;
            String mTitle = null;
            String mDesc = null;
            String mIconUrl = null;
            String mIntent = null;
            if (null != mString) {
                mAction = mString[0];
                mPackageName = mString[1];
                mTitle = mString[2];
                mDesc = mString[3];
                mIconUrl = mString[4];
                mIntent = mString[5];
            }
            Intent mPlayIntent =null;

            //myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.i(TAG, "mAction : " + mAction
                    + "; mPackageName : " + mPackageName
                    + "; mTitle : " + mTitle
                    + "; mDesc : " + mDesc
                    + "; mIconUrl : " + mIconUrl
                    + "; mIntent : " + mIntent);
            if(context.getPackageName().equals(mPackageName)) {
                manager = (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
                ComponentName cn =null;
                if(null ==mIntent || null == mAction || null == mPackageName){
                    mPlayIntent = new Intent(context, MitMarketActivity.class);
                }else {
                    try {
                        mPlayIntent = Intent.parseUri(mIntent, 0);
                        mPlayIntent.getDataString();
                        cn = new ComponentName(mPackageName,
                                mPlayIntent.getDataString());
                        mPlayIntent.setComponent(cn);
                        //mPlayIntent.setAction(mAction);
                    }catch (URISyntaxException e){
                        e.printStackTrace();
                    }

                }
                Log.i(TAG, "mPlayIntent : " + mPlayIntent +" ; mPlayIntent.getDataString()"+mPlayIntent.getDataString());
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context,
                        mRequestCode,
                        mPlayIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                Log.i(TAG, "pendingIntent : " + pendingIntent );
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setContentTitle(mTitle).setContentText(mDesc).
                        setSmallIcon(R.drawable.ic_launcher).
                        setDefaults(Notification.DEFAULT_ALL).
                        setContentIntent(pendingIntent).
                        setAutoCancel(true).setSubText(mPackageName);
                manager.notify(mRequestCode, builder.build());
            }
        }
    }
    
    void packageUpdated(final int op, final IAppInfo[] infos){
        Runnable r = new Runnable() {
            public void run() {
                final int N = infos.length;
                int currItemGroup = AppliteConfig.getProject(mContext);
                ApplicationInfo appInfo = null;
                switch (op) {
                    case OP_ADD:
                        for (int i = 0; i < N; i++) {
                            if (DEBUG_LOADERS)  Log.d(TAG, "mAllApps.addPackage " + infos[i]);
                            appInfo = (ApplicationInfo)infos[i];
                            if (appInfo.getItemGroup() == currItemGroup||currItemGroup==IAppInfo.CatgoryNone) {
                                mAllApps.add(infos[i]);
                            }
                        }
                        break;
                    case OP_UPDATE:
                        for (int i = 0; i < N; i++) {
                            if (DEBUG_LOADERS)  Log.d(TAG, "mAllApps.updatePackage " + infos[i]);
                            appInfo = (ApplicationInfo)infos[i];
                            if (appInfo.getItemGroup() == currItemGroup||currItemGroup==IAppInfo.CatgoryNone) {
                                mAllApps.update(infos[i]);
                            }
                        }
                        break;
                    case OP_REMOVE:
                    case OP_UNAVAILABLE:
                        for (int i = 0; i < N; i++) {
                            if (DEBUG_LOADERS) Log.d(TAG, "mAllApps.removePackage " + infos[i]);
                            appInfo = (ApplicationInfo)infos[i];
                            if (appInfo.getItemGroup() == currItemGroup||currItemGroup==IAppInfo.CatgoryNone) {
                                mAllApps.remove(infos[i]);
                            }
                        }
                        break;
                }

                final IModelCallback callbacks = mCallbacks != null ? mCallbacks.get() : null;
                if (callbacks == null) {
                    Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                    return;
                }

                mHandler.post(new Runnable() {
                    public void run() {
                        IModelCallback cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            for (AppSet appSet:mAllApps.getSets()){
                                if (appSet.added.size() <= 0) {
                                    continue;
                                }
                                final boolean recommend = appSet.recommend;
                                final ArrayList<IAppInfo> addedFinal = appSet.added; 
                                appSet.added = new ArrayList<IAppInfo>();
                                callbacks.bindAdded(recommend,addedFinal);
                            }
                        }
                    }
                });

                mHandler.post(new Runnable() {
                    public void run() {
                        IModelCallback cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            for (AppSet appSet:mAllApps.getSets()){
                                if (appSet.removed.size() <= 0) {
                                    continue;
                                }
                                final boolean permanent = op != OP_UNAVAILABLE;
                                final boolean recommend = appSet.recommend;
                                final ArrayList<IAppInfo> removedFinal = appSet.removed;
                                appSet.removed = new ArrayList<IAppInfo>();
                                for (IAppInfo info: removedFinal) {
                                    mIconCache.remove(info.getComponentName());
                                }
                                callbacks.bindRemoved(recommend,removedFinal, permanent);
                            }
                        }
                    }
                });
                        
                mHandler.post(new Runnable() {
                    public void run() {
                        IModelCallback cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            for (AppSet appSet:mAllApps.getSets()){
                                if (appSet.modified.size() <= 0) {
                                    continue;
                                }
//                                final boolean recommend = appSet.recommend;
//                                final ArrayList<IAppInfo> modifiedFinal = appSet.modified;
                                appSet.modified = new ArrayList<IAppInfo>();
//                                callbacks.bindUpdated(recommend,modifiedFinal);
                            }
                        }
                    }
                });
            }
        };

        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            sWorker.post(r);
        }
    }

    
    /**
     * Add an item to the database in a specified container. Sets the container, screen, cellX and
     * cellY fields of the item. Also assigns an ID to the item.
     */
    void addItemToDatabase(final IAppInfo info) {
        final ContentResolver cr = mContext.getContentResolver();
        final ContentValues values = ((ApplicationInfo)info).getContentValues();
        Runnable r = new Runnable() {
            public void run() {
                cr.insert(AppLiteSettings.Favorites.CONTENT_URI, values);
            }
        };

        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            sWorker.post(r);
        }
    }
    
    /**
     * Update an item to the database in a specified container.
     */
    void updateItemInDatabase(final IAppInfo info) {
        final ContentValues values =((ApplicationInfo)info).getContentValues();
        
        final Uri uri = AppLiteSettings.Favorites.getContentUri(info.getId(), false);
        final ContentResolver cr = mContext.getContentResolver();
        Runnable r = new Runnable() {
            public void run() {
            	cr.update(uri, values, null, null);
            }
        };

        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            sWorker.post(r);
        }
    }
    
    /**
     * Removes the specified item from the database
     * @param item
     */
    void deleteItemFromDatabase(final IAppInfo item) {
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uriToDelete = AppLiteSettings.Favorites.getContentUri(item.getId(), false);

        Runnable r = new Runnable() {
            public void run() {
                cr.delete(uriToDelete, null, null);
            }
        };
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            sWorker.post(r);
        }
    }
    public void addMarketApp(final String details,final String packageName) {
    	final int category = AppliteConfig.getProject(mContext);
    	final IModelCallback callbacks = mCallbacks != null ? mCallbacks.get() : null;
        if (callbacks == null) {
            // This launcher has exited and nobody bothered to tell us.  Just bail.
            Log.w(TAG, "LoaderTask running with no launcher (onlyBindAllApps)");
            return;
        }
    	Runnable runnable=new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String url = AppliteConfig.getUpdateServer(mContext);
//                url += "?category=";
//                url += category;
//                url +="&";
//                url +=details;
                mUpdateHelper.updateImpl(mContext, url, category,details, new UpdateHelperCallback() {
					@Override
					public void result(UpdateHelper helper, Object obj) {
						// TODO Auto-generated method stub
						if (obj==null/*||!isSamePackageName(obj, packageName)*/) {
							callbacks.getDataFailed();
						}else {
							handleResult((String)obj,category);
						}
					}
				});
			}
		};
		if (sWorkerThread.getThreadId() == Process.myTid()) {
            runnable.run();
        } else {
            sWorker.post(runnable);
        }
	}
    private boolean isSamePackageName(final JSONObject jsonObject,final String packageName) {
    	if (null == jsonObject){
             return false;
         }
		try {
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			if (jsonArray.length() <= 0)
				return false;
			int i = 0;
			for(i = 0; i < jsonArray.length();i++) {
				JSONObject appObject = jsonArray.getJSONObject(i);
				if (appObject.getString("package_name") == null	|| !appObject.getString("package_name").trim().equals(packageName)) {
					return false;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
    public void updateOnlineApp() {
        final int category = AppliteConfig.getProject(mContext);
        long prevUpdate = AppliteConfig.getUpdateMillis(mContext,category);
        long now = System.currentTimeMillis();
        if (now - prevUpdate > AppliteConfig.getUpdateInterval(mContext)*DateUtils.HOUR_IN_MILLIS){
            Runnable r = new Runnable() {
                public void run() {
                	//获取json数据的url
                	String url = AppliteConfig.getUpdateServer(mContext);
//                    url += "?category=";
//                    url += category;
                    boolean result = mUpdateHelper.updateImpl(mContext,url,category,"",
                            new UpdateHelperCallback(){
                                @Override
                                public void result(UpdateHelper helper, Object obj) {
                                    // TODO Auto-generated method stub
                                    handleResult((String)obj,category);
                                }
                            });
                }
            };

            if (sWorkerThread.getThreadId() == Process.myTid()) {
                r.run();
            } else {
                sWorker.post(r);
            }
        }
    }
    
    public void cancelUpdate() {
        Runnable r = new Runnable() {
            public void run() {
                mUpdateHelper.cancelUpdate(mContext);
                if (null != mCallbacks){
                    IModelCallback callback = mCallbacks.get();
                    if (null != callback){
                        callback.finishRefresh();
                    }
                }
            }
        };

        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            sWorker.post(r);
        }
    }
    
    
    private void handleResult(final String jsonStr,final int project){
        if (null == jsonStr || jsonStr.length()<=0){
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try{
                    UpdateDataModel model = new Gson().fromJson(jsonStr, UpdateDataModel.class);
                    AppliteConfig.setUpdateServer(mContext,model.getUpdate_url());
                    AppliteConfig.setUpdateInterval(mContext,model.getUpdate_interval());
                    AppliteConfig.setRecommendHidden(mContext,model.getRecommend_hide()==1);
                    AppliteConfig.setDataTimestamp(mContext, model.getData_timestamp());

                    final ArrayList<ContentValues> valuesArray = new ArrayList<ContentValues>();
                    int size = model.getData().size();
                    for (int i = 0; i < size; i++) {
                        UpdateDataModel.AppData data = model.getData().get(i);
                        if (null == data.getPackage_name() || null == data.getClass_name()
                                || 0 == data.getPackage_name().trim().length() || 0 == data.getClass_name().trim().length()){
							continue;
						}
                        ApplicationInfo app = new ApplicationInfo(mContext,data);
                        ApplicationInfo appInList = (ApplicationInfo)mAllApps.getIAppInfoWithId(app.getId());
                        if (null == appInList){//mAllApps中不存在这个json数据（应用中没有这项）
                        	//通过id查找数据库
                            Cursor c = mContext.getContentResolver().query(
                                    AppLiteSettings.Favorites.CONTENT_URI, 
                                    null, 
                                    AppLiteSettings.Favorites.ID + " = ?",
                                    new String[]{app.getId()}, 
                                    null);
                            try{
                                if (null == c || c.getCount()==0){//如果没有这个id,就add一个
                                    ContentValues value  = app.getContentValues();
                                    valuesArray.add(value);
                                }else{
                                    c.moveToNext();
                                    appInList = new ApplicationInfo(mContext,c);
                                    appInList.updateItem(app);
                                }
                            }finally {
                                if (null != c){
                                    c.close();
                                }
                            }
                        }else{//存在的话就改变数据并update数据库中的数据
                            appInList.updateItem(app);
                        }
                    }
                    if (valuesArray.size()>0){
                        mContext.getContentResolver().bulkInsert(
                                AppLiteSettings.Favorites.CONTENT_URI, 
                                valuesArray.toArray(new ContentValues[valuesArray.size()]));
                    }
                    if (size > 0){
                        AppliteConfig.setUpdateMillis(mContext,project);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };

        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            sWorker.post(r);
        }
    }
    
    public IconCache getIconCache(){
        return mIconCache;
    }
    
    private boolean isOnExternalStorage(Cursor cursor) {
        String localUriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
        if (localUriString == null) {
            return false;
        }
        Uri localUri = Uri.parse(localUriString);
        if (!localUri.getScheme().equals("file")) {
            return false;
        }
        String path = localUri.getPath();
        String externalRoot = Environment.getExternalStorageDirectory().getPath();
        return path.startsWith(externalRoot);
    }

    public IAppInfo getMoreApp(){
        AppSet appSet = mAllApps.getRecommendSet();
        for (IAppInfo appInfo:appSet.data){
            if (IAppInfo.AppMore == appInfo.getItemType()){
                return appInfo;
            }
        }
        return null;
    }
    
	public Intent getMoreIntent() {
	    List<IAppInfo> allData = mAllApps.getAllData();
		long size=allData.size();
		for (int i = 0; i < size; i++) {
			if (allData.get(i).getItemType()==IAppInfo.AppMore) {
				return allData.get(i).getIntent();
			}
		}
		return null;
	}
	public IAppInfo getInfoByPackageName(String packageName) {
		return mAllApps.getIAppInfo(packageName);
	}
	
	Handler getWorkHandler(){
	    return sWorker;
	}
	private void makeDir() {
		String path = Environment.getExternalStorageDirectory().getPath() 
				+ File.separator + IAppInfo.extenStorageDirPath;
		File mFile=new File(path);
		if (!mFile.exists()) {
			mFile.mkdirs();
		}
	}
}
