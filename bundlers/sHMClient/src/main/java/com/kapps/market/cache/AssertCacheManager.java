package com.kapps.market.cache;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.kapps.market.bean.MImageType;
import com.kapps.market.util.Util;

/**
 * 2010-7-25<br>
 * 
 * @author admin
 * 
 */
public class AssertCacheManager {

	public static final String TAG = "AssertCacheManager";
	public static final boolean logCheck = false;

	// Ӧ�ý�ͼ���棬��ͼƬ��url��hashCode��Ϊ��ֵ
	private HashMap<Integer, DataWraper> screenshotsWraperCache = new HashMap<Integer, DataWraper>();

	// ͼ�껺�� key: appId
	private HashMap<Integer, DataWraper> appIconWraperCache = new HashMap<Integer, DataWraper>();

	// ���ͼ����
	private HashMap<Integer, DataWraper> advertiseWraperCache = new HashMap<Integer, DataWraper>();

	// ���ͼ��
	private HashMap<Integer, DataWraper> categoryIconWraperCache = new HashMap<Integer, DataWraper>();

	// ���ػ���
	private LocaleCacheManager localeCacheManager;
	// ���ձ���ͼ��
	private int MIN_CACHE_ICON = 25;
	// ���ͼ��
	private int MIN_CACHE_ADVERTISE_ICON = 15;
	// ���ձ�����ͼ5
	private int MIN_CACHE_SHOT = 5;
	// �����
	private int CHECK_PERIOD = 1000 * 15;
	// ���泤ʱ��û��ʹ����ô�������
	private long MAX_IDLE = 1000 * 60;
	// ���һ��ʹ��
	private long lastVisit = 0;
	// �ڴ滺���� ÿ��CHECK_PERIOD����һ��
	private Timer timer;

	public AssertCacheManager(LocaleCacheManager localeCacheManager) {
		this.localeCacheManager = localeCacheManager;

		timer = new Timer(true);
		timer.scheduleAtFixedRate(new AssertChecker(), 1000 * 20, CHECK_PERIOD);
	}

	/**
	 * ���ͼƬ
	 * 
	 * @param appId
	 *            Ӧ�õ�id
	 * @param bytes
	 *            ͼƬ���
	 */
	public void addAppIconByteToCache(int appId, byte[] bytes) {
		if (bytes != null) {
			appIconWraperCache.put(appId, new DataWraper(bytes, System.currentTimeMillis()));
		}
	}

	/**
	 * ���Ӧ�õ�ͼ��
	 * 
	 * @param appId
	 * @return
	 */
	public Drawable getAppIconFromCache(int appId) {
		return getAppIconFromCache(appId, false);
	}

	/**
	 * ���Ӧ�õ�ͼ��
	 * 
	 * @param appId
	 * @return
	 */
	public Drawable getAppIconFromCache(int appId, boolean checkLocal) {
		lastVisit = System.currentTimeMillis();
		DataWraper dataWraper = appIconWraperCache.get(appId);
		if (dataWraper != null) {
			dataWraper.setLastVisit(lastVisit);
			byte[] buf = dataWraper.getData();
			return new BitmapDrawable(new ByteArrayInputStream(buf));

		} else if (checkLocal) {
			byte[] buf = localeCacheManager.getIconFromCache(appId);
			if (buf != null) {
				addAppIconByteToCache(appId, buf);
				return new BitmapDrawable(new ByteArrayInputStream(buf));
			}
		}
		return null;
	}

	/**
	 * ��ͼƬ��Դ���뻺��
	 * 
	 * @param url
	 * @param bytes
	 */
	public void addAdvertiseIconByteToCache(int appId, byte[] bytes) {
		if (bytes != null) {
			advertiseWraperCache.put(appId, new DataWraper(bytes, System.currentTimeMillis()));
		}
	}

	/**
	 * ���Ӧ�õ�ͼ��
	 * 
	 * @param appId
	 * @return
	 */
	public Drawable getAdvertiseIconFromCache(int appId) {
		return getAdvertiseIconFromCache(appId, false);
	}

	/**
	 * ���Ӧ�õ�ͼ��
	 * 
	 * @param appId
	 * @return
	 */
	public Drawable getAdvertiseIconFromCache(int appId, boolean checkLocal) {
		lastVisit = System.currentTimeMillis();
		DataWraper dataWraper = advertiseWraperCache.get(appId);
		if (dataWraper != null) {
			dataWraper.setLastVisit(lastVisit);
			byte[] buf = dataWraper.getData();
			return new BitmapDrawable(new ByteArrayInputStream(buf));

		} else if (checkLocal) {
			byte[] buf = localeCacheManager.getAdvertiseIconFromCache(appId);
			if (buf != null) {
				addAdvertiseIconByteToCache(appId, buf);
				return new BitmapDrawable(new ByteArrayInputStream(buf));
			}
		}
		return null;
	}

