package com.kapps.market.bean.config;

import java.io.Serializable;

/**
 * 2010-6-10 �г�������Ϣ�ķ�װ<br>
 * see: market_config
 * 
 * @author admin
 * 
 */
public class ContextConfig implements Serializable {

	private static final long serialVersionUID = 391014354442902238L;

	// ����id(����������)
	private String vid;
	// ����(��ͬ�����в�ͬ�Ļ��ͣ�����Ҫ��ʱĬ��Ϊmgy, ��鿴market_config)(����������)
	private String model;
	// ����id(����������)
	private String cid;
	// �汾
	private String version;
	// �汾��
	private int versionCode;
	// �̼�
	private String firmware;
	// �̼���Ӧ��sdk�汾
	private String sdkVersion;
	// �ֱ���
	private String resolution;
	// ��Ļ�ܶ�
	private String density;
	// �豸��
	private String deviceName;
	// �豸id(�ֻ������IMEI)
	private String deviceId;
	// ��id
	private String simId;
	// �Ƿ�ֻҪ��Ȩ���
	// 0: �������Ȩ����Ȩ
	// 1: ��Ȩ
	// 2: ����Ȩ
	private String authorization;
	// ����
	private String language;
	// ����ִ���Զ���װ
	private boolean permiInstall;
	
	private long install_time;
/**
 * 
 * @param time
 */
	public void setInstallTime(long time)
	{
		install_time=time;
	}
	
	public long getInstallTime()
	{
		return install_time;
	}
	/**
	 * @return the model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * @return the vid
	 */
	public String getVid() {
		return vid;
	}

	/**
	 * @param vid
	 *            the vid to set
	 */
	public void setVid(String vid) {
		this.vid = vid;
	}

	/**
	 * @return the cid
	 */
	public String getCid() {
		return cid;
	}

	/**
	 * @param cid
	 *            the cid to set
	 */
	public void setCid(String cid) {
		this.cid = cid;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the versionCode
	 */
	public int getVersionCode() {
		return versionCode;
	}

	/**
	 * @param versionCode
	 *            the versionCode to set
	 */
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	/**
	 * @return the firmware
	 */
	public String getFirmware() {
		return firmware;
	}

	/**
	 * @param firmware
	 *            the firmware to set
	 */
	public void setFirmware(String firmware) {
		this.firmware = firmware;
	}

	/**
	 * @return the sdkVersion
	 */
	public String getSdkVersion() {
		return sdkVersion;
	}

	/**
	 * @param sdkVersion
	 *            the sdkVersion to set
	 */
	public void setSdkVersion(String sdkVersion) {
		this.sdkVersion = sdkVersion;
	}

	/**
	 * @return the resolution
	 */
	public String getResolution() {
		return resolution;
	}

	/**
	 * @param resolution
	 *            the resolution to set
	 */
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	/**
	 * @return the density
	 */
	public String getDensity() {
		return density;
	}

	/**
	 * @param density
	 *            the density to set
	 */
	public void setDensity(String density) {
		this.density = density;
	}

	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId
	 *            the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return the simId
	 */
	public String getSimId() {
		return simId;
	}

	/**
	 * @param simId
	 *            the simId to set
	 */
	public void setSimId(String simId) {
		this.simId = simId;
	}

	/**
	 * @return the authorization
	 */
	public String getAuthorization() {
		return authorization;
	}

	/**
	 * @param authorization
	 *            the authorization to set
	 */
	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language
	 *            the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @return the deviceName
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param deviceName
	 *            the deviceName to set
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * @return the permiInstall
	 */
	public boolean isPermiInstall() {
		return permiInstall;
	}

	/**
	 * @param permiInstall
	 *            the permiInstall to set
	 */
	public void setPermiInstall(boolean permiInstall) {
		this.permiInstall = permiInstall;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ContextConfig [authorization=" + authorization + ", cid=" + cid + ", density=" + density
				+ ", deviceId=" + deviceId + ", deviceName=" + deviceName + ", firmware=" + firmware + ", language="
				+ language + ", model=" + model + ", permiInstall=" + permiInstall + ", resolution=" + resolution
				+ ", sdkVersion=" + sdkVersion + ", simId=" + simId + ", version=" + version + ", versionCode="
				+ versionCode + ", vid=" + vid + "]";
	}

}
