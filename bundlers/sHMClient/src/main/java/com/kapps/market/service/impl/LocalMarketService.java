package com.kapps.market.service.impl;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.kapps.market.MApplication;
import com.kapps.market.MarketManager;
import com.kapps.market.NetworkinfoParser;
import com.kapps.market.bean.AppBadness;
import com.kapps.market.bean.AppCategory;
import com.kapps.market.bean.AppComment;
import com.kapps.market.bean.AppDetail;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.AppPermission;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.CommentMark;
import com.kapps.market.bean.DownloadItem;
import com.kapps.market.bean.KeyWord;
import com.kapps.market.bean.LoginResult;
import com.kapps.market.bean.MImageType;
import com.kapps.market.bean.MarketUpdateInfo;
import com.kapps.market.bean.PageableResult;
import com.kapps.market.bean.Software;
import com.kapps.market.bean.StaticAD;
import com.kapps.market.bean.UserInfo;
import com.kapps.market.bean.config.ContextConfig;
import com.kapps.market.bean.config.MarketConfig;
import com.kapps.market.cache.CacheConstants;
import com.kapps.market.cache.LocaleCacheManager;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.service.ILocalService;
import com.kapps.market.service.IMarketService;
import com.kapps.market.store.MDBHelper;
import com.kapps.market.store.MDBHelper.TAppDownload;
import com.kapps.market.task.ApkSimpleParser;
import com.kapps.market.task.ApkSimpleParser.ApkDetail;
import com.kapps.market.task.ApkSimpleParser.ApkSummary;
import com.kapps.market.util.SecurityUtil;
import com.kapps.market.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2010-6-8 <br>
 * real API manager??
 * @author Administrator
 * 
 */
public class LocalMarketService implements IMarketService, ILocalService {

	public static final String TAG = "LocalMarketService";

	private MApplication marketContext;

	private LocaleCacheManager localeCacheManager;

	// �г�����
	private MarketConfig marketConfig;

	// ��������ݿ����
	private HttpMarketService chainService;

	public LocalMarketService(MApplication marketContext, HttpMarketService chainService) {
		this.marketContext = marketContext;
		this.chainService = chainService;
		this.marketConfig = marketContext.getMarketConfig();
		this.localeCacheManager = marketContext.getLocaleCacheManager();
	}

	@Override
	public LoginResult login(UserInfo user, String deviceId, String simId, String sign) throws ActionException {
		return chainService.login(user, deviceId, simId, sign);
	}

	@Override
	public void register(UserInfo user, String sign) throws ActionException {
		chainService.register(user, sign);
	}

	@Override
	public PageableResult getAppListByCategory(int categoryId, int sortType, int feeType, int pageIndex, int perCount)
			throws ActionException {
		return chainService.getAppListByCategory(categoryId, sortType, feeType, pageIndex, perCount);
	}

	@Override
	public PageableResult getAppListByTopDownload(int sorttype, int pi, int ps) throws ActionException {
		return chainService.getAppListByTopDownload(sorttype, pi, ps);
	}

	@Override
	public PageableResult getAppListByRecommend(int recommendId, int sortType, int pageIndex, int perCount)
			throws ActionException {
		return chainService.getAppListByRecommend(recommendId, sortType, pageIndex, perCount);
	}

	@Override
	public PageableResult getNewsAppList(int pageIndex, int perCount) throws ActionException {
		return chainService.getNewsAppList(pageIndex, perCount);
	}

	@Override
	public PageableResult getAdvertiseApps(int poptype, int pageIndex, int perCount) throws ActionException {
		return chainService.getAdvertiseApps(poptype, pageIndex, perCount);
	}

	@Override
	public PageableResult getAppListByDeveloper(String developer, int start, int count) throws ActionException {
		return chainService.getAppListByDeveloper(developer, start, count);
	}

	@Override
	public PageableResult getHistoryAppList(int appId, String pname) throws ActionException {
		return chainService.getHistoryAppList(appId, pname);
	}

