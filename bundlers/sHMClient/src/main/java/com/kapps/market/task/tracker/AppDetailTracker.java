package com.kapps.market.task.tracker;


import com.kapps.market.bean.AppDetail;
import com.kapps.market.bean.AppItem;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppDetailTaskMark;

/**
 * 2010-6-26 <br>
 * �������ϸ��Ϣ
 * 
 * @author admin
 * 
 */
public class AppDetailTracker extends AInvokeTracker {

	public static final String TAG = "AppDetailTaskMark";

	/**
	 * @param resultReceiver
	 */
	public AppDetailTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.task.tracker.AInvokeTracker#handleInvokeOver(com.hiapk
	 * .market.task.OperateResult)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handleResult(OperateResult result) {
		AppDetail appDetail = (AppDetail) result.getResultData();
		AppDetailTaskMark taskMark = (AppDetailTaskMark) result.getTaskMark();
		if (appDetail != null) {
			AppItem appItem = appCahceManager.getAppItemById(taskMark.getId());
			appItem.setAppDetail(appDetail);

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
