/** 
 *@项目名称: SHMClient 
 *@文件名称: BootBroadcastReceiver.java 
 *@Author: linlin.zou
 *@Date: 2015-1-24 
 *@Copyright: 2015 www.alipear.com Inc. All rights reserved. 

 *注意：本内容仅限于上海鸭梨网络有限公司内部传阅，禁止外泄以及用于其他的商业目的 
 */ 
package com.kapps.market;

import org.androidpn.client.ServiceManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		ServiceManager serviceManager = new ServiceManager(context);
		serviceManager.setNotificationIcon(R.drawable.a_notification_icon);
		serviceManager.startService();
	}
}
