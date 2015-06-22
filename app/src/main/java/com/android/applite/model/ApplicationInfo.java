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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.android.applite.model.UpdateDataModel.AppData;
import com.android.dsc.downloads.DownloadManager;
import com.applite.android.R;
import com.applite.util.AppLiteSpUtils;
import com.applite.util.AppliteUtilities;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;


/**
 * Represents an app in AllAppsView.
 */
class ApplicationInfo extends Observable implements IAppInfo{
    private static final String TAG = "ApplicationInfo";
    private Context mContext = null;
    private UpdateDataModel.AppData mAppData;
    private int itemType;
    protected String localApkPath;
    protected int feedbackStatus;
    protected long firstExecuteTime;
    protected long downloadId;
    protected int downloadProgress;
    protected int downloadNetFlags;
    protected int status;
    protected boolean newFlag = true;
    protected int displayCount = 0;
    protected Bitmap iconBitmap;
    
    private static int mNewFlagRadius = 0;
    private static Paint mPaint = new Paint();
    private static RectF mArcRect = new RectF();
    private static Bitmap sPauseBitmap = null;
    private static Bitmap sFailBitmap;
    private static Bitmap sMaskBitmap = null;

    
    private static List<ImplInterface> sImplList = new ArrayList<ImplInterface>();
    static {
        sImplList.add(ImplDownload.getInstance());
        sImplList.add(ImplFakeDownload.getInstance());
        sImplList.add(ImplPackageManager.getInstance());
    }
    
    private ImplListener mImplListener = new ImplListener() {
        @Override
        public void onFinish(boolean success,String cmd, Intent result) {
            // TODO Auto-generated method stub
            AppLiteModel model = AppLiteModel.getInstance(mContext);
            if (ImplInterface.ACTION_DOWNLOAD_REQ.equals(cmd)){
                if (success){
                    downloadId = result.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    itemType = AppOffline;
                    status = STATUS_PENDING;
                    downloadProgress = 0;
                    firstExecuteTime = System.currentTimeMillis();
                    model.packageUpdated(AppLiteModel.OP_UPDATE, new ApplicationInfo[]{ApplicationInfo.this});
                    model.updateItemInDatabase(ApplicationInfo.this);
                    AppLiteSpUtils.setDataDownload(mContext,AppLiteSpUtils.getDataDownload(mContext)+1);
                }
            }else if (ImplInterface.ACTION_DOWNLOAD_COMPLETE.equals(cmd)){
                if (success && status >= STATUS_INIT && status <= STATUS_FAILED){
                    localApkPath = result.getStringExtra("localPath");
                    downloadProgress = result.getIntExtra("progress", (null != localApkPath)?100:0);
                    String path = getMatchFile();
                    if (null != localApkPath && null == path){
                        status = STATUS_PACKAGE_INVALID;
                    }else{
                        status = result.getIntExtra("status", (null != localApkPath)?STATUS_SUCCESSFUL:STATUS_FAILED);
                        if  (null != localApkPath){
                            requestPackageInstall();
                            model.packageUpdated(AppLiteModel.OP_UPDATE, new ApplicationInfo[]{ApplicationInfo.this});
                            model.updateItemInDatabase(ApplicationInfo.this);
                        }
                    }
                    setChanged();
                    notifyObservers();
                    AppLiteSpUtils.setDataDownloadSuccess(mContext, AppLiteSpUtils.getDataDownloadSuccess(mContext)+1);
                }
            }else if (ImplInterface.ACTION_DOWNLOAD_UPDATE_REQ.equals(cmd)){
                if (success && status >= STATUS_INIT && status <= STATUS_FAILED){
                    downloadProgress = result.getIntExtra("progress", downloadProgress);
                    int preStatus = status;
                    status = result.getIntExtra("status", status);
                    setChanged();
                    notifyObservers();
                    if (STATUS_RUNNING == preStatus && STATUS_PAUSED == status){
                        AppLiteSpUtils.setDataTimeOut(mContext, AppLiteSpUtils.getDataTimeOut(mContext)+1);
                    }
                    if (STATUS_PAUSED == preStatus && STATUS_RUNNING == status){
                        AppLiteSpUtils.setDataCarryOn(mContext, AppLiteSpUtils.getDataCarryOn(mContext)+1);
                    }
                }
            }else if (ImplInterface.ACTION_PACKAGE_INSTALL_REQ.equals(cmd)){
                if (success){
                    boolean silent = result.getBooleanExtra("silent", true);
                    if (silent){
                        status = STATUS_PRIVATE_INSTALLING;
                    }else{
                        status = STATUS_NORMAL_INSTALLING;
                    }
                    setChanged();
                    notifyObservers();
                }
            }else if (ImplInterface.ACTION_PACKAGE_ADDED.equals(cmd)
                    || ImplInterface.ACTION_PACKAGE_CHANGED.equals(cmd)
                    || "com.installer.system.install.result".equals(cmd)){
                if (success){
                    itemType = AppInstalled;
                    newFlag = true;
                    requestDownloadDelete();
                    downloadId = 0;
                    
                    status=STATUS_INSTALLED;
                    model.getIconCache().remove(getComponentName());
                    AppLiteSpUtils.setDataInstallSuccess(mContext, AppLiteSpUtils.getDataInstallSuccess(mContext)+1);
                }else {
                    status=STATUS_INSTALL_FAILED;
                }
                model.packageUpdated(AppLiteModel.OP_UPDATE, new ApplicationInfo[]{ApplicationInfo.this});
                model.updateItemInDatabase(ApplicationInfo.this);
                setChanged();
                notifyObservers();
            }else if (ImplInterface.ACTION_PACKAGE_REMOVED.equals(cmd)
                    || "com.installer.system.delete.result".equals(cmd)){
                if (success){
                    newFlag = false;
                    itemType = AppOnline;
                    status = STATUS_INIT;
                    checkItemType();
                    localApkPath = null;
                    model.packageUpdated(AppLiteModel.OP_UPDATE, new ApplicationInfo[]{ApplicationInfo.this});
                    model.updateItemInDatabase(ApplicationInfo.this);
                    model.getIconCache().remove(getComponentName());
                    setChanged();
                    notifyObservers();
                    AppLiteSpUtils.setDataUninstall(mContext, AppLiteSpUtils.getDataUninstall(mContext)+1);
                }
            } 
        }
    };

