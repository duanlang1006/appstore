package com.kapps.market.task.tracker.local;

import com.kapps.market.bean.Software;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.tracker.AInvokeTracker;

import java.util.List;

/**
 * 2010-6-26<br>
 * �������Ŀ¼��apk�ļ�
 * 
 * @author admin
 * 
 */
public class InitLocalApkFileTracker extends AInvokeTracker {

	private static final String TAG = "InitLocalApkFileTracker";

	public InitLocalApkFileTracker(IResultReceiver iReceiver) {
		super(iReceiver);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleResult(OperateResult result) {
		ATaskMark taskWraper = result.getTaskMark();
		// ���óɹ�
		List<Software> apkList = (List<Software>) result.getResultData();
		if (apkList != null) {
			// ��ӵ�����
			marketManager.setLocalApkList(apkList);
		}

		LogUtil.d(TAG, "*****************init local apk size: " + marketManager.getLocalApkList().size()
				+ "\ntaskWraper: " + taskWraper);
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

}
