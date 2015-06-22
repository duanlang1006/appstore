package com.kapps.market.task.tracker;

import com.kapps.market.bean.MarketUpdateInfo;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;

/**
 * 2010-6-26<br>
 * �г����¸�������
 * 
 * @author admin
 * 
 */
public class MarketUpdateTracker extends AInvokeTracker {

	private static final String TAG = "MarketUpdateaTracker";

	public MarketUpdateTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
	}

	@Override
	public void handleResult(OperateResult result) {
		MarketUpdateInfo updateInfo = (MarketUpdateInfo) result.getResultData();
		this.trackerResult = updateInfo;
	}

	@Override
	public String TAG() {
		// TODO Auto-generated method stub
		return TAG;
	}

}
