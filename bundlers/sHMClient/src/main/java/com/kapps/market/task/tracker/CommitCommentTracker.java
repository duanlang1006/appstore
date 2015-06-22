package com.kapps.market.task.tracker;

import com.kapps.market.bean.AppComment;
import com.kapps.market.bean.AppItem;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.CommitCommentTaskMark;

/**
 * 2010-6-27
 * 
 * @author admin
 * 
 */
public class CommitCommentTracker extends AInvokeTracker {

	public static final String TAG = "CommitCommentTracker";

	/**
	 * @param resultReceiver
	 */
	public CommitCommentTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
		// TODO Auto-generated constructor stub
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ck.market.task.tracker.AInvokeTracker#handleResult(com.ck.market
	 * .task.OperateResult)
	 */
	@Override
	public void handleResult(OperateResult result) {
		CommitCommentTaskMark taskMark = (CommitCommentTaskMark) result.getTaskMark();
		AppItem appItem = appCahceManager.getAppItemById(taskMark.getAppId());
		appItem.setMyComment((AppComment) result.getAttach());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ck.market.task.tracker.AInvokeTracker#handleFault(com.ck.market
	 * .task.OperateResult)
	 */
	@Override
	public void handleFault(OperateResult result) {

	}

}
