package com.kapps.market.task.tracker;

import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.ATaskMark;

public class commitMarketFirstTracker extends AInvokeTracker {

	public static final String TAG = "commitMarketFirstTracker";

	/**
	 * @param iReference
	 */
	public commitMarketFirstTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
	}

	@Override
	public void handleResult(OperateResult result) {
		Integer ret =  (Integer)result.getResultData();
		int data=ret.intValue();
		ATaskMark taskMark = result.getTaskMark();
		if (data == 0) {
			marketContext.setNewMarketSuccess();
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