	/**
	 * �������Դ���뻺��
	 * 
	 * @param url
	 * @param bytes
	 */
	public void addCategoryIconByteToCache(int categoryId, byte[] bytes) {
		if (bytes != null) {
			categoryIconWraperCache.put(categoryId, new DataWraper(bytes, System.currentTimeMillis()));
		}
	}

	/**
	 * �������ͼ��
	 * 
	 * @param appId
	 * @return
	 */
	public Drawable getCategoryIconFromCache(int categoryId) {
		return getCategoryIconFromCache(categoryId, false);
	}

	/**
	 * �������ͼ��
	 * 
	 * @param categoryId
	 * @return
	 */
	public Drawable getCategoryIconFromCache(int categoryId, boolean checkLocal) {
		lastVisit = System.currentTimeMillis();
		DataWraper dataWraper = categoryIconWraperCache.get(categoryId);
		if (dataWraper != null) {
			dataWraper.setLastVisit(lastVisit);
			byte[] buf = dataWraper.getData();
			return new BitmapDrawable(new ByteArrayInputStream(buf));

		} else if (checkLocal) {
			byte[] buf = localeCacheManager.getCategoryIconFromCache(categoryId);
			if (buf != null) {
				addCategoryIconByteToCache(categoryId, buf);
				return new BitmapDrawable(new ByteArrayInputStream(buf));
			}
		}
		return null;
	}

	/**
	 * ��ͼƬ��Դ���뻺��
	 * 
	 * @param url
	 * @param bytes
	 */
	public void addScreenshotsByteToCache(String url, byte[] bytes) {
		if (bytes != null) {
			int key = url.hashCode();
			screenshotsWraperCache.put(key, new DataWraper(bytes, System.currentTimeMillis()));
		}

		// ��������Ƿ���
		checkScreenshotCacheCount();
	}

	/**
	 * ��ͼƬ�����л�ÿ��Ի��ƵĶ���
	 * 
	 * @param url
	 */
	public Drawable getScreenshotsFromCache(String url) {
		return getScreenshotsFromCache(url, false, false);
	}

	/**
	 * ��ͼ
	 * 
	 * @param url
	 * @param checkLocal
	 * @param thumb
	 *            �Ƿ�������ͼ
	 * @return
	 */
	public Drawable getScreenshotsFromCache(String url, boolean checkLocal, boolean thumb) {
		int key = url.hashCode();
		lastVisit = System.currentTimeMillis();
		DataWraper dataWraper = screenshotsWraperCache.get(key);
		byte[] buf = null;
		if (dataWraper != null) {
			dataWraper.setLastVisit(lastVisit);
			buf = dataWraper.getData();

		} else if (checkLocal) {
			buf = localeCacheManager.getScreenShotFromCache(url);
			if (buf != null) {
				addScreenshotsByteToCache(url, buf);
			}
		}

		// ���ؿɻ��Ƶģ�ȷʵ�Ƿ�Ϊ����ͼ
		if (buf != null) {
			if (thumb) {
				return Util.getScreenThumb(buf);
			} else {
				return new BitmapDrawable(new ByteArrayInputStream(buf));
			}
		} else {
			return null;
		}
	}

	/**
	 * ͼ���Ƿ����
	 */
	public boolean isItemIconExist(int type, int itemId) {
		if (type == MImageType.APP_ICON || type == MImageType.APK_ICON) {
			return appIconWraperCache.containsKey(itemId);

		} else if (type == MImageType.CATEGORY_ICON) {
			return categoryIconWraperCache.containsKey(itemId);

		} else if (type == MImageType.APP_ADVERTISE_ICON) {
			return advertiseWraperCache.containsKey(itemId);

		} else if (type == MImageType.APP_SCREENSHOT) {
			return screenshotsWraperCache.containsKey(itemId);

		} else {
			return false;
		}

	}

	/**
	 * ������еĻ���
	 */
	public synchronized void clearCacheData() {
		appIconWraperCache.clear();
		screenshotsWraperCache.clear();
		advertiseWraperCache.clear();
		categoryIconWraperCache.clear();
		if (logCheck) {
			Log.v(TAG, "!!!!!!!!!!!!!!clear assert cache");
		}
	}

