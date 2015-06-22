package com.kapps.market.task.tracker;

import com.kapps.market.bean.LoginResult;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.ATaskMark;

/**
 * 2010-6-26 <br>
 * ��½����
 * 
 * @author admin
 * 
 */
public class LoginTracker extends AInvokeTracker {

	public static final String TAG = "LoginTracker";

	/**
	 * @param iReference
	 */
	public LoginTracker(IResultReceiver resultReceiver) {
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
		// ��½�ɹ�
		ATaskMark taskWraper = result.getTaskMark();
		LoginResult loginResult = (LoginResult) result.getResultData();
		marketContext.getSharedPrefManager().saveSession(loginResult.getSessinId());

		LogUtil.d(TAG, "taskWraper: " + taskWraper + "\nloginResult: " + loginResult);
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
