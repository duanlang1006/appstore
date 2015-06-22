package com.kapps.market;

import java.util.List;
import java.util.Vector;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;

import com.kapps.market.bean.DownloadItem;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.AResourceDownloader;
import com.kapps.market.task.NetApkDownloader;
import com.kapps.market.task.RunAsyncTask;
import com.kapps.market.task.WapApkDownloader;
import com.kapps.market.task.AResourceDownloader.IDownloadProgress;
import com.kapps.market.util.Constants;

/**
 * 2011-3-14<br>
 * ���ط�����
 * 
 * @author admin
 * 
 */
public class DownloadStatePart implements IStatePart {

	public static final String TAG = "DownloadStatePart";
	private static final boolean debug = false;

	// ʣ������������
	private int downloadTaskCount = 0;

	// ��ǰ���е�����: ���ͬʱ����3������
	private List<DownloadTask> runningTaskList = new Vector<DownloadTask>();
	private List<DownloadTask> waitTaskList = new Vector<DownloadTask>();
	private MApplication mApplication;

	/**
	 * @param mApplication
	 */
	public DownloadStatePart(MApplication mApplication) {
		this.mApplication = mApplication;
	}

	@Override
	public void handleServiceRequest(Intent intent, int startId) {
		if (debug) {
			Log.v(TAG, "action: " + intent.getAction() + " Starting intent:" + intent.getExtras());
		}

		// ֻ�����ط�������
		if (Constants.ACTION_SERVICE_DOWNLOAD_REQUEST.equals(intent.getAction())) {
			// ������������Ѿ��������������
			int did = intent.getIntExtra(Constants.DOWNLOAD_ITEM_ID, Constants.NONE_ID);
			// ȷ������ʼ��ָ��ͬһ������
			DownloadItem downloadItem = mApplication.getMarketManager().getDownloadItemById(did);
			String sign = intent.getStringExtra(Constants.SEC_SIGN);
			String ts = intent.getStringExtra(Constants.H_SER_TS);
			boolean taskExist = isTaskExist(downloadItem.getAppId());

			if (debug) {
				Log.v(TAG, "taskExist: " + taskExist + " pname:" + downloadItem.getPackageName() + " sign: " + sign
						+ " ts: " + ts);
			}
			if (!taskExist) {
				try {
					// ����������
					addDownloadTaskCount(downloadItem);
					DownloadTask downloadTask = new DownloadTask(downloadItem, sign, ts);
					// ��ִ��
					if (runningTaskList.size() < 3) {
						runningTaskList.add(downloadTask);
						downloadTask.execute();

						// �ȴ�
					} else {
						waitTaskList.add(downloadTask);
					}

				} catch (Exception e) {
					Message message = Message.obtain();
					message.what = Constants.M_DOWNLOAD_FAIL;
					message.obj = downloadItem;
					mApplication.handleMarketMessage(message);

					e.printStackTrace();
				}
			}

			// ȡ������
		} else if (Constants.ACTION_SERVICE_DOWNLOAD_CANCEL.equals(intent.getAction())) {
			boolean handle = false;
			int appId = intent.getIntExtra(Constants.APP_ID, -1);
			// �Ƿ��Ѿ���������
			for (DownloadTask task : runningTaskList) {
				if (task.getDownloadItem().getAppId() == appId) {
					task.cancelDownloadTask();
					handle = true;
					break;
				}
			}
			// ����ڵȴ���ֱ��ɾ��
			if (!handle) {
				for (DownloadTask task : waitTaskList) {
					if (task.getDownloadItem().getAppId() == appId) {
						waitTaskList.remove(task);
						reduceDownloadTaskCount(task.getDownloadItem());
						break;
					}
				}
			}

			if (debug) {
				Log.d(TAG, "cancel handle: " + handle + " appId: " + appId);
			}

			// ȡ��ֹͣ
		} else if (Constants.ACTION_SERVICE_DOWNLOAD_STOP.equals(intent.getAction())) {
			boolean handle = false;
			int appId = intent.getIntExtra(Constants.APP_ID, -1);
			// �Ƿ��Ѿ���������
			for (DownloadTask task : runningTaskList) {
				if (task.getDownloadItem().getAppId() == appId) {
					task.stopDownloadTask();
					handle = true;
					break;
				}
			}
			// ����ڵȴ���ֱ��ɾ��
			if (!handle) {
				for (DownloadTask task : waitTaskList) {
					if (task.getDownloadItem().getAppId() == appId) {
						waitTaskList.remove(task);
						reduceDownloadTaskCount(task.getDownloadItem());
						break;
					}
				}
			}

			if (debug) {
				Log.d(TAG, "stop handle: " + handle + " appId: " + appId);
			}
		}

	}

	@Override
	public void handleServiceExit() {
		hideNotification(mApplication, R.string.market_download_task);
	}

	/**
	 * ����֪ͨ
	 */
	public void hideNotification(Context context, int id) {
		NotificationManager notiflyManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notiflyManager.cancel(id);
	}

