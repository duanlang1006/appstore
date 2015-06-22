package com.kapps.market.task.tracker;


import com.kapps.market.bean.AppItem;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.ATaskMark;

/**
 * 2010-6-26 <br>
 * ����ĸ�Ҫ��Ϣ
 * 
 * @author admin
 * 
 */
public class AppSummaryTracker extends AInvokeTracker {

	public static final String TAG = "AppSummaryTracker";

	/**
	 * @param resultReceiver
	 */
	public AppSummaryTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
	}

	@Override
	public void handleResult(OperateResult result) {
		AppItem appItem = (AppItem) result.getResultData();
		ATaskMark taskMark = result.getTaskMark();
		if (appItem != null) {
			// ����ظ�����ͬһ������ĸ�Ҫ��Ϣ�Ļ��������滻��ʹ�þ�ֵ
			AppItem oldAppItem = appCahceManager.getAppItemById(appItem.getId());
			if (oldAppItem != null) {
				trackerResult = oldAppItem;

			} else {
				appCahceManager.addAppItemToCache(appItem);
				trackerResult = appItem;
			}

		} else {
			taskMark.setTaskStatus(ATaskMark.HANDLE_ERROR);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.task.tracker.AInvokeTracker#TAG()
	 */
	@Override
	public String TAG() {
		return TAG;
	}

}
