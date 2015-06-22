package com.kapps.market.service;

/**
 * 2010-6-23<br>
 *
 * 
 * @author admin
 * 
 */
@SuppressWarnings("serial")
public class ActionException extends Exception {

	public static final int RESULT_OK = 0;
	public static final int RESULT_ERROR = 10;
	// pro
	public static final int RESULT_PRO_WRONG = 20;
	// arg
	public static final int RESULT_ARG_WRONG = 30;
	// bus
	public static final int RESULT_SESSION_TIMEOUT = 40;
	public static final int RESULT_LOGIN_ERROR = 41;
	public static final int RESULT_REGISTE_ERROR = 42;
	public static final int RESULT_SOFTWARE_UPDATE_INVALID = 43;
	public static final int RESULT_USERE_NOT_EXIST = 44;
	// �쳣���
	private int exCode;

	// �쳣��Ϣ
	private String exMessage;

	/**
	 * @param exCode
	 */
	public ActionException() {
		super();
	}

	/**
	 * @param exCode
	 */
	public ActionException(int exCode) {
		super();
		this.exCode = exCode;
	}

	/**
	 * @param exCode
	 */
	public ActionException(int exCode, String message) {
		super();
		this.exCode = exCode;
		this.exMessage = message;
	}

	/**
	 * @return the exMessage
	 */
	public String getExMessage() {
		return exMessage;
	}

	/**
	 * @param exMessage
	 *            the exMessage to set
	 */
	public void setExMessage(String exMessage) {
		this.exMessage = exMessage;
	}

	/**
	 * @return the exCode
	 */
	public int getExCode() {
		return exCode;
	}

	/**
	 * @param exCode
	 *            the exCode to set
	 */
	public void setExCode(int exCode) {
		this.exCode = exCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServiceException [exCode=" + exCode + ", exMessage=" + exMessage + "]";
	}

}
