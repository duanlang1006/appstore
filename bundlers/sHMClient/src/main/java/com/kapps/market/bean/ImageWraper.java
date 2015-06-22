package com.kapps.market.bean;

/**
 * 2010-6-10
 * Ĭ�ϲ�ѯͼ��ʱ���²��Ҷ�Ӧid�Ƿ��У�û�еĻ���ͨ��packageName���в��ҡ�
 * 
 * @author admin �����Ϊ�� BitMap �� byte[]
 */
public class ImageWraper {
	// ��ӦӦ�õ�id
	private String appId;
	// ������
	private String packageName;
	// ע�⻺���ʱ�������ԭʼ�ֽ���ʽ�����Ա�����ڴ濪��
	// ϵͳ���붨�ڼ����Щ��ʱ��Ϊ��ʹ�õ�Ӧ�ã��Ա�ȡ��
	// ��ЩӦ�õ�
	private byte[] imageBytes;
	// �����װ��ʲôͼƬ
	/**
	 * ͼ��
	 */
	public static final int ICON_TYPE = 0;
	/**
	 * ˵����ͼ
	 */
	public static final int SCREENSHOT_TYPE = 1;

	/**
	 * @return the appId
	 */
	public String getAppId() {
		return appId;
	}

	/**
	 * @param appId
	 *            the appId to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
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
	 * @return the imageBytes
	 */
	public byte[] getImageBytes() {
		return imageBytes;
	}

	/**
	 * @param imageBytes
	 *            the imageBytes to set
	 */
	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

}
