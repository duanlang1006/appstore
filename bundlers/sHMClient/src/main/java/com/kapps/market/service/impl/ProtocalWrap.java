package com.kapps.market.service.impl;

/**
 * 2010-7-16<br>
 * a protocalwrap == an API URL's parameter.
 * ProtocalWrap pw = new ProtocalWrap();
 * pw.setGetData("op=" + USER_LOGIN + "&user=" + Util.encodeContentForUrl(user) + "&pwd=" + pwd + "&chid=" + imei + "&imsi=" + imsi + "&sign=" + sign);
 * 
 * @author admin
 * 
 */
public class ProtocalWrap {
	// ����õĲ�����ݣ� qt=xx&qid=xx
	private String getData;
	// post��ݲ���
	private String postData;
	// ����������ͬ���ֵû�����ý�ʹ��Ĭ�ϵ�����
	private String host;
	// ��ʱ(�����ʱ��������)
	private int soTimeout = -1;
	// �Ƿ������������Ի��ƣ�Ĭ�������Եġ�
	private boolean reTry = true;

	/**
	 * @return the getData
	 */
	public String getGetData() {
		return getData;
	}

	/**
	 * @param getData
	 *            the getData to set
	 */
	public void setGetData(String getData) {
		this.getData = getData;
	}

	/**
	 * @return the postData
	 */
	public String getPostData() {
		return postData;
	}

	/**
	 * @param postData
	 *            the postData to set
	 */
	public void setPostData(String postData) {
		this.postData = postData;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the soTimeout
	 */
	public int getSoTimeout() {
		return soTimeout;
	}

	/**
	 * @param soTimeout
	 *            the soTimeout to set
	 */
	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	/**
	 * @return the reTry
	 */
	public boolean isReTry() {
		return reTry;
	}

	/**
	 * @param reTry
	 *            the reTry to set
	 */
	public void setReTry(boolean reTry) {
		this.reTry = reTry;
	}

}
