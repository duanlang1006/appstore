package com.kapps.market.task.tracker;

import com.kapps.market.bean.AppItem;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.ATaskMark;

import java.util.List;

/**
 * 2010-6-26<br>
 * ������¸�������
 * 
 * @author admin
 * 
 */
public class SoftwareUpdateTracker extends AInvokeTracker {

	private static final String TAG = "SoftwareUpdateTracker";

	public SoftwareUpdateTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleResult(OperateResult result) {
		ATaskMark taskWraper = result.getTaskMark();
		List<AppItem> itemList = (List<AppItem>) result.getResultData();
		if (itemList != null && itemList.size() > 0) {
			// ��ӵ�����
			appCahceManager.setAppItemToCache(taskWraper, itemList);
			trackerResult = itemList;
		}
		LogUtil.d(TAG, "handleResult itemList size: " + itemList + " taskWraper: " + taskWraper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hiapk.market.task.tracker.AInvokeTracker#TAG()
	 */
	@Override
	public String TAG() {
		// TODO Auto-generated method stub
		return TAG;
	}

}
