package com.kapps.market.task.tracker;

import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;

/**
 * 2010-9-8<br>
 * ʲô��������tracker
 * 
 * @author admin
 * 
 */
public class EmptyTracker extends AInvokeTracker {

	/**
	 * @param resultReceiver
	 */
	public EmptyTracker(IResultReceiver resultReceiver) {
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
		return null;
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
		// TODO Auto-generated method stub

	}

}
