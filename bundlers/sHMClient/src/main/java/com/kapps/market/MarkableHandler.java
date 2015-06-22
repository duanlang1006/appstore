package com.kapps.market;

import android.os.Handler;

/**
 * 2010-7-23<br>
 * 
 * @author Administrator
 * 
 */
abstract class MarkableHandler extends Handler {

	// ��������ʶ
	private int messageMark;
	// �Ƿ���ȫ��
	private boolean handleAll;

	/**
	 * @param handlerMark
	 */
	public MarkableHandler() {
		super();
	}

	/**
	 * @return the handlerMark
	 */
	public int getMssageMark() {
		return messageMark;
	}

	/**
	 * @return the handleAll
	 */
	public boolean isHandleAll() {
		return handleAll;
	}

	/**
	 * @param messageMark
	 *            the messageMark to set
	 */
	public void setMessageMark(int messageMark) {
		this.messageMark = messageMark;
	}

	/**
	 * @param handleAll
	 *            the handleAll to set
	 */
	public void setHandleAll(boolean handleAll) {
		this.handleAll = handleAll;
	}

}