    /**
     * Must not hold the Context.ApplicationInfo
     */
    public ApplicationInfo(Context context,Cursor c) {
        mContext = context;
        mAppData = new AppData(context,c);
        localApkPath = c.getString(c.getColumnIndex(AppLiteSettings.Favorites.LOCAL_APK_PATH));
        feedbackStatus = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.FEEDBACK_STATUS));
        firstExecuteTime = c.getLong(c.getColumnIndex(AppLiteSettings.Favorites.EXECUTE_MILLIS));
        downloadId = c.getLong(c.getColumnIndex(AppLiteSettings.Favorites.DOWNLOAD_ID));
        itemType = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.ITEM_TYPE));
        iconBitmap = getIconFromCursor(c, c.getColumnIndex(AppLiteSettings.Favorites.ICON), context);
        newFlag = c.getInt(c.getColumnIndex(AppLiteSettings.Favorites.NEW_FLAG))==1;
		status = STATUS_INIT;
		downloadNetFlags = NETWORK_WIFI;
		if (0 != mAppData.getData_download()){
		    downloadNetFlags |= NETWORK_MOBILE;
		}
		
        if (null == iconBitmap ){
            IconCache iconCache = AppLiteModel.getInstance(mContext).getIconCache();
            Bitmap icon = iconCache.getIcon(mAppData.getRealIntent(), this);
            if (iconCache.isDefaultIcon(icon)){
                String iconUrl = mAppData.getIcon_url();
                String iconTarget = Environment.getExternalStorageDirectory().getPath()
                        +File.separator+ApplicationInfo.extenStorageDirPath 
                        + AppliteUtilities.getFilenameFromUrl(iconUrl);
                iconBitmap = getIconFromFile(new File(iconTarget),mContext);
                if (null == iconBitmap){
                    FinalHttp finalHttp = new FinalHttp();
                    finalHttp.configTimeout(60*1000);
                    finalHttp.download(iconUrl, iconTarget,new AjaxCallBack<File>() {
                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            // TODO Auto-generated method stub
                            super.onFailure(t, errorNo, strMsg);
                        }
    
                        @Override
                        public void onSuccess(File t) {
                            // TODO Auto-generated method stub
                            iconBitmap = getIconFromFile(t,mContext);
                            if (null != iconBitmap){
                                AppLiteModel model = AppLiteModel.getInstance(mContext);
                                model.packageUpdated(AppLiteModel.OP_UPDATE, new ApplicationInfo[]{ApplicationInfo.this});
                                model.updateItemInDatabase(ApplicationInfo.this);
                            }
                            super.onSuccess(t);
                        }
                    });
                }
            }else{
                iconBitmap = icon;
            }
        }
        checkItemType();
        init();
    }

    public ApplicationInfo(Context context,AppData data) {
        mContext = context;
        mAppData = data;
        status=STATUS_INIT;
        downloadNetFlags = NETWORK_WIFI;
        if (0 != mAppData.getData_download()){
            downloadNetFlags |= NETWORK_MOBILE;
        }
        firstExecuteTime = System.currentTimeMillis();
        if ("APPLITE_MORE".equals(mAppData.getId())) {
            itemType = AppMore;
            mAppData.setApp_name(mContext.getResources().getString(R.string.more));
            firstExecuteTime = 0;
             try {
                 BitmapDrawable bitmapDrawable =  (BitmapDrawable)mContext.getResources().getDrawable(R.drawable.ic_launcher);
                 iconBitmap = bitmapDrawable.getBitmap();
             } catch (Exception e) {
                 e.printStackTrace();
                 iconBitmap = null;
             }
        }else {
            checkItemType();
        }
        newFlag = true;
        displayCount = 0;
        init();
    }
    
    private void init(){
        requestDownloadInit();
        if (0 == mNewFlagRadius){
            mNewFlagRadius = (int) mContext.getResources().getDimension(R.dimen.app_icon_new_flag_radius);
        }
    }
    
    
    public String getId(){
        return mAppData.getId();
    }
    
    public Bitmap getIcon() {
        if (null == iconBitmap ){
            String iconUrl = mAppData.getIcon_url();
            String iconTarget = Environment.getExternalStorageDirectory().getPath()
                    +File.separator+ApplicationInfo.extenStorageDirPath 
                    + AppliteUtilities.getFilenameFromUrl(iconUrl);
            iconBitmap = getIconFromFile(new File (iconTarget),mContext);
            if (null == iconBitmap){
                FinalHttp finalHttp = new FinalHttp();
                finalHttp.configTimeout(60*1000);
                finalHttp.download(iconUrl, iconTarget,new AjaxCallBack<File>() {
                    @Override
                    public void onFailure(Throwable t, int errorNo, String strMsg) {
                        // TODO Auto-generated method stub
                        super.onFailure(t, errorNo, strMsg);
                    }
    
                    @Override
                    public void onSuccess(File t) {
                        // TODO Auto-generated method stub
                        iconBitmap = getIconFromFile(t,mContext);
                        if (null != iconBitmap){
                            AppLiteModel model = AppLiteModel.getInstance(mContext);
                            model.packageUpdated(AppLiteModel.OP_UPDATE, new ApplicationInfo[]{ApplicationInfo.this});
                            model.updateItemInDatabase(ApplicationInfo.this);
                        }
                        super.onSuccess(t);
                    }
                });
            }
        }
        IconCache iconCache = AppLiteModel.getInstance(mContext).getIconCache();
        Bitmap icon =  iconCache.getIcon(mAppData.getRealIntent(),this);
        if (null == sMaskBitmap){
            sMaskBitmap = iconCache.getMaskIcon();
        }
        return icon;
    }
	
	public Intent getIntent() {
		return mAppData.getRealIntent();
	}