	/**
	 * ������û�з��ʵ�ͼƬ
	 */
	private synchronized void releaseCahceData() {
		// ���������ʵ� ͼ��
		boolean useMax = isMemaryEnough();
		if (logCheck) {
			Log.v(TAG, "!!!!!!!!!!!!!!useMax: " + useMax);
		}

		ArrayList<Entry<Integer, DataWraper>> imageEntryList = null;

		// ͼ��
		int deleteCount = appIconWraperCache.size() - MIN_CACHE_ICON;
		if (deleteCount > 0) {
			imageEntryList = new ArrayList<Entry<Integer, DataWraper>>(appIconWraperCache.entrySet());
			Collections.sort(imageEntryList, new VisitTimeCompare());
			for (int index = 0; index < deleteCount; index++) {
				appIconWraperCache.remove(imageEntryList.get(index).getKey());
			}
		}

		// ���ͼ
		deleteCount = advertiseWraperCache.size() - MIN_CACHE_ADVERTISE_ICON;
		if (deleteCount > 0) {
			imageEntryList = new ArrayList<Entry<Integer, DataWraper>>(advertiseWraperCache.entrySet());
			Collections.sort(imageEntryList, new VisitTimeCompare());
			for (int index = 0; index < deleteCount; index++) {
				advertiseWraperCache.remove(imageEntryList.get(index).getKey());
			}
		}

		// ���ͼ��
		deleteCount = categoryIconWraperCache.size() - MIN_CACHE_ICON;
		if (deleteCount > 0) {
			imageEntryList = new ArrayList<Entry<Integer, DataWraper>>(categoryIconWraperCache.entrySet());
			Collections.sort(imageEntryList, new VisitTimeCompare());
			for (int index = 0; index < deleteCount; index++) {
				categoryIconWraperCache.remove(imageEntryList.get(index).getKey());
			}
		}

		if (logCheck) {
			Log.v(TAG, "!!!!!!!!!!!!!!release assert cache: icon count: " + appIconWraperCache.size() + " shot count: "
					+ screenshotsWraperCache.size() + " advertise count: " + advertiseWraperCache.size());
		}
	}

	// ���ڽṹ������б���ʵʱ���
	private void checkScreenshotCacheCount() {
		// ���������ʵ�5�Ž�ͼ
		int deleteCount = screenshotsWraperCache.size() - MIN_CACHE_SHOT;
		if (deleteCount > 0) {
			ArrayList<Entry<Integer, DataWraper>> imageEntryList = new ArrayList<Entry<Integer, DataWraper>>(
					screenshotsWraperCache.entrySet());
			Collections.sort(imageEntryList, new VisitTimeCompare());
			for (int index = 0; index < deleteCount; index++) {
				screenshotsWraperCache.remove(imageEntryList.get(index).getKey());
			}
		}
	}

	// ����ڴ��ͷ��㹻�������ôʹ�ô����
	private boolean isMemaryEnough() {
		return false;
	}

	// ����ʱ��Ƚ�
	public class VisitTimeCompare implements Comparator<Entry<Integer, DataWraper>> {

		@Override
		public int compare(Entry<Integer, DataWraper> o1, Entry<Integer, DataWraper> o2) {
			return (int) (o1.getValue().getLastVisit() - o2.getValue().getLastVisit());
		}

	}

	// ��������
	private class AssertChecker extends TimerTask {

		@Override
		public void run() {
			try {
				if (appIconWraperCache.size() > 0 || screenshotsWraperCache.size() > 0
						|| advertiseWraperCache.size() > 0) {
					long checkTime = System.currentTimeMillis();
					if (checkTime - lastVisit > MAX_IDLE) {
						clearCacheData();
					} else {
						releaseCahceData();
					}
					System.gc();
				}

			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	}

	/**
	 * @return the lastVisit
	 */
	public long getLastVisit() {
		return lastVisit;
	}

	// ��ݷ�װ
	private class DataWraper {
		// ���
		private byte[] data;
		// ������
		private long lastVisit;

		/**
		 * @param data
		 * @param lastVisit
		 */
		public DataWraper(byte[] data, long lastVisit) {
			super();
			this.data = data;
			this.lastVisit = lastVisit;
		}

		/**
		 * @return the lastVisit
		 */
		public long getLastVisit() {
			return lastVisit;
		}

		/**
		 * @param lastVisit
		 *            the lastVisit to set
		 */
		public void setLastVisit(long lastVisit) {
			this.lastVisit = lastVisit;
		}

		/**
		 * @return the data
		 */
		public byte[] getData() {
			return data;
		}

	}
}
