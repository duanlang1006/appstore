package com.kapps.market.task.tracker;


import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;

/**
 * 2010-8-4 <br>
 * ��鱾�ػ���
 * 
 * @author admin
 * 
 */
public class CheckLocalCacheTracker extends AInvokeTracker {

	/**
	 * @param resultReceiver
	 */
	public CheckLocalCacheTracker(IResultReceiver resultReceiver) {
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
		return "CheckLocalCacheTracker";
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