//	public String getPercent() {
//		String string;
//		if (downloadProgress==-1) {
//			string=mContext.getResources().getString(R.string.connection);
//			return new StringBuffer().append(string).toString();
//		}
//		if (downloadProgress==-2) {
//			return new StringBuffer().append("").toString();
//		}
//		return new StringBuilder().append(downloadProgress).append("%").toString();
//	}
	
	public String getDetailIcon(){
	    return mAppData.getDetail_url();
	}

//    public Bitmap getDetailIcon(boolean download) {
//        Bitmap ret = null;
//        final long size;
//        String fileName=mAppData.getId()+"_"+AppliteUtilities.getFilenameFromUrl(mAppData.getDetail_url());
//        final String fullName = Environment.getExternalStorageDirectory().getPath()
//                        +File.separator
//                        +extenStorageDirPath
//                        +fileName;
//        File file = new File(fullName);
//        if (file.exists()){
//            try{
//                ret = BitmapFactory.decodeFile(fullName);
//            }catch(Exception e){
//                ret = null;
//            }
//        }
//        final IconCache iconCache = AppLiteModel.getInstance(mContext).getIconCache();
//        if (null == ret){
////            ret = operateDetailBitmap(iconCache.getDetailIcon(),iconCache);
//            ret = iconCache.getDetailIcon();
//            size=BitmapToBytes(ret).length;
//            String state = Environment.getExternalStorageState();
//            if (null != networkHelper && Environment.MEDIA_MOUNTED.equals(state)
//                    && Environment.getExternalStorageDirectory().canWrite() && download) {
//                networkHelper.downloadBigIconImpl(mContext, mAppData.getDetail_url(), new NetworkHelperCallback(){
//                    @Override
//                    public void result(NetworkHelper helper, byte[] data) {
//                        // TODO Auto-generated method stub
//                    	if (data!=null) {
//                    		Bitmap bitmap=BitmapFactory.decodeByteArray(data, 0, data.length);
//                    		OutputStream os = null;
//                    		try{
////                    			byte[] data1=BitmapToBytes(operateDetailBitmap(bitmap,iconCache));
//                    		    byte[] data1=BitmapToBytes(bitmap);
//                    			if (data1.length!=size) {
//                    				os = new FileOutputStream(fullName);
//                    				os.write(data1);
//								}
//                    			AppLiteModel model = AppLiteModel.getInstance(mContext);
//                    			model.packageUpdated(AppLiteModel.OP_UPDATE, new ApplicationInfo[]{ApplicationInfo.this});
//                    		}catch(Exception e){
//                    			e.printStackTrace();
//                    		}finally{
//                    			if (null != os){
//                    				try{
//                    					os.close();
//                    				} catch (IOException e) {
//                    					e.printStackTrace();
//                    				}
//                    			}
//                    		}
//                    	}
//                    }
//                });
//            }
//        }
//        return ret;
//    }

    private void checkItemType(){
        if (AppMore == itemType) return;
        
        ComponentName cn = getComponentName();
        ActivityInfo info = null;
        try {
            info = mContext.getPackageManager().getActivityInfo(cn, 0);
        } catch (Exception nnfe) {
            info = null;
        }
        if (null == info) {
            switch(itemType){
                case AppInstalled:
                    itemType = AppOffline;
                    break;
                case AppOffline:
                    break;
                case AppOnline:
                    break;
                case AppUpgrade:
                    itemType = AppOffline;
                    break;
                default:
                    itemType = AppOnline;
                    break;
            }
            if (1 == mAppData.getApp_buildin()){
                itemType = AppOffline;
            }
		} else {
			itemType = AppInstalled;
        }
    }
	
	public int getStatus() {
		return status;
	}

	public long getSize() {
		return mAppData.getSize();
	}

	public int getItemType() {
		return itemType;
	}
	
	public String getVersionName() {
		return mAppData.getVersion_name();
	}

    public void setShown() {
        if (AppOnline == itemType || AppMore == itemType) {
            newFlag = false;
            displayCount = 1;
        }
    }
    
    public void clearDisplay(){
        displayCount = 0;
    }
    
    public boolean isNewFlag() {
        return newFlag;
    }

	public ComponentName getComponentName(){
        ComponentName cn = mAppData.getRealIntent().getComponent();
        if (null == cn){
            cn = new ComponentName(mContext,"com.android.applite.plugin.AppLitePlugin");
        }
        return cn;
    }
    
    public String getDetailUrl() {
		return mAppData.getDetail_url();
	}

	public String getDetailText() {
		return mAppData.getIntroduce_text();
	}

	public String getTitle() {
        // TODO Auto-generated method stub
	    String title = mAppData.getApp_name();
	    if (AppInstalled != itemType){
    	    switch(status){
    	        case STATUS_INIT:
    	            break;
    	        case STATUS_PENDING:
    	            title = mContext.getResources().getString(R.string.download_running);
    	            break;
    	        case STATUS_RUNNING:
    	            title = mContext.getResources().getString(R.string.download_running);
    	            break;
    	        case STATUS_PAUSED:
    	            title = mContext.getResources().getString(R.string.download_paused);
    	            break;
    	        case STATUS_FAILED:
    	            title = mContext.getResources().getString(R.string.download_failed);
    	            break;
    	        case STATUS_SUCCESSFUL:
    	            title = mContext.getResources().getString(R.string.download_success);
    	            break;
    	        case STATUS_PACKAGE_INVALID:
    	            title = mContext.getResources().getString(R.string.package_invalid);
    	            break;
    	        case STATUS_PRIVATE_INSTALLING:
    	            title = mContext.getResources().getString(R.string.installing);
    	            break;
    	        case STATUS_NORMAL_INSTALLING:
    	            break;
    	        case STATUS_INSTALLED:
    	            break;
    	        case STATUS_INSTALL_FAILED:
    	            title = mContext.getResources().getString(R.string.install_failed);
    	            break;
    	    }
	    }
        return title;
    }
    
	/*
	 * public int itemType(){ return itemType; }
	 */
    @Override
	public String toString() {
		return "ApplicationInfo ["+mAppData.getApp_name() 
		        + ",id:" + downloadId+ ",status:" + status 
		        + ",progress:" + downloadProgress
		        +"itemType:"+itemType+""+ "]";
	}

    ContentValues getContentValues() {
        ContentValues values = mAppData.getContentValues(mContext);
        values.put(AppLiteSettings.Favorites.LOCAL_APK_PATH, localApkPath);
        values.put(AppLiteSettings.Favorites.FEEDBACK_STATUS, feedbackStatus);
        values.put(AppLiteSettings.Favorites.EXECUTE_MILLIS, firstExecuteTime);
        values.put(AppLiteSettings.Favorites.DOWNLOAD_ID, downloadId);
        values.put(AppLiteSettings.Favorites.ITEM_TYPE, itemType);
        values.put(AppLiteSettings.Favorites.ICON, (null != iconBitmap)?flattenBitmap(iconBitmap):null);
        values.put(AppLiteSettings.Favorites.NEW_FLAG, newFlag?1:0);
        return values;
    }

	boolean testBuildinApkFileExist() {
	    String appbuildinpath = mAppData.getApp_buildinpath();
		if (null == appbuildinpath || appbuildinpath.length() < 1) {
			return false;
		}
		File file = new File(appbuildinpath);
		if (file.exists()) {
			return true;
		}
		return false;
	}
	void requestDownloadInit(){
        Intent intent = new Intent (ImplInterface.ACTION_DOWNLOAD_INIT);
        intent.putExtra("key", mAppData.getId());
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
        request(intent);
	}
	
	void requestPackageDownload(){
	    Intent intent = new Intent (ImplInterface.ACTION_DOWNLOAD_REQ);
	    intent.putExtra("key", mAppData.getId());
	    intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
	    intent.putExtra("url", mAppData.getApk_url());
	    intent.putExtra("publicDir",extenStorageDirPath);
	    intent.putExtra("dataFlag", mAppData.getData_download());
	    intent.putExtra("roming", false);
	    intent.putExtra("title", mAppData.getApp_name());
	    intent.putExtra("desc", mAppData.getIntroduce_text());
	    intent.putExtra("fake",testBuildinApkFileExist());
        request(intent);
	}

	void requestFakeDownload(){
        Intent intent = new Intent (ImplInterface.ACTION_FAKE_DOWNLOAD_REQ);
        intent.putExtra("key", mAppData.getId());
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
        intent.putExtra("appId",mAppData.getId());
        intent.putExtra("localPath", mAppData.getApp_buildinpath());
        intent.putExtra("progress", downloadProgress);
        intent.putExtra("status", status);
        request(intent);
    }
	
	void requestUpdateDownload(){
	    Intent intent = new Intent (ImplInterface.ACTION_DOWNLOAD_UPDATE_REQ);
	    intent.putExtra("key", mAppData.getId());
	    intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
	    request(intent);
	}
	
	void requestDownloadDelete(){
        Intent intent = new Intent (ImplInterface.ACTION_DOWNLOAD_DELETE_REQ);
        intent.putExtra("key", mAppData.getId());
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
        request(intent);
    }
	
    void requestPackageInstall(){
        if (null != localApkPath){
            Intent intent = new Intent (ImplInterface.ACTION_PACKAGE_INSTALL_REQ);
            intent.putExtra("key", mAppData.getId());
            intent.putExtra("apk", localApkPath);
            intent.putExtra("package", getComponentName().getPackageName());
            intent.putExtra("silent", true);
            request(intent);
        }
    }
    
    public void launchApp(){
        final AppLiteModel model = AppLiteModel.getInstance(mContext);
        newFlag = false;
        model.packageUpdated(
                AppLiteModel.OP_UPDATE,new ApplicationInfo[]{ApplicationInfo.this});
        model.updateItemInDatabase(ApplicationInfo.this);
        setChanged();
        notifyObservers();
        try{
            mContext.startActivity(mAppData.getRealIntent());
            AppLiteSpUtils.setDataRunNumber(mContext, AppLiteSpUtils.getDataRunNumber(mContext)+1);
        }catch(Exception e){
            e.printStackTrace();
        }
    } 
    
    public void removeApp(){
        final AppLiteModel model = AppLiteModel.getInstance(mContext);
        switch(itemType){
              case AppInstalled:
                  String packageN = getComponentName().getPackageName(); 
                  Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + packageN));
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  mContext.startActivity(intent);
                  break;
              case AppOffline:
                  requestDownloadDelete();
                  downloadId = 0;
                  itemType = AppOnline;
                  status = STATUS_INIT;
                  checkItemType();
                  model.packageUpdated(AppLiteModel.OP_UPDATE,new ApplicationInfo[]{ApplicationInfo.this});
                  model.updateItemInDatabase(ApplicationInfo.this);
                  
                  String path = Environment.getExternalStorageDirectory().getPath()
                                  +File.separator+ApplicationInfo.extenStorageDirPath;
                  String appFileName = AppliteUtilities.getFilenameFromUrl(mAppData.getApk_url());
                  File file = new File(path+appFileName);
                  if (file.exists()){
                      file.delete();
                  }
                  break;
          }
    }
    
    public void downloadApp(){
        final AppLiteModel model = AppLiteModel.getInstance(mContext);
        String fileName = getMatchFile();
        if (null != fileName){
            //本地已有文件存在，安装
            itemType = AppOffline;
            firstExecuteTime = System.currentTimeMillis();
            localApkPath = fileName;
            status = STATUS_SUCCESSFUL;
            model.packageUpdated(AppLiteModel.OP_UPDATE,new ApplicationInfo[]{ApplicationInfo.this});
            model.updateItemInDatabase(ApplicationInfo.this);
            requestPackageInstall(); 
        } else if (testBuildinApkFileExist()) {
            requestFakeDownload();
        }else {
            requestPackageDownload();
        }
    }
    
    @Override
    public void drawIcon(Canvas canvas, 
            int left, int top, int right, int bottom) {
        // TODO Auto-generated method stub
        int iconWidth = right - left;
        int iconHeight = bottom - top;
        mPaint.reset();
        mPaint.setAntiAlias(true); // 消除锯齿
        if (IAppInfo.AppOffline ==  itemType) {
            if (null != sMaskBitmap){
                canvas.drawBitmap(sMaskBitmap,left,top, null);
            }
            switch(status){
                case STATUS_INIT:
                    if (1 == mAppData.getApp_buildin()){
                        if (null == sPauseBitmap){
                            sPauseBitmap= BitmapFactory.decodeResource(mContext.getResources(), R.drawable.app_pause);
                        }
                        canvas.drawBitmap(sPauseBitmap, 
                                left + (iconWidth - sPauseBitmap.getWidth()) / 2, 
                                top + (iconHeight - sPauseBitmap.getHeight())/2, 
                                null);
                    }
                    break;
                case STATUS_PAUSED:
                    if (null == sPauseBitmap){
                        sPauseBitmap= BitmapFactory.decodeResource(mContext.getResources(), R.drawable.app_pause);
                    }
                    canvas.drawBitmap(sPauseBitmap, 
                            left + (iconWidth - sPauseBitmap.getWidth()) / 2, 
                            top + (iconHeight - sPauseBitmap.getHeight())/2, 
                            null);
                    break;
                case STATUS_SUCCESSFUL:
                    break;
                case STATUS_FAILED:
                    if (null == sFailBitmap){
                        sFailBitmap= BitmapFactory.decodeResource(mContext.getResources(), R.drawable.app_fail);
                    }
                    canvas.drawBitmap(sFailBitmap, 
                            right - sFailBitmap.getWidth(),
                            bottom -sFailBitmap.getHeight(), 
                            null);
                    break;
            }
            //画进度
            final int radius = Math.min(iconWidth,iconHeight)/5;
            mPaint.setStrokeWidth(3);
            mPaint.setColor(Color.argb(100, 180, 180, 180));
            mPaint.setStyle(Style.STROKE);
            mArcRect.set(left+radius,top+radius,right-radius,bottom-radius);
            canvas.drawArc(mArcRect, 0, 360, false, mPaint);
            mPaint.setColor(Color.argb(255, 255, 255, 255));
            canvas.drawArc(mArcRect, -90, (float) ((3.6)*downloadProgress), false, mPaint);
        }else if (IAppInfo.AppInstalled == itemType){
            if (newFlag){
                mPaint.setStyle(Style.FILL);
                mPaint.setColor(Color.RED);
                canvas.drawCircle(right-mNewFlagRadius/2, top+mNewFlagRadius/2, mNewFlagRadius, mPaint);
            }
        }
        setShown();
    }



    class MyFilenameFilter implements FilenameFilter{
        private String mPreFileName;
        private String mExternsion;
        
        public MyFilenameFilter(String filename) {
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > 0){
                mPreFileName = filename.substring(0,dotIndex);
                mExternsion = filename.substring(dotIndex);
            }else{
                mPreFileName = filename;
                mExternsion = null;
            }
        }

        @Override
        public boolean accept(File dir, String filename) {
            boolean preMatch = true;
            boolean extMatch = true;
            if (null != mExternsion){
                extMatch = filename.endsWith(mExternsion);
            }
            if (null != mPreFileName){
                preMatch = filename.contains(mPreFileName);
            }
            return (preMatch&&extMatch);
        }
    }

    private boolean fileMatch(Context context ,File file,long size){
        if (file.exists()
            /*&& (size <= 100 || file.length() == size)*/){
            PackageInfo pk = context.getPackageManager()
                .getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
            if (null != pk && pk.packageName.equals(getComponentName().getPackageName().trim())){
                return true;
            }
        }
        return false;
    }
    
    String getMatchFile(){
        String fileName=AppliteUtilities.getFilenameFromUrl(mAppData.getApk_url());
        String path = Environment.getExternalStorageDirectory().getPath()
                        +File.separator
                        +extenStorageDirPath;
        File dir = new File(path);
        //dir.exists();
        File[] listOfFiles = null;
        if (dir != null && dir.isDirectory()) {
            listOfFiles = dir.listFiles(new MyFilenameFilter(fileName));
        }
        if (null != listOfFiles){
            for (File file : listOfFiles){
                if (fileMatch(mContext,file,mAppData.getSize())){
                    return file.getAbsolutePath();
                  
                }
            }
        }
        
        if (null != localApkPath && !localApkPath.startsWith(path)){
            dir = new File(localApkPath.substring(0,localApkPath.lastIndexOf("/")));
            if (dir != null && dir.isDirectory()) {
                listOfFiles = dir.listFiles(new MyFilenameFilter(fileName));
            }
            if (null != listOfFiles){
                for (File file : listOfFiles){
                    if (fileMatch(mContext,file,mAppData.getSize())){
                        return file.getAbsolutePath();
                    }
                }
            }
        }
        
        return null;
    }
    
    void updateItem(ApplicationInfo info){
        if (AppInstalled != itemType){
            newFlag = true;
            displayCount = 0;
            if (!mAppData.getApp_name().equals(info.mAppData.getApp_name())){
                mAppData.setApp_name(info.getTitle());
            }
            String myUri = mAppData.getRealIntent().toUri(0);
            String otherUri = info.mAppData.getRealIntent().toUri(0);
            if (!myUri.equals(otherUri)){
                try{
                    Intent in = Intent.parseUri(otherUri, 0);
                    mAppData.setRealIntent(in);
                    mAppData.setIntent(info.mAppData.getIntent());
                    mAppData.setPackage_name(info.mAppData.getPackage_name());
                    mAppData.setClass_name(info.mAppData.getClass_name());
                }catch(Exception e){}
            }
            if (!mAppData.getIcon_url().equals(info.mAppData.getIcon_url())){
                mAppData.setIcon_url(info.mAppData.getIcon_url());
                iconBitmap = null;
            }
            if (!mAppData.getApk_url().equals(info.mAppData.getApk_url())){
                mAppData.setApk_url(info.mAppData.getApk_url());
                localApkPath = info.localApkPath;
            }
            if (mAppData.getSize() != info.mAppData.getSize()){
                mAppData.setSize(info.mAppData.getSize());
            }
            
            if (mAppData.getVersion_code() != info.mAppData.getVersion_code()){
                mAppData.setVersion_code(info.mAppData.getVersion_code());
            }
            if (!mAppData.getVersion_name().equals(info.mAppData.getVersion_name())){
                mAppData.setVersion_name(info.mAppData.getVersion_name());
            }
            mAppData.setData_download(info.mAppData.getData_download());
            mAppData.setFb_url(info.mAppData.getFb_url());
            feedbackStatus = info.feedbackStatus;
            
            firstExecuteTime = info.firstExecuteTime;
            if (!mAppData.getDetail_url().equals(info.mAppData.getDetail_url())){
                mAppData.setDetail_url(info.mAppData.getDetail_url());
            }
            if (mAppData.getIntroduce_text().equals(info.mAppData.getIntroduce_text())){
                mAppData.setIntroduce_text(info.mAppData.getIntroduce_text());
            }
            mAppData.setItem_group(info.mAppData.getItem_group());
            mAppData.setApp_apkstartdate(info.mAppData.getApp_apkstartdate());
            mAppData.setApp_apkenddate(info.mAppData.getApp_apkenddate());
            mAppData.setApp_buildin(info.mAppData.getApp_buildin());
            mAppData.setApp_buildinpath(info.mAppData.getApp_buildinpath());

            downloadId = info.downloadId;
            downloadNetFlags=info.downloadNetFlags;
            status=info.status;
            AppLiteModel.getInstance(mContext).updateItemInDatabase(this);
        }
    }
    
    private Bitmap getIconFromCursor(Cursor c, int iconIndex, Context context) {
        byte[] data = c.getBlob(iconIndex);
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            return null;
        }
    }
 
	private Bitmap getIconFromData(byte[] data, Context context) {
		if (data==null||data.length==0) 
			return null;
		try {
		    return BitmapFactory.decodeByteArray(data, 0, data.length);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Bitmap getIconFromFile(File file, Context context) {
        if (file==null||false == file.exists()) 
            return null;
        try {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }

    static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = flattenBitmap(bitmap);
            values.put(AppLiteSettings.Favorites.ICON, data);
        }
    }

	public int getItemGroup(){
	    return mAppData.getItem_group();
	}
	
	@Override
    public void request(Intent cmd) {
        // TODO Auto-generated method stub
	    for (ImplInterface impl:sImplList){
	        if (impl.request(mContext,cmd, mImplListener)){
	            break;
	        }
	    }
    }

    @Override
    public void abort(Intent cmd) {
        // TODO Auto-generated method stub
        
    }
    
    
//    private byte[] BitmapToBytes(Bitmap bm) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        return baos.toByteArray();
//    }
//    private Bitmap operateDetailBitmap(Bitmap ret,IconCache iconCache) {
//        int width = (int)mContext.getResources().getDimension(R.dimen.detail_icon_bg_width);
//        int height = (int)mContext.getResources().getDimension(R.dimen.detail_icon_bg_height);
//        mMask = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
//        Paint paint = new Paint();
//        paint.setAntiAlias(true); // 消除锯齿
//        paint.setColor(Color.argb(255, 255, 255, 255));
//        Canvas canvas = new Canvas(mMask); 
//        canvas.drawRect(0, 0, mMask.getWidth(), mMask.getHeight(), paint);
//        paint.setXfermode(new PorterDuffXfermode(Mode.DARKEN));
//        canvas.drawColor(Color.argb(255, 255, 255, 255), Mode.MULTIPLY);
//        Bitmap big=null;
//        if (ret.getWidth()>ret.getHeight()&&ret!=iconCache.getDetailIcon()) {
//            big=big(onRotaBitmap(ret));
//        }else{
//             big=big(ret);
//        }
//        canvas.drawBitmap(big, (mMask.getWidth()-big.getWidth())/2,(mMask.getHeight()-big.getHeight())/2, paint);
//
//        big.recycle();
//        big = null;
//        return ThemeManager.getRoundedCornerBitmap(mMask, 5);
//    }

//    private  Bitmap big(Bitmap bitmap) {
//        IconCache iconCache = AppLiteModel.getInstance(mContext).getIconCache();
//        Matrix matrix = new Matrix(); 
//        if (bitmap == iconCache.getDetailIcon()) {
//            matrix.postScale(1.0f,1.0f); //长和宽放大缩小的比例
//        }else {
//            float sx=(float)mContext.getResources().getDimension(R.dimen.detail_icon_width)/bitmap.getWidth();
//            float sy=(float)mContext.getResources().getDimension(R.dimen.detail_icon_height)/bitmap.getHeight();
//            if (sx==1.0&&sy==1.0) {
//              return bitmap;
//          }
//            matrix.postScale(sx,sy); //长和宽放大缩小的比例
//        }
//        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
//        bitmap.recycle();
//        bitmap = null;
//        return resizeBmp;
//    }

//    private Bitmap onRotaBitmap(Bitmap bm) {
//        Matrix m = new Matrix();
//        m.setRotate(90, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
//        float targetX, targetY;
//        targetX = bm.getHeight();
//        targetY = 0;
//
//        final float[] values = new float[9];
//        m.getValues(values);
//
//        float x1 = values[Matrix.MTRANS_X];
//        float y1 = values[Matrix.MTRANS_Y];
//
//        m.postTranslate(targetX - x1, targetY - y1);
//
//        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
//        Paint paint = new Paint();
//        Canvas canvas = new Canvas(bm1);
//        canvas.drawBitmap(bm, m, paint);
//
//        return bm1;
//    }
    
    @Override
    public void registerObserver(Observer o) {
        // TODO Auto-generated method stub
        this.addObserver(o);
    }

    @Override
    public void unregisterObserver(Observer o) {
        // TODO Auto-generated method stub
        this.deleteObservers();
    }

//    interface NetworkHelperCallback{
//        void result(NetworkHelper helper,byte[] data);
//    }

//    class NetworkHelper{
//        private static final String TAG = "AppLite_NetworkHelper";
//
//        private static final int MAX_RETRY_COUNT = 3; 
//        private static final int HTTP_TYPE_REQUEST_SMALL_ICON =  10000;
//        private static final int HTTP_TYPE_REQUEST_BIG_ICON =  10001;
//     
//        //small icon member
//        private int mSmallIconReqId = -1;
//        private int mSmallIconRetryCount = MAX_RETRY_COUNT;
//        private NetworkHelperCallback mSmallIconCallback;
//
//        //big icon member
//        private int mBigIconReqId = -1;
//        private int mBigIconRetryCount = MAX_RETRY_COUNT;
//        private NetworkHelperCallback mBigIconCallback;
//
//        private Handler mHandler = new Handler();
//        private HttpTaskListener mListener = new HttpTaskListener(){
//            @Override
//            public void onReceiveResponseData(int cmdType, HttpResponse response, HttpTask httpTask) throws Exception {
//                Log.d(TAG,"onReceiveResponseData,"+cmdType+","+",retry="+mSmallIconRetryCount);
//                HttpEntity entity = response.getEntity();
//                final byte[] data = getByteData(entity);
//                switch(cmdType){
//                    case HTTP_TYPE_REQUEST_SMALL_ICON:
//                        mHandler.post(new Runnable(){
//                            @Override
//                            public void run() {
//                                // TODO Auto-generated method stub
//                                if (null != mSmallIconCallback){
//                                    mSmallIconCallback.result(NetworkHelper.this,data);
//                                }
//                            }
//                        });
//                        mSmallIconReqId = -1;
//                        break;
//                    case HTTP_TYPE_REQUEST_BIG_ICON:
//                        mHandler.post(new Runnable(){
//                            @Override
//                            public void run() {
//                                // TODO Auto-generated method stub
//                                if (null != mBigIconCallback){
//                                    mBigIconCallback.result(NetworkHelper.this,data);
//                                }
//                            }
//                        });
//                        mBigIconReqId = -1;
//                        break;
//                    default:
//                        break;
//                }
//            }
//
//            @Override
//            public void onDealHttpError(int cmdType, int errorCode, String errorStr, final HttpTask httpTask) {
//                Log.d(TAG,"onDealHttpError,"+cmdType+","+errorCode+","+errorStr+",retry="+mSmallIconRetryCount);
//                switch(cmdType){
//                    case HTTP_TYPE_REQUEST_SMALL_ICON:
//                        mSmallIconReqId = -1;
//                        if (mSmallIconRetryCount > 0){
//                            mHandler.postDelayed(new Runnable(){
//                                @Override
//                                public void run() {
//                                    // TODO Auto-generated method stub
//                                    downloadSmallIconImpl(httpTask.mContext,httpTask.url,mSmallIconCallback);
//                                }
//                            }, 5000);
//                        }else{
//                            mSmallIconRetryCount = MAX_RETRY_COUNT;
//                            mHandler.post(new Runnable(){
//                                @Override
//                                public void run() {
//                                    // TODO Auto-generated method stub
//                                    if (null != mSmallIconCallback){
//                                        mSmallIconCallback.result(NetworkHelper.this,null);
//                                    }
//                                }
//                            });
//                        }
//                        break;
//                    case HTTP_TYPE_REQUEST_BIG_ICON:
//                        mBigIconReqId = -1;
//                        if (mBigIconRetryCount > 0){
//                            mHandler.postDelayed(new Runnable(){
//                                @Override
//                                public void run() {
//                                    // TODO Auto-generated method stub
//                                    downloadBigIconImpl(httpTask.mContext,httpTask.url,mBigIconCallback);
//                                }
//                            }, 5000);
//                        }else{
//                            mBigIconRetryCount = MAX_RETRY_COUNT;
//                            mHandler.post(new Runnable(){
//                                @Override
//                                public void run() {
//                                    // TODO Auto-generated method stub
//                                    if (null != mBigIconCallback){
//                                        mBigIconCallback.result(NetworkHelper.this,null);
//                                    }
//                                }
//                            });
//                        }
//                        break;
//
//                    default:
//                        break;
//                }
//            }
//        };
//        
//        
//        public void downloadSmallIconImpl(Context context,String url,NetworkHelperCallback callback){
//            if (null == url || url.length() <1) return;
//            mSmallIconCallback = callback;
//            if (mSmallIconRetryCount>0 && mSmallIconReqId < 0){
//                Log.d(TAG,"downloadSmallIconImpl,"+url+",retry="+mSmallIconRetryCount);
//                HttpTask ht = new HttpTask(
//                        context,
//                        url, 
//                        false, 
//                        HTTP_TYPE_REQUEST_SMALL_ICON, 
//                        null, 
//                        mListener);
//                ht.setPriority(HttpTask.PRIORITY_NORMAL);
//                HttpThreadPoolController.getInstance(context).addHttpTask(ht);
//                mSmallIconReqId = ht.getmSerialId();
//                mSmallIconRetryCount --;
//            }
//        }
//
//        public void cancelSmallIconDownload(Context context){
//            mSmallIconCallback = null;
//            if (mSmallIconReqId != -1){
//                HttpThreadPoolController.getInstance(context).cancelTask(mSmallIconReqId, false);
//                mSmallIconReqId = -1;
//                mSmallIconRetryCount = MAX_RETRY_COUNT;
//            }
//        }
//        
//        public void downloadBigIconImpl(Context context,String url,NetworkHelperCallback callback){
//            if (null == url || url.length() <1) return;
//            mBigIconCallback = callback;
//            if (mBigIconRetryCount>0 && mBigIconReqId < 0){
//                HttpTask ht = new HttpTask(
//                        context,
//                        url, 
//                        false, 
//                        HTTP_TYPE_REQUEST_BIG_ICON, 
//                        null, 
//                        mListener);
//                ht.setPriority(HttpTask.PRIORITY_DOWNLOAD_ICON);
//                HttpThreadPoolController.getInstance(context).addHttpTask(ht);
//                mBigIconReqId = ht.getmSerialId();
//                mBigIconRetryCount --;
//            }
//        }
//
//        public void cancelBigIconDownload(Context context){
//            mBigIconCallback = null;
//            if (mBigIconReqId != -1){
//                HttpThreadPoolController.getInstance(context).cancelTask(mBigIconReqId, false);
//                mBigIconReqId = -1;
//                mBigIconRetryCount = MAX_RETRY_COUNT;
//            }
//        }
//        
//        private byte[] getByteData(HttpEntity entity){
//            byte[] data = null;
//            InputStream is = null;
//            ByteArrayOutputStream os = null;
//            byte[] bs = new byte[1024];
//            int len;
//            try {
//                is = entity.getContent();
//                os = new ByteArrayOutputStream();
//                while ((len = is.read(bs)) != -1) {
//                    os.write(bs, 0, len);
//                }
//                os.flush();
//                data = os.toByteArray();
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (is != null) {
//                    try {
//                        is.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (os != null) {
//                    try {
//                        os.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            return data;
//        }
//        
//        private boolean isUrlAvailable(URI uri) {
//            if (uri == null || uri.getScheme() == null || !uri.getScheme().startsWith("http")) {
//                return false;
//            } else {
//                return true;
//            }
//        }
//    }
}
