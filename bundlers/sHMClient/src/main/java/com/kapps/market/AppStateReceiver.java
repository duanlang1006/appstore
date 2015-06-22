package com.kapps.market;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Message;

import com.kapps.market.log.LogUtil;
import com.kapps.market.util.Constants;

/**
 */
public class AppStateReceiver extends BroadcastReceiver {
	public static final String TAG = "AppStateReceiver";

	// ������handle
	private MApplication mApplication;

	public AppStateReceiver() {
		mApplication = MApplication.getInstance();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) {
			return;
		}

		String action = intent.getAction();
		LogUtil.d(TAG, "action: " + action + " intent: " + intent);
		if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
			// package:
			String pname = intent.getDataString().substring(8);
			Message message = Message.obtain();
			message.what = Constants.M_INSTALL_APK;
			message.obj = pname;
			mApplication.handleMarketMessage(message);

		} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
			Uri d = intent.getData();
			String path = d.getEncodedPath();
			String uri = intent.getDataString();
			// "package:" length=8
			String pname = uri.substring(8, uri.length());
			Message message = Message.obtain();
			message.what = Constants.M_UNINSTALL_APK;
			message.obj = pname;
			mApplication.handleMarketMessage(message);

		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			NetworkInfo networkInfo = intent.getParcelableExtra("networkInfo");
			if (networkInfo != null) {
				Message message = Message.obtain();
				message.what = Constants.M_PARSE_NETWORKINFO;
				message.obj = networkInfo;
				mApplication.handleMarketMessage(message);
			}

		} else if (Constants.ACTION_CHOOSE_APP_DOWNLOAD_LIST_NOTIFY.equals(action)) {
			Message message = Message.obtain();
			message.what = Constants.M_NOTIFY_APP_DOWNLOAD_LIST;
			message.obj = intent;
			mApplication.handleMarketMessage(message);

		} else if (Constants.ACTION_CHOOSE_DOWNLOAD_APP_COMPLETED.equals(action)) {
			Message message = Message.obtain();
			message.what = Constants.M_NOTIFY_APP_DOWNLOADED;
			message.obj = intent;
			mApplication.handleMarketMessage(message);

		} else if (Constants.ACTION_CHOOSE_SOFT_UPDATE_NOTIFY.equals(action)) {
			Message message = Message.obtain();
			message.what = Constants.M_NOTIFY_SOFT_UPDATE;
			mApplication.handleMarketMessage(message);

		} else if (Constants.ACTION_CHECK_STATIC_AD_NOTIFY.equals(action)) {
			Message message = Message.obtain();
			message.what = Constants.M_CHECK_SATIC_AD;
			mApplication.handleMarketMessage(message);

		} else if (Constants.ACTION_CHOOSE_STATIC_AD_NOTIFY.equals(action)) {
			Message message = Message.obtain();
			message.what = Constants.M_NOTIFY_STATIC_AD;
			message.obj = intent;
			mApplication.handleMarketMessage(message);

		} else if (Constants.ACTION_CHOOSE_MARKET_UPDATE_NOTIFY.equals(action)) {
			Message message = Message.obtain();
			message.what = Constants.M_NOTIFY_MARKET_UPDATE;
			mApplication.handleMarketMessage(message);

		} else if (Constants.ACTION_CHECK_UPDATE_NOTIFY.equals(action)) {
			Message message = Message.obtain();
			message.what = Constants.M_CHECK_MS_UPDATE;
			mApplication.handleMarketMessage(message);

		}
	}
}
