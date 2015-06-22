package com.kapps.market;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.DownloadItem;
import com.kapps.market.bean.Software;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

public class MarketNotify {

	public static final String TAG = "MarketNotify";

	private static NotificationManager notificationManager;
	private static Notification notificationProgress;

	private static NotificationManager getNotificationManager(Context context) {
		if (notificationManager == null) {
			notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return notificationManager;
	}

	/**
	 * 
	 * @param context
	 * @param note
	 * @param title
	 * @param info
	 */
	public static void notifyStaticADInfo(Context context, String note, String title, String info) {
		Notification notification = new Notification(R.drawable.news_icon, note, System.currentTimeMillis());
		Intent intent = new Intent(Constants.ACTION_CHOOSE_STATIC_AD_NOTIFY);
		intent.setPackage(context.getPackageName());
		PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		notification.setLatestEventInfo(context, title, info, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		getNotificationManager(context).notify(R.string.static_ad_title, notification);

	}

	/**
	 * ֪ͨ���������Ϣ
	 * 
	 * @param context
	 * @param title
	 * @param info
	 */
	public static void notifySoftwareUpdateInfo(Context context, String title, String info) {
		Notification notification = new Notification(R.drawable.soft_update, title, System.currentTimeMillis());
		Intent intent = new Intent(Constants.ACTION_CHOOSE_SOFT_UPDATE_NOTIFY);
		PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		notification.setLatestEventInfo(context, title, info, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		getNotificationManager(context).notify(R.string.software_update_title, notification);
	}

	/**
	 * ֪ͨ�г�������Ϣ
	 * 
	 * @param context
	 * @param title
	 * @param info
	 */
	public static void notifyMarketUpdateInfo(Context context, String title, String info) {
		Notification notification = new Notification(R.drawable.soft_update, title, System.currentTimeMillis());
		Intent intent = new Intent(Constants.ACTION_CHOOSE_MARKET_UPDATE_NOTIFY);
		PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		notification.setLatestEventInfo(context, title, info, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		getNotificationManager(context).notify(R.string.market_update_title, notification);
	}

	/**
	 * ������أ���AppDownloadService.java��reduceDownloadTaskCount()���������õ�.
	 * 
	 * @param context
	 * @param pm
	 * @param text
	 * @param flag
	 * @param icon
	 */
	public static void nofityAppDownloadList(Context context, String text, int flag, int icon) {
		// Notification notification = new Notification(icon, text,
		// System.currentTimeMillis());
		//
		// // �㲥֪ͨ��Ϣ
		// Intent intent = new
		// Intent(Constants.ACTION_CHOOSE_APP_DOWNLOAD_LIST_NOTIFY);
		// PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0,
		// intent, 0);
		// notification.setLatestEventInfo(context,
		// context.getString(R.string.market_download_task), text,
		// contentIntent);
		// notification.flags |= flag;
		// getNotificationManager(context).notify(R.string.market_download_task,
		// notification);
	}

	/**
	 * �������
	 * 
	 * @param context
	 * @param pm
	 * @param text
	 */
	public static void nofityAppDownloadList(Context context, String text) {
		nofityAppDownloadList(context, text, Notification.FLAG_ONGOING_EVENT, R.drawable.app_download_task);
	}

	/**
	 * ���ؽ��
	 * 
	 * @param context
	 * @param tickerText
	 * @param downloadItem
	 */
	public static void notifyAppDownloadProgress(MApplication context, String tickerText, DownloadItem downloadItem) {
		// �㲥֪ͨ��Ϣ
		Intent intent = new Intent(Constants.ACTION_CHOOSE_APP_DOWNLOAD_LIST_NOTIFY);
		PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		if (notificationProgress == null) {
			notificationProgress = new Notification();
            notificationProgress.icon = R.drawable.download_process_icon;//download_process_small_icon;
            notificationProgress.tickerText = tickerText;
            notificationProgress.when = System.currentTimeMillis();
		}

		notificationProgress.contentView = new RemoteViews(context.getPackageName(), R.layout.notify_downlaod_item);
		notificationProgress.contentIntent = contentIntent;

		int progress = (int) (downloadItem.getdSize() / downloadItem.getSize() * 100);
		notificationProgress.contentView.setProgressBar(R.id.app_download_pross, 100, progress, false);
		notificationProgress.contentView.setTextViewText(R.id.app_download_text,
				Util.getDownloadProgressStr(downloadItem.getdSize(), downloadItem.getSize()));

		String textStr = downloadItem.getName();
		notificationProgress.contentView.setTextViewText(R.id.app_download_title, textStr);

		notificationProgress.flags |= Notification.FLAG_ONGOING_EVENT;
		getNotificationManager(context).notify(downloadItem.getAppId(), notificationProgress);

	}

	/**
	 * apk���������Ϣ
	 */
	public static void notifyAppDownloadCompleted(Context imContext, BaseApp baseApp, String contentPrefix) {
		String noteStr = baseApp.getName() + " v" + baseApp.getVersion() + " "
				+ imContext.getString(R.string.download_task_complete);

		Notification notification = new Notification(R.drawable.app_downloaded_task, noteStr,
				System.currentTimeMillis());
		Intent intent = new Intent(Constants.ACTION_CHOOSE_DOWNLOAD_APP_COMPLETED);
		intent.putExtra(Constants.APP_PACKAGE_NAME, baseApp.getPackageName());
		intent.putExtra(Constants.APP_NAME, baseApp.getName());
		if (baseApp instanceof DownloadItem) {
			intent.putExtra(Constants.APP_PATH, ((DownloadItem) baseApp).getSavePath());

		} else if (baseApp instanceof Software) {
			intent.putExtra(Constants.APP_PATH, baseApp.getApkPath());
		}

		// requestCode ����һ����Чֵ��Ϊ��ȷ��ϵͳÿ��Ϊ�������һ���µ�PendingIntent��
		PendingIntent contentIntent = PendingIntent.getBroadcast(imContext,
				(int) SystemClock.currentThreadTimeMillis(), intent, 0);
		notification.setLatestEventInfo(imContext, baseApp.getName() + " v" + baseApp.getVersion(), contentPrefix + " "
				+ imContext.getString(R.string.click_to_install), contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		int nid = (baseApp.getPackageName() + baseApp.getVersionCode()).hashCode();

		notificationManager.cancel(nid);
		notificationManager.notify(nid, notification);
	}

	/**
	 * apk��װ
	 * 
	 * @param context
	 * @param title
	 * @param info
	 */
	public static void notifySoftwareInstallInfo(Context context, String pname, String title, String info) {
		Notification notification = new Notification(R.drawable.app_installed, title, System.currentTimeMillis());
		Intent intent = null;
		try {
			intent = context.getPackageManager().getLaunchIntentForPackage(pname);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (intent == null) {
			intent = new Intent();
		}
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, title, info, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		getNotificationManager(context).notify(pname.hashCode(), notification);
	}

	/**
	 * ����Context������Ϣ���ȶ����ID,��������Ӧ����Ϣ��ʾ
	 * 
	 * @param context
	 * @param id
	 */
	public static void clearNofity(Context context, int id) {
		getNotificationManager(context).cancel(id);
	}

}