	@Override
	public PageableResult searchAppByCondition(int type, String key, int pageIndex, int perCount)
			throws ActionException {
		return chainService.searchAppByCondition(type, key, pageIndex, perCount);
	}

	@Override
	public AppDetail getAppDetailById(int appId) throws ActionException {
		return chainService.getAppDetailById(appId);
	}

	@Override
	public List<AppCategory> getAppCategoryList() throws ActionException {
		return chainService.getAppCategoryList();
	}

	@Override
	public PageableResult getAppCommentList(int appId, String pname, int pageIndex, int perCount)
			throws ActionException {
		return chainService.getAppCommentList(appId, pname, pageIndex, perCount);
	}

	@Override
	public void commitAppComment(AppComment appComment, String pname, String sign) throws ActionException {
		chainService.commitAppComment(appComment, pname, sign);
	}

	@Override
	public CommentMark commitAppCommentMark(int commentId, int mark) throws ActionException {
		return chainService.commitAppCommentMark(commentId, mark);
	}

	@Override
	public void commitBadnessContent(AppBadness appBadness, String sign) throws ActionException {
		chainService.commitBadnessContent(appBadness, sign);
	}

	@Override
	public List<AppPermission> getAppPermissionList(int appId) throws ActionException {
		return chainService.getAppPermissionList(appId);
	}

	@Override
	public byte[] getAppImageResource(int iconId, String url, int type, String sign) throws ActionException {
		Log.d("getAppImageResource.....", "type:"+type+ " url:"+url);
		
		//url = url.replaceAll("\\", "/");
		byte[] data = null;
		if (type == MImageType.APP_ICON || type == MImageType.APK_ICON) {
			data = localeCacheManager.getIconFromCache(iconId);

		} else if (type == MImageType.APP_SCREENSHOT) {
			data = localeCacheManager.getScreenShotFromCache(url);

		} else if (type == MImageType.APP_ADVERTISE_ICON) {
			data = localeCacheManager.getAdvertiseIconFromCache(iconId);

		} else if (type == MImageType.CATEGORY_ICON) {
			data = localeCacheManager.getCategoryIconFromCache(iconId);
		}
		// Log.d("getAppImageResource", "url: " + url + " type: " + type);
		if (data != null) {
			return data;

		} else {
			if (url != null) {
				// ���ͼ�궼���أ���ͼ���������ֶ����أ�������Ҫ����ͼ����ơ�
				if (type == MImageType.APK_ICON) {
					data = createApkIcon(marketContext, url);

				} else if (NetworkinfoParser.isRawNetConnect(marketContext)
						&& (type != MImageType.APP_ICON || (type == MImageType.APP_ICON && marketConfig.isLoadAppIcon()))) {
					data = chainService.getAppImageResource(iconId, url, type, sign);
				}
			}

			if (data != null) {
				// ���ͼƬ�����ô��С��
				if (data.length > MarketConfig.MAX_IMAGE_SIZE) {
					data = Util.getScreenThumbBytes(data);
				}

				if (type == MImageType.APP_ICON || type == MImageType.APK_ICON) {
					localeCacheManager.writeIconToCache(iconId, data);

				} else if (type == MImageType.APP_SCREENSHOT) {
					localeCacheManager.writeScreenshotToCache(url, data);

				} else if (type == MImageType.APP_ADVERTISE_ICON) {
					localeCacheManager.writeAdvertiseIconToCache(iconId, data);

				} else if (type == MImageType.CATEGORY_ICON) {
					localeCacheManager.writeCategoryIconToCache(iconId, data);
				}
			}

			return data;
		}
	}

