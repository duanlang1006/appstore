package com.kapps.market.task.tracker;

import com.kapps.market.bean.UserInfo;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;

/**
 * 2010-6-26 <br>
 * ��½����
 * 
 * @author admin
 * 
 */
public class RegistTracker extends AInvokeTracker {

	public static final String TAG = "RegistTracker";

	/**
	 * @param iReference
	 */
	public RegistTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ck.market.task.tracker.AInvokeTracker#handleInvokeOver(com.hiapk
	 * .market.task.OperateResult)
	 */
	@Override
	public void handleResult(OperateResult result) {
		UserInfo userInfo = (UserInfo) result.getAttach();
		// ��¼�û����ֺ�����
		marketContext.getSharedPrefManager().saveUserInfo(userInfo);
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
