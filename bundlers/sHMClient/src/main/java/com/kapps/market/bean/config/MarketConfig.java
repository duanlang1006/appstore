package com.kapps.market.bean.config;

/**
 * 2010-8-2 <br>
 *
 * 
 * @author admin
 * 
 */
public class MarketConfig {
	// ����ͼ�����ñ�ʾ
	public static final String LOAD_APP_ICON = "load_app_icon";
	public static final String LOAD_APP_SCREENSHOT = "load_app_screenshot";
	// �Ƿ��Զ�ִ���������
	public static final String AUTO_CHECK_SOFTWARE_UPDATE = "auto_check_software_update";
	public static final String AUTO_INSTALL = "auto_install";
	public static final String BACK_MORE_FUNC = "back_more_func";
	public static final String LOCAK_APK_DIR = "local_apk_dir";
	// ��̬���
	public static final String STATIC_AD_ID = "static_ad_id";

	// ��������ʱ
	public static final int CHECK_UPDATE_DELAY = 1000 * 35;
	public static final int CHECK_UPDATE_INTERVAL = 1000 * 60 * 60 * 6;
	// ע����ͼ��С����һ������ô������Ҫ��������ȣ������ڴ����
	public static final int MAX_IMAGE_SIZE = 1024 * 150;
	// ��̬�������(12Сʱ)
	public static final long CHECK_STATIC_AD_INTERVAL = 1000 * 60 * 60 * 12;
	// �Ƿ�������ͼ��
	private boolean loadAppIcon = true;
	// �Ƿ���������ͼ
	private boolean loadAppScreenshot = true;
	// �Ƿ�ʹ�û��˿�ݹ���
	private boolean backMoreFunc = false;
	// �Ƿ��Զ�����������
	private boolean checkSoftwareUpdate = false;
	// ����ͼ��ʹ�ÿռ䣬����Ԥ��Ϊ2M
	private int iconCacheSize = 1024 * 1024 * 2;
	// ���Ľ�ͼ���棬����Ԥ��Ϊ ��2M
	private int shotCacheSize = 1024 * 1024 * 2;
	// ���ͼƬԤ��Ϊ1M
	private int advertiseCacheSize = 1024 * 1024;
	// ͼƬ���Ļ���ʱ�� ����, 1000 * 60 * 60 * 24 * 2
	private long imageCacheTime = 1000 * 60 * 60 * 24 * 1;
	// ������� ����1000 * 60 * 60 * 12
	private long updateInfoCacheTime = 1000 * 60 * 60 * 12;
	// �����г�����������ݳ�ʼ��ʱ�䣬������ڿ����ڴ�app����� 15����
	private long maxContextReinit = 1000 * 60 * 15;
	// ����apkɨ��·�����г�ֻɨ��һ��Ŀ¼��
	private String localApkDir;

	/**
	 * @return the loadAppIcon
	 */
	public boolean isLoadAppIcon() {
		return loadAppIcon;
	}

	/**
	 * @param loadAppIcon
	 *            the loadAppIcon to set
	 */
	public void setLoadAppIcon(boolean loadAppIcon) {
		this.loadAppIcon = loadAppIcon;
	}

	/**
	 * @return the loadAppScreenshot
	 */
	public boolean isLoadAppScreenshot() {
		return loadAppScreenshot;
	}

	/**
	 * @param loadAppScreenshot
	 *            the loadAppScreenshot to set
	 */
	public void setLoadAppScreenshot(boolean loadAppScreenshot) {
		this.loadAppScreenshot = loadAppScreenshot;
	}

	/**
	 * @return the backMoreFunc
	 */
	public boolean isBackMoreFunc() {
		return backMoreFunc;
	}

	/**
	 * @param backMoreFunc
	 *            the backMoreFunc to set
	 */
	public void setBackMoreFunc(boolean backMoreFunc) {
		this.backMoreFunc = backMoreFunc;
	}

	public boolean isCheckSoftwareUpdate() {
		return checkSoftwareUpdate;
	}

	public void setCheckSoftwareUpdate(boolean checkSoftwareUpdate) {
		this.checkSoftwareUpdate = checkSoftwareUpdate;
	}

	/**
	 * @return the iconCacheSize
	 */
	public int getIconCacheSize() {
		return iconCacheSize;
	}

	/**
	 * @param iconCacheSize
	 *            the iconCacheSize to set
	 */
	public void setIconCacheSize(int iconCacheSize) {
		this.iconCacheSize = iconCacheSize;
	}

	/**
	 * @return the shotCacheSize
	 */
	public int getShotCacheSize() {
		return shotCacheSize;
	}

	/**
	 * @param shotCacheSize
	 *            the shotCacheSize to set
	 */
	public void setShotCacheSize(int shotCacheSize) {
		this.shotCacheSize = shotCacheSize;
	}

	/**
	 * @return the advertiseCacheSize
	 */
	public int getAdvertiseCacheSize() {
		return advertiseCacheSize;
	}

	/**
	 * @param advertiseCacheSize
	 *            the advertiseCacheSize to set
	 */
	public void setAdvertiseCacheSize(int advertiseCacheSize) {
		this.advertiseCacheSize = advertiseCacheSize;
	}

	/**
	 * @return the imageCacheTime
	 */
	public long getImageCacheTime() {
		return imageCacheTime;
	}

	/**
	 * @param imageCacheTime
	 *            the imageCacheTime to set
	 */
	public void setImageCacheTime(long imageCacheTime) {
		this.imageCacheTime = imageCacheTime;
	}

	/**
	 * @return the maxContextReinit
	 */
	public long getMaxContextReinit() {
		return maxContextReinit;
	}

	/**
	 * @param maxContextReinit
	 *            the maxContextReinit to set
	 */
	public void setMaxContextReinit(long maxContextReinit) {
		this.maxContextReinit = maxContextReinit;
	}

	/**
	 * @return the updateInfoCacheTime
	 */
	public long getUpdateInfoCacheTime() {
		return updateInfoCacheTime;
	}

	/**
	 * @param updateInfoCacheTime
	 *            the updateInfoCacheTime to set
	 */
	public void setUpdateInfoCacheTime(long updateInfoCacheTime) {
		this.updateInfoCacheTime = updateInfoCacheTime;
	}

	/**
	 * @return the localApkDir
	 */
	public String getLocalApkDir() {
		return localApkDir;
	}

	/**
	 * @param localApkDir
	 *            the localApkDir to set
	 */
	public void setLocalApkDir(String localApkDir) {
		this.localApkDir = localApkDir;
	}

}
