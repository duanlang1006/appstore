package com.kapps.market.task.tracker;


import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;

/**
 * 2010-6-22 <br>
 * ��������
 * 
 * @author admin
 * 
 */
public class ChannelReportTracker extends AInvokeTracker {
	public static final String TAG = "ChannelReportTracker";

	/**
	 * @param resultReceiver
	 */
	public ChannelReportTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);

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
	 * @see com.ck.market.task.tracker.AInvokeTracker#handleResult(com.ck.market
	 * .task.OperateResult)
	 */
	@Override
	public void handleResult(OperateResult result) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SimpleReportTracker [toString()=" + super.toString() + "]";
	}

}
