package com.kapps.market.bean;

import java.io.Serializable;

/**
 * 2010-7-12<br>
 * Ӧ�õĻ���Ϣ
 * 
 * @author admin
 * 
 */
public class BaseApp implements Serializable, Iconable {
	//
	private static final long serialVersionUID = 20100712L;
	// һЩ��־��Ϣ
	// �µ�
	public static final int APP_NEW = 0;
	// �Ѱ�װ
	public static final int APP_INSTALLED = 1;
	// ��װ��
	public static final int APP_INSTALLING = 2;
	// ��ж��
	public static final int APP_UNINSTALLED = 3;
	// ������
	public static final int APP_DOWNLOADED = 4;
	// ��������
	public static final int APP_DOWNLOADING = 5;
	// ����ֹͣ�������ֶ�ֹͣ��ʧ���ճɵ�ֹͣ��
	public static final int APP_DOWNLOAD_STOP = 6;
	// ������
	public static final int APP_BACKUPING = 7;
	// �����һ��״̬
	private int state = APP_NEW;

	// Ӧ������
	private String name;
	// Ӧ�ð汾
	private String version;
	// Ӧ�ð汾��
	private int versionCode;
	// ����
	private String packageName;
	// ��С KB
	private int size;
	// ��ӵ�ϲ��/�����ʱ��/����ʱ��
	private String time;
	// �Ƿ���ϲ��
	private boolean favor;
	// ���id����Ӧ����ڷ���˵���ݿ��е�id��
	// �ͻ��˲����������id�����ڻ���Ӧ����
	// ʹ������ΪΨһ�ı�ʾ��
	private int id;
	// ͼ��
	private String iconUrl;
	// �Ƿ񸶷�
	private boolean free;
	// �۸�
	private double price;
	// �Ƿ��Ѿ�����
	private boolean purchase;
	// ���ص�ַ : Զ��Ӧ��/�ѹ���/�ҵ��ղص�ʱ�������ص�ַ(��Ե�ַ)������Ӧ��ʱ���Ǳ���·����
	private String apkPath;
	// ����·�����粻ͬ�汾�Ĳ��������host��Ҫָ��
	// �������ֶ�Ϊnull������ʹ��Ĭ����������
	// ���һ��������Դ���ء�
	private String hostPath;

	// �Ƿ��Ѿ���ѡ���ˣ��û��������?ͬʱ������ͼ
	private boolean choose;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * ��SoftwareManager ͬ��
	 * 
	 * @param state
	 *            the state to set
	 */
	public void setState(int state) {
		this.state = state;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @param packageName
	 *            the packageName to set
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the favorTime
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the favorTime to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the favor
	 */
	public boolean isFavor() {
		return favor;
	}

	/**
	 * @param favor
	 *            the favor to set
	 */
	public void setFavor(boolean favor) {
		this.favor = favor;
	}

	@Override
	public int getIconId() {
		return id;
	}

	/**
	 * @return the iconUrl
	 */
	@Override
	public String getIconUrl() {
		return iconUrl;
	}

	/**
	 * @param iconUrl
	 *            the iconUrl to set
	 */
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	@Override
	public int getIconType() {
		return MImageType.TYPE_NONE;
	}

	/**
	 * @return the free
	 */
	public boolean isFree() {
		return free;
	}

	/**
	 * @param free
	 *            the free to set
	 */
	public void setFree(boolean free) {
		this.free = free;
	}

	/**
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * @param price
	 *            the price to set
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * @return the purchase
	 */
	public boolean isPurchase() {
		return purchase;
	}

	/**
	 * @param purchase
	 *            the purchase to set
	 */
	public void setPurchase(boolean purchase) {
		this.purchase = purchase;
	}

	/**
	 * @return the apkPath
	 */
	public String getApkPath() {
		return apkPath;
	}

	/**
	 * @param apkPath
	 *            the apkPath to set
	 */
	public void setApkPath(String apkPath) {
		this.apkPath = apkPath;
	}

	public String getHostPath() {
		return hostPath;
	}

	public void setHostPath(String hostPath) {
		this.hostPath = hostPath;
	}

	/**
	 * @return the choose
	 */
	public boolean isChoose() {
		return choose;
	}

	/**
	 * @param choose
	 *            the choose to set
	 */
	public void setChoose(boolean choose) {
		this.choose = choose;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BaseApp [id=" + id + ", name=" + name + ", packageName=" + packageName + ", size=" + size
				+ ", version=" + version + ", versionCode=" + versionCode + "]";
	}

}
