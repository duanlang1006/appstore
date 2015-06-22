package com.kapps.market;

import android.content.Intent;

/**
 * 2011-3-14<br>
 * 
 * @author admin
 * 
 */
public interface IStatePart {

	/**
	 * �����������
	 * 
	 * @param intent
	 *            ���
	 * @param startId
	 *            ������ʾ
	 */
	public void handleServiceRequest(Intent intent, int startId);

	/**
	 * �������˳�
	 */
	public void handleServiceExit();
}
