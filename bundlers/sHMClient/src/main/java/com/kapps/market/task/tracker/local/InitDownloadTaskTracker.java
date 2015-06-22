package com.kapps.market.task.tracker.local;

import android.content.Intent;
import android.os.Message;

import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.DownloadItem;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.tracker.AInvokeTracker;
import com.kapps.market.util.Constants;
import com.kapps.market.util.SecurityUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2010-6-26<br>
 * �ָ����������б�
 * 
 * @author admin
 * 
 */
public class InitDownloadTaskTracker extends AInvokeTracker {

	private static final String TAG = "InitDTaskTracker";

	public InitDownloadTaskTracker(IResultReceiver iReceiver) {
		super(iReceiver);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleResult(OperateResult result) {
		// ���óɹ�
		Map<Integer, List<DownloadItem>> taskMap = (HashMap<Integer, List<DownloadItem>>) result.getResultData();
		// ��ӵ�����
		marketManager.setTaskMap(taskMap);

		// �����г�����İ�װ����ô�Ƿ��Ѿ���װͬһ�汾�ˣ��������ɾ��
		if (taskMap.size() > 0) {
			DownloadItem item = marketManager.getMarketDownloadedItem();
			if (item != null && marketContext.getContextConfig().getVersionCode() == item.getVersionCode()) {
				// ɾ���г�apk
				Message message = Message.obtain();
				message.what = Constants.M_DOWNLOAD_CANCEL;
				message.obj = item;
				marketContext.handleMarketMessage(message);
			}

			// �ָ�����
			for (DownloadItem downloadItem : taskMap.get(BaseApp.APP_DOWNLOADING)) {
				Intent intent = new Intent(Constants.ACTION_SERVICE_DOWNLOAD_REQUEST);
				intent.putExtra(Constants.DOWNLOAD_ITEM_ID, downloadItem.getId());
				intent.putExtra(Constants.SEC_SIGN,
						SecurityUtil.md5Encode(marketContext.getTs() + Constants.SEC_KEY_STRING));
				intent.putExtra(Constants.H_SER_TS, marketContext.getTs());
				marketContext.startService(intent);
			}

		}

		LogUtil.d(TAG, "handleResult taskMap: " + taskMap);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.task.tracker.AInvokeTracker#TAG()
	 */
	@Override
	public String TAG() {
		// TODO Auto-generated method stub
		return TAG;
	}

}