	// �����Ƿ����
	private boolean isTaskExist(int appId) {
		for (DownloadTask task : runningTaskList) {
			if (task.getDownloadItem().getAppId() == appId) {
				return true;
			}
		}
		for (DownloadTask task : waitTaskList) {
			if (task.getDownloadItem().getAppId() == appId) {
				return true;
			}
		}
		return false;
	}

	private synchronized void addDownloadTaskCount(DownloadItem downloadItem) {
		downloadTaskCount++;
		
		// ����������app
		MarketNotify.nofityAppDownloadList(mApplication,
				downloadTaskCount + mApplication.getString(R.string.download_task_rest_colon));
		
		// �������֪ͨ
		MarketNotify.notifyAppDownloadProgress(mApplication, downloadItem.getName(), downloadItem);
		
	}

	private synchronized void reduceDownloadTaskCount(DownloadItem downloadItem) {
		if (downloadTaskCount > 0) {
			downloadTaskCount--;
			if (downloadTaskCount > 0) {
				MarketNotify.nofityAppDownloadList(mApplication,
						downloadTaskCount + mApplication.getString(R.string.download_task_rest_colon));
			} else {
				MarketNotify.nofityAppDownloadList(mApplication, mApplication.getString(R.string.download_task_finish),
						Notification.FLAG_AUTO_CANCEL, R.drawable.app_downloaded_task);
			}
			
			// ��ȡ������֪ͨ
			hideNotification(mApplication, downloadItem.getAppId());
			
		}
	}

	// Ӧ�õ���������
	@SuppressWarnings("unchecked")
	private class DownloadTask extends RunAsyncTask implements IDownloadProgress {

		private DownloadItem downloadItem;
		private String sign;
		private String ts;
		private AResourceDownloader downloader;

		/**
		 * @param packageName
		 */
		public DownloadTask(DownloadItem downloadItem, String sign, String ts) throws Exception {
			this.downloadItem = downloadItem;
			this.sign = sign;
			this.ts = ts;
		}

		@Override
		protected Object doInBackground(Object... params) {
			// ʵ�ʵ�����
			int resultMark = Constants.FAILURE;
			try {
				if (NetworkinfoParser.isWapConnector(mApplication)) {
					downloader = new WapApkDownloader(mApplication, this);
				} else {
					downloader = new NetApkDownloader(mApplication, this);
				}
				downloader.downloadResource(downloadItem, sign, ts);
				int filesize=downloadItem.getSize();
				double realsize=downloadItem.getdSize();
				if(filesize==(int)realsize)
					resultMark = Constants.SUCCESS;

			} catch (Exception ex) {
				ex.printStackTrace();
				Log.e(TAG, "download app err: " + ex.getMessage());
			}

			return resultMark;
		}

		@Override
		public void receiveProgress(DownloadItem downloadItem, int downloadedBytes) {
            //LogUtil.d("download", "receiveProgress:"+(downloadedBytes / 1024.0));
			downloadItem.setdSize(downloadedBytes / 1024.0);

			Message message = Message.obtain();
			message.what = Constants.M_DOWNLOAD_PROGRESS;
			message.obj = downloadItem;

			mApplication.handleMarketMessage(message);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Object result) {
			int resultMark = Integer.parseInt(result.toString());
			// ����ȡ��󽫲����? ����ȡ���ֹͣ����ʱ�Ѿ��������һ������
			if (downloader.isCancelDownload() || downloader.isStopDownload()) {
				return;
			}

			if (resultMark == Constants.SUCCESS) {
				// �㲥�������
				Message message = Message.obtain();
				message.what = Constants.M_DOWNLOAD_COMPLETED;
				message.obj = downloadItem;
				mApplication.handleMarketMessage(message);

			} else if (resultMark == Constants.FAILURE) {
				// �㲥����ʧ��
				Message message = Message.obtain();
				message.what = Constants.M_DOWNLOAD_FAIL;
				message.obj = downloadItem;
				mApplication.handleMarketMessage(message);
			}

			// ������һ������
			scheduleNextTask();
		}

		private void scheduleNextTask() {
			// ������һ
			reduceDownloadTaskCount(downloadItem);

			// ִ����һ������
			runningTaskList.remove(this);
			if (waitTaskList.size() > 0) {
				DownloadTask task = waitTaskList.remove(0);
				runningTaskList.add(task);
				task.execute();
			}
		}

		/**
		 * @return the downloadItem
		 */
		public DownloadItem getDownloadItem() {
			return downloadItem;
		}

		/**
		 * @param cancelDownload
		 *            the cancelDownload to set
		 */
		public void cancelDownloadTask() {
			downloader.cancelDownload();
			// ������һ������
			scheduleNextTask();
		}

		/**
		 * @param cancelDownload
		 *            the cancelDownload to set
		 */
		public void stopDownloadTask() {
			downloader.stopDownload();
			// ������һ������
			scheduleNextTask();
		}

	}

}
