package com.kapps.market.bean;

/**
 * 2010-7-29<br>
 * 
 * @author admin
 * 
 */
public class LoginResult {
	private String sessinId;
	// ����imsi�����ƽ̨
	private String smscat;

	/**
	 * @return the sessinId
	 */
	public String getSessinId() {
		return sessinId;
	}

	/**
	 * @param sessinId
	 *            the sessinId to set
	 */
	public void setSessinId(String sessinId) {
		this.sessinId = sessinId;
	}

	/**
	 * @return the smscat
	 */
	public String getSmscat() {
		return smscat;
	}

	/**
	 * @param smscat
	 *            the smscat to set
	 */
	public void setSmscat(String smscat) {
		this.smscat = smscat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LoginResult [sessinId=" + sessinId + ", smscat=" + smscat + "]";
	}

}
