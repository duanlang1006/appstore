package com.kapps.market.cache;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;

import com.kapps.market.bean.config.MarketConfig;
import com.kapps.market.log.LogUtil;

/**
 * 2010-8-3 <br>
 * 
 * @author admin
 * 
 */
public class LocaleCacheManager {

	public static final String TAG = "LocaleCacheManager";

	// ����Ŀ¼
	private String cacheDir;
	// ͼ��Ŀ¼
	private String iconDirName = "18";
	// ��ͼĿ¼
	private String screenshotDirName = "19";
	// ���ͼĿ¼
	private String advertiseIconDirName = "20";
	// ���ͼ��Ŀ¼
	private String categoryIconDirName = "21";
	// �г�����
	private MarketConfig marketConfig;
	// ���һ�ν������ӵ������ʱ���
	private long lastSave;

	public LocaleCacheManager(Context context, MarketConfig marketConfig) {
		this.marketConfig = marketConfig;

		cacheDir = context.getCacheDir().getAbsolutePath();

		// ͼ�껺��
		File file = new File(cacheDir + File.separator + iconDirName);
		if (!file.exists()) {
			file.mkdirs();
		}
		// ��ͼ����
		file = new File(cacheDir + File.separator + screenshotDirName);
		if (!file.exists()) {
			file.mkdirs();
		}
		// ��滺��
		file = new File(cacheDir + File.separator + advertiseIconDirName);
		if (!file.exists()) {
			file.mkdirs();
		}
		// ���ͼ��Ŀ¼
		file = new File(cacheDir + File.separator + categoryIconDirName);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * д��ͼ��
	 * 
	 * @param iconName
	 * @param obj
	 */
	public void writeIconToCache(int appId, Serializable obj) {
		writeDataToCache(iconDirName + File.separator + appId, obj);
	}

	/**
	 * д���ͼ
	 * 
	 * @param screenshotName
	 * @param obj
	 */
	public void writeScreenshotToCache(String url, Serializable obj) {
		if (url != null) {
			writeDataToCache(screenshotDirName + File.separator + url.hashCode(), obj);
		}
	}

	/**
	 * д����
	 */
	public void writeAdvertiseIconToCache(int appId, Serializable obj) {
		writeDataToCache(advertiseIconDirName + File.separator + appId, obj);
	}

	/**
	 * ���ͼ��
	 * 
	 * @param categoryId
	 * @param obj
	 */
	public void writeCategoryIconToCache(int categoryId, Serializable obj) {
		writeDataToCache(categoryIconDirName + File.separator + categoryId, obj);
	}

	/**
	 * д����ͨ���
	 * 
	 * @param name
	 * @param obj
	 */
	public void writeDataToCache(String name, Object obj) {
		lastSave = System.currentTimeMillis();

		byte[] data = serializeObject(obj);
		if (data != null) {
			BufferedOutputStream bos = null;
			try {
				File file = new File(cacheDir + File.separator + name);
				bos = new BufferedOutputStream(new FileOutputStream(file));
				bos.write(data);
				bos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	/**
	 * ���һ�㻺�����
	 * 
	 * @param name
	 * @return
	 */
	public Object getDataFormCache(String name) {
		File file = new File(cacheDir + File.separator + name);
		if (file.exists()) {
			return deserializeObject(file);
		}
		return null;
	}

	/**
	 * ����Ƿ����
	 * 
	 * @param name
	 *            ������� see: CacheConstants
	 * @param cacheTime
	 *            ����ʱ��
	 * @return
	 */
	public boolean isDataExpired(String name, long cacheTime) {
		File file = new File(cacheDir + File.separator + name);
		return (System.currentTimeMillis() - file.lastModified() > cacheTime);
	}

	/**
	 * ����Ƿ���ڲ��������
	 * 
	 * @param name
	 *            ����
	 * @param cacheTime
	 *            ����ʱ��
	 * @return
	 */
	public Object getDataCheckExpired(String name, long cacheTime) {
		if (isDataExpired(name, cacheTime)) {
			deleteCacheData(name);
			return null;

		} else {
			return getDataFormCache(name);
		}
	}

	/**
	 * ���ͼ��
	 * 
	 * @param appId
	 * @return
	 */
	public byte[] getIconFromCache(int appId) {
		File file = new File(cacheDir + File.separator + iconDirName + File.separator + appId);
		if (file.exists()) {
			return (byte[]) deserializeObject(file);
		} else {
			return null;
		}
	}

	/**
	 * ͼ���Ƿ����
	 * 
	 * @param appId
	 * @return
	 */
	public boolean isIconExist(int appId) {
		File file = new File(cacheDir + File.separator + iconDirName + File.separator + appId);
		return file.exists();
	}

	/**
	 * ���ͼƬ�Ƿ����
	 * 
	 * @param appId
	 * @return
	 */
	public byte[] getAdvertiseIconFromCache(int appId) {
		File file = new File(cacheDir + File.separator + advertiseIconDirName + File.separator + appId);
		if (file.exists()) {
			return (byte[]) deserializeObject(file);
		} else {
			return null;
		}
	}

	/**
	 * ���ͼ�Ƿ��Ѿ�����
	 * 
	 * @param appId
	 * @return
	 */
	public boolean isAdvertiseIconExist(int appId) {
		File file = new File(cacheDir + File.separator + advertiseIconDirName + File.separator + appId);
		return file.exists();
	}

	/**
	 * ��ý�ͼ
	 * 
	 * @param url
	 * @return
	 */
	public byte[] getScreenShotFromCache(String url) {
		if (url == null) {
			return null;
		}
		File file = new File(cacheDir + File.separator + screenshotDirName + File.separator + url.hashCode());
		if (file.exists()) {
			return (byte[]) deserializeObject(file);
		} else {
			return null;
		}
	}

	/**
	 * ͼ���Ƿ����
	 * 
	 * @param appId
	 * @return
	 */
	public boolean isScreenShotExist(String url) {
		File file = new File(cacheDir + File.separator + screenshotDirName + File.separator + url.hashCode());
		return file.exists();
	}

	/**
	 * ���ͼ���Ƿ����
	 */
	public byte[] getCategoryIconFromCache(int categoryId) {
		File file = new File(cacheDir + File.separator + categoryIconDirName + File.separator + categoryId);
		if (file.exists()) {
			return (byte[]) deserializeObject(file);
		} else {
			return null;
		}
	}

	/**
	 * ���ͼ���Ƿ����
	 */
	public boolean isCategoryIconExist(int categoryId) {
		File file = new File(cacheDir + File.separator + categoryIconDirName + File.separator + categoryId);
		return file.exists();
	}

	/**
	 * ɾ��һ�������ļ�
	 */
	public boolean deleteCacheData(String name) {
		File file = new File(cacheDir + File.separator + name);
		return file.delete();
	}

	/**
	 * ���л�����
	 * 
	 * @param obj
	 * @return
	 */
	private byte[] serializeObject(Object obj) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bos.toByteArray();
	}

	/**
	 * �����ж���
	 * 
	 * @param data
	 * @return
	 */
	private Object deserializeObject(byte[] data) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * �����ж���
	 * 
	 * @param data
	 * @return
	 */
	private Object deserializeObject(File file) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return the lastSave
	 */
	public long getLastSave() {
		return lastSave;
	}

	/**
	 * ���ͼ�껺�棬����Ԥ��Ľ�������û��ʹ�õ�ͼ��
	 */
	public void checkLocalIconCache() {
		int maxCacheSize = marketConfig.getIconCacheSize();
		File file = new File(cacheDir + File.separator + iconDirName);
		File[] files = file.listFiles();
		doCheckImageCache(files, maxCacheSize);
	}

	/**
	 * ��鱾�ؽ�ͼ����
	 */
	public void checkLocalShotCache() {
		int maxCacheSize = marketConfig.getShotCacheSize();
		File file = new File(cacheDir + File.separator + screenshotDirName);
		File[] files = file.listFiles();
		doCheckImageCache(files, maxCacheSize);
	}

	/**
	 * ��鱾�ع��ͼƬ
	 */
	public void checkLocalAdvertiseIconCache() {
		int maxCacheSize = marketConfig.getAdvertiseCacheSize();
		File file = new File(cacheDir + File.separator + advertiseIconDirName);
		File[] files = file.listFiles();
		doCheckImageCache(files, maxCacheSize);
	}

	/**
	 * ��鱾�����ͼ��
	 */
	public void checkLocalCategoryIconCache() {
		int maxCacheSize = marketConfig.getAdvertiseCacheSize();
		File file = new File(cacheDir + File.separator + categoryIconDirName);
		File[] files = file.listFiles();
		doCheckImageCache(files, maxCacheSize);
	}

	private void doCheckImageCache(File[] files, int maxCacheSize) {
		// ��ɾ����ڵ�ͼƬ
		long cacheTime = marketConfig.getImageCacheTime();
		long nowTime = System.currentTimeMillis();
		int nowCacheSize = 0;
		for (File cachedFile : files) {
			if (cachedFile.exists()) {
				if (nowTime - cachedFile.lastModified() > cacheTime) {
					cachedFile.delete();
				} else {
					nowCacheSize += cachedFile.length();
				}
			}
		}

		// ���ٵ�ԭ����4��3
		if (nowCacheSize > maxCacheSize) {
			int useSize = maxCacheSize / 4 * 3;
			long fileLength = 0;
			for (File cachedFile : files) {
				if (cachedFile.exists()) {
					fileLength = cachedFile.length();
					if (nowCacheSize - fileLength > useSize) {
						cachedFile.delete();
						nowCacheSize -= fileLength;
					}
				}
			}
		}

		LogUtil.v(TAG, "maxCachSize: " + maxCacheSize + " nowCacheSize: " + nowCacheSize + " fileCount: "
				+ files.length + " useSize: " + (maxCacheSize / 4 * 3));
	}

	/**
	 * �����ͼƬ
	 */
	public void clearAdvertiseCache() {
		File file = new File(cacheDir + File.separator + advertiseIconDirName);
		File[] files = file.listFiles();
		for (File cachedFile : files) {
			cachedFile.delete();
		}
	}

	/**
	 * �������ͼƬ�Ļ���
	 */
	public void clearAllImageCache() {
		// ͼ��
		File file = new File(cacheDir + File.separator + iconDirName);
		File[] files = file.listFiles();
		for (File cachedFile : files) {
			cachedFile.delete();
		}

		// ��ͼ
		file = new File(cacheDir + File.separator + screenshotDirName);
		files = file.listFiles();
		for (File cachedFile : files) {
			cachedFile.delete();
		}

		// ���
		file = new File(cacheDir + File.separator + advertiseIconDirName);
		files = file.listFiles();
		for (File cachedFile : files) {
			cachedFile.delete();
		}
	}

}