	// ���apkͼ��
	private byte[] createApkIcon(Context context, String path) {
		try {
			LogUtil.d("createApkIcon", "....................................");
			Drawable drawable = ApkSimpleParser.parseApkDrawableo(context, new File(path));
			if (drawable != null) {
				Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				return out.toByteArray();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public AppItem getAppSummary(String pname, int versionCode) throws ActionException {
		return chainService.getAppSummary(pname, versionCode);
	}

	@Override
	public AppItem getAppSummaryById(int appId) throws ActionException {
		return chainService.getAppSummaryById(appId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AppItem> checkSoftwareUpdate(List<Software> softwareList) throws ActionException {
		List<AppItem> itemList = (List<AppItem>) localeCacheManager.getDataCheckExpired(
				CacheConstants.SOFTWARE_UPDATE_INFO, marketConfig.getUpdateInfoCacheTime());
		if (itemList == null) {
			itemList = chainService.checkSoftwareUpdate(softwareList);
		}

		// ��ӵ����ػ���
		if (itemList != null) {
			Software software = null;
			boolean find = false;
			for (AppItem appItem : itemList) {
				// ���������ù��ı�־
				software = marketContext.getMarketManager().getSoftware(appItem.getPackageName());
				if (software != null) {
					if (!find) {
						find = true;
					}
					software.setUpdate(true);
				}
			}
			if (find) {
				Collections.sort(softwareList, new UpdatableComparator());
				localeCacheManager.writeDataToCache(CacheConstants.SOFTWARE_UPDATE_INFO, itemList);
			}
		}

		return itemList;
	}

	@Override
	public MarketUpdateInfo checkMarketUpdate(ContextConfig contextConfig) throws ActionException {
		MarketUpdateInfo marketUpdateInfo = (MarketUpdateInfo) localeCacheManager.getDataCheckExpired(
				CacheConstants.MARKET_UPDATE_INFO, marketConfig.getUpdateInfoCacheTime());
		if (marketUpdateInfo != null) {
			// ���汾�Ѿ�������ʾ��
			try {
				int newVersion = marketUpdateInfo.getVersionCode();
				int localVersion = contextConfig.getVersionCode();
				if (localVersion >= newVersion) {
					localeCacheManager.deleteCacheData(CacheConstants.MARKET_UPDATE_INFO);
					marketUpdateInfo = null;
				}
			} catch (Exception e) {
			}

		}

		if (marketUpdateInfo == null) {
			marketUpdateInfo = chainService.checkMarketUpdate(contextConfig);
		}

		if (marketUpdateInfo != null) {
			marketUpdateInfo.setFree(true);

			// д�뱾�ػ���
			localeCacheManager.writeDataToCache(CacheConstants.MARKET_UPDATE_INFO, marketUpdateInfo);
		}

		return marketUpdateInfo;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * ����ֻ��û���Ϣ����ϸ��Ϣ����������������ٻ�ȡ
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Software> initSoftwareSummaryInfoList() {
		List<Software> cacheList = (List<Software>) localeCacheManager.getDataFormCache(CacheConstants.SOFTWARE_INFO);
		LogUtil.d(TAG, "initSoftwareList.............cacheSize: " + (cacheList == null ? 0 : cacheList.size()));
		// ���������Ѿ���װ�����
		PackageManager pm = marketContext.getPackageManager();
		List<PackageInfo> mApps = pm.getInstalledPackages(0);
		List<Software> softwareList = new ArrayList<Software>();
		Software software = null;
		for (PackageInfo info : mApps) {
			if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
					&& !info.packageName.equals(marketContext.getPackageName())) {
				if (cacheList != null) {
					software = getOldFromSoftList(cacheList, info);
				}
				if (software == null) {
					software = new Software();
					software.setState(BaseApp.APP_INSTALLED);
					software.setPackageName(info.packageName);
					software.setVersion(info.versionName);
					software.setVersionCode(info.versionCode);
					software.setName(info.packageName);
					// id����Ϊpackage��version��hashCode
					software.setId((software.getPackageName() + software.getVersionCode()).hashCode());

					// �������������±�־��Ϊ�����Ϣ�����Ѿ���ʱ
				} else {
					software.setUpdate(false);
				}

				// ����ʵʱ���¿ɱ�Ԫ��
				software.setApkPath(info.applicationInfo.sourceDir);
				// �������ͼ��Ҫ��apk�ļ��л�ȡ������ͼ���ַ����Ϊapk��·����
				software.setIconUrl(software.getApkPath());
				softwareList.add(software);
				software = null;
			}
		}
		return softwareList;
	}

	@Override
	public List<Software> initSoftwareDetailInfoList(List<Software> softwareList) {
		LogUtil.d(TAG, "begin load software more info..................");
		LocaleCacheManager localeCacheManager = marketContext.getLocaleCacheManager();
		PackageManager pm = marketContext.getPackageManager();
		ApplicationInfo appInfo = null;
		BitmapDrawable icon = null;
		byte[] data = null;
		Software software = null;
		// ע�������ʵ�֣�û��ʹ����ǿforѭ��������һ���̶��ϱ�����߳��ճɵ��쳣��
		for (int index = 0; index < softwareList.size(); index++) {
			software = softwareList.get(index);
			if (!software.isInfoFull()) {
				try {
					appInfo = pm.getPackageInfo(software.getPackageName(), 0).applicationInfo;
					// ��ͼ�걣�ֵ�����
					// ��黺���Ƿ��Ѿ�����ͼƬ��
					if (!localeCacheManager.isIconExist(software.getId())) {
						icon = (BitmapDrawable) pm.getApplicationIcon(appInfo);
						data = Util.getDrawableBytes(icon);
						if (data != null) {
							localeCacheManager.writeIconToCache(software.getId(), data);
						}
					}
					software.setName(pm.getApplicationLabel(appInfo).toString());
					software.setSize((int) (new File(appInfo.sourceDir).length() / 1024));
					software.setApkPath(appInfo.sourceDir);
					software.setInfoFull(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// �����뱾�ػ���
		localeCacheManager.writeDataToCache(CacheConstants.SOFTWARE_INFO, softwareList);

		return softwareList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Software> initApkSummaryInfoList(List<Software> oldList, String cacheMark, String dir) {
		LogUtil.d(TAG, "initApkSummaryInfoList..............cacheMark: " + cacheMark);
		List<Software> apkList = new ArrayList<Software>();
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			return apkList;
		}

		// ����ʹ�ñ��ػ���
		if (oldList == null) {
			oldList = (List<Software>) localeCacheManager.getDataFormCache(cacheMark);
			LogUtil.d(TAG, "initApkSummaryInfoList  local cache: " + (oldList == null ? null : oldList.size()));
		} else {
			LogUtil.d(TAG, "initApkSummaryInfoList  exist list: " + (oldList == null ? null : oldList.size()));
		}

		// �Ƿ��Ѿ���sdcard�ĸ�·���ˡ�
		File useFile = null;
		if (dir.indexOf(File.separator) != 0) {
			useFile = new File(Environment.getExternalStorageDirectory(), dir);
		} else {
			useFile = new File(dir);
		}
		if (useFile.exists()) {
			// ����һ��Ŀ¼
			File files[] = useFile.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return (pathname.isFile() && pathname.getName().endsWith(".apk"));
				}
			});
			ApkSummary info = null;
			Software apk = null;
			for (File apkFile : files) {
				try {
					info = ApkSimpleParser.parseApkSummaryInfo(apkFile);
					// �ų��޷��������Ϣ��apk
					if (info.packageName == null || info.versionName == null) {
						continue;
					}

					if (oldList != null) {
						apk = getOldFromSoftList(oldList, info);
					}
					if (apk == null) {
						apk = new Software();
						apk.setState(BaseApp.APP_DOWNLOADED);
						apk.setPackageName(info.packageName);
						apk.setName(info.packageName);
						apk.setVersion(info.versionName);
						apk.setVersionCode(info.versionCode);
						// id����Ϊpackage��version��hashCode
						apk.setId((apk.getPackageName() + apk.getVersionCode()).hashCode());
					}

					// ����ʵʱ���¿ɱ�Ԫ��
					apk.setApkPath(apkFile.getAbsolutePath());
					apkList.add(apk);
					apk = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return apkList;
	}

	/**
	 * ��ʼapk��ϸ���б�
	 * 
	 * @param apkList
	 *            apk�б�
	 */
	@Override
	public List<Software> initApkDetailInfoList(List<Software> apkList, String cacheMark) {
		LocaleCacheManager localeCacheManager = marketContext.getLocaleCacheManager();
		ApkDetail apkDetail = null;
		byte[] data = null;
		File file = null;
		Software apk = null;
		// ע�������ʵ�֣�û��ʹ����ǿforѭ��������һ���̶��ϱ�����߳��ճɵ��쳣��
		for (int index = 0; index < apkList.size(); index++) {
			apk = apkList.get(index);
			if (!apk.isInfoFull()) { // ֻ����û�м��ع������Ϣ�����
				try {
					// ��黺���Ƿ��Ѿ�����ͼƬ��
					if (!localeCacheManager.isIconExist(apk.getId())) {
						apkDetail = ApkSimpleParser.parsetApkDetailInfo(marketContext, new File(apk.getApkPath()),
								apk.getPackageName(), true);
						data = Util.getDrawableBytes((BitmapDrawable) apkDetail.icon);
						if (data != null) {
							localeCacheManager.writeIconToCache(apk.getId(), data);
						}

					} else {
						apkDetail = ApkSimpleParser.parsetApkDetailInfo(marketContext, new File(apk.getApkPath()),
								apk.getPackageName(), false);
					}

					// ����Ϣ
					file = new File(apk.getApkPath());
					// kb
					apk.setSize((int) (file.length() / 1024));
					// ����ʵʱ���¿ɱ�Ԫ��
					apk.setApkPath(file.getAbsolutePath());
					apk.setTime(Util.dateFormat(file.lastModified()));
					apk.setName(apkDetail.label.toString());
					apk.setInfoFull(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// �����뱾�ػ���
		localeCacheManager.writeDataToCache(cacheMark, apkList);

		return apkList;
	}

	private Software getOldFromSoftList(List<Software> oldList, ApkSummary newInfo) {
		try {
			for (Software software : oldList) {
				if (software.getPackageName().equals(newInfo.packageName)
						&& software.getVersionCode() == newInfo.versionCode) {
					return software;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	private Software getOldFromSoftList(List<Software> oldList, PackageInfo newInfo) {
		try {
			for (Software software : oldList) {
				if (software.getPackageName().equals(newInfo.packageName)
						&& software.getVersionCode() == newInfo.versionCode) {
					return software;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public Map<Integer, List<DownloadItem>> initDownloadTaskMap() {
		MDBHelper dbHelper = marketContext.getMdbHelper();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TAppDownload.TABLE_NAME, null, null, null, null, null, null);
		Map<Integer, List<DownloadItem>> dMap = new HashMap<Integer, List<DownloadItem>>();
		dMap.put(BaseApp.APP_DOWNLOADED, new ArrayList<DownloadItem>());
		dMap.put(BaseApp.APP_DOWNLOAD_STOP, new ArrayList<DownloadItem>());
		dMap.put(BaseApp.APP_DOWNLOADING, new ArrayList<DownloadItem>());

		DownloadItem downloadItem = null;
		MarketManager marketManager = marketContext.getMarketManager();
		// ��ǰ��Ҫ���ļ���
		List<Integer> fileIdist = new ArrayList<Integer>();
		while (cursor.moveToNext()) {
			downloadItem = new DownloadItem();
			downloadItem.setId((int) cursor.getLong(0));
			downloadItem.setAppId(cursor.getInt(1));
			downloadItem.setPackageName(cursor.getString(2));
			downloadItem.setName(cursor.getString(3));
			downloadItem.setVersion(cursor.getString(4));
			downloadItem.setVersionCode(cursor.getInt(5));
			// �� base64
			downloadItem.setApkPath(SecurityUtil.decodeBase64(cursor.getString(6)));
			downloadItem.setIconUrl(SecurityUtil.decodeBase64(cursor.getString(7)));
			downloadItem.setState(cursor.getInt(8));
			downloadItem.setdSize(cursor.getInt(9));
			downloadItem.setSize(cursor.getInt(10));
			// ʹ��Ĭ�ϱ���Ŀ¼
			String saveDir = marketManager.getSoftwareSaveDir();
			downloadItem.setSavePath(marketManager.getSoftwareSavePath(saveDir, downloadItem.getAppId()));
			dMap.get(downloadItem.getState()).add(downloadItem);
			// LogUtil.d(TAG, "download task: " + software);
			fileIdist.add(downloadItem.getAppId());
		}
		cursor.close();
		dbHelper.close();

		// TODO beta 3 ��鲢ɾ����Ч�����ص�apk��Ϣ
		String saveDir = marketManager.getSoftwareSaveDir();
		if (saveDir != null) {
			try {
				File dir = new File(saveDir);
				File[] files = dir.listFiles();
				for (File file : files) {
					if (!fileIdist.contains(Integer.parseInt(file.getName()))) {
						LogUtil.d(TAG, "**** delete involid file: " + file);
						file.delete();
					}
				}
			} catch (Exception e) {
			}
		}

		return dMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hiapk.market.service.ILocalService#checkLocalCache()
	 */
	@Override
	public void checkLocalCache() {
		// ���ͼ�껺��
		try {
			localeCacheManager.checkLocalIconCache();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ����ͼ����
		try {
			localeCacheManager.checkLocalShotCache();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ���и��µ��ŵ�ǰ��
	private class UpdatableComparator implements Comparator<Software> {

		@Override
		public int compare(Software o1, Software o2) {
			if ((o1.isUpdate() && o2.isUpdate()) || (!o1.isUpdate() && !o2.isUpdate())) {
				return 0;
			} else if (o1.isUpdate()) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	@Override
	public void addFavorAppItem(int appId, String sign) throws ActionException {
		chainService.addFavorAppItem(appId, sign);
	}

	@Override
	public void deleteFavorAppItem(int appId, String sign) throws ActionException {
		chainService.deleteFavorAppItem(appId, sign);
	}

	@Override
	public PageableResult getAppFavorList(int pageIndex, int perCount) throws ActionException {
		return chainService.getAppFavorList(pageIndex, perCount);
	}

	@Override
	public void reportChannel(String did, String cid, long createTime, String sign) throws ActionException {
		chainService.reportChannel(did, cid, createTime, sign);
	}

	@Override
	public StaticAD checkStaticAD(long oldAdId) throws ActionException {
		return chainService.checkStaticAD(oldAdId);
	}

	@Override
	public Integer commitDownloadRecord(String id, String sign, String ei,
			String si) throws ActionException {
		return	chainService.commitDownloadRecord(id, sign, ei, si);
		
	}

	@Override
	public List<KeyWord> getSearchKeyword() throws ActionException {
		
		return chainService.getSearchKeyword();
	}

	@Override
	public Integer commitMarketDownloadRecord(Integer id, String sign, String ei)
			throws ActionException {		
		return chainService.commitMarketDownloadRecord(id, sign, ei);
	}

	@Override
	public Integer commitMarketDownloadRecordFirst(int vcode, String sign)
			throws ActionException {
		
		return chainService.commitMarketDownloadRecordFirst(vcode, sign);
	}

	@Override
	public PageableResult getAppListByTopDownloadFirstPage(int sorttype,
			int pi, int ps) throws ActionException {
		// TODO Auto-generated method stub
		return chainService.getAppListByTopDownloadFirstPage(sorttype, pi, ps);
	}

}
