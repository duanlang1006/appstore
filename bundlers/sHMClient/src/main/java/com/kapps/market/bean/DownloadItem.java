package com.kapps.market.bean;

/**
 * 2010-7-18
 * 
 * @author admin
 * 
 */
public class DownloadItem extends BaseApp {

	//
	private String savePath;
	// ��¼��ǰ������(��ʱ���),��λKB
	private double dSize;
	// ��Ӧ�����id
	private int appId;

	/**
	 * @return the dSize
	 */
	public double getdSize() {
		return dSize;
	}

	/**
	 * @param dSize
	 *            the dSize to set
	 */
	public void setdSize(double dSize) {
		this.dSize = dSize;
	}

	@Override
	public int getIconId() {
		return appId;
	}

	/**
	 * @return the appId
	 */
	public int getAppId() {
		return appId;
	}

	/**
	 * @param appId
	 *            the appId to set
	 */
	public void setAppId(int appId) {
		this.appId = appId;
	}

	@Override
	public int getIconType() {
		return MImageType.APP_ICON;
	}

	public String getSavePath() {
		return savePath;
	}

	/**
	 * @param savePath
	 *            the savePath to set
	 */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	@Override
	public String toString() {
		return "DownloadItem [appId=" + appId + ", dSize=" + dSize
				+ ", savePath=" + savePath + ", toString()=" + super.toString()
				+ "]";
	}

}
