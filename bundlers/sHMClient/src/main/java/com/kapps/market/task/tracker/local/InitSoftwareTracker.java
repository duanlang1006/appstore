package com.kapps.market.task.tracker.local;

import com.kapps.market.bean.Software;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.local.InitSoftwareSummaryTaskMark;
import com.kapps.market.task.tracker.AInvokeTracker;

import java.util.List;

/**
 * 2010-6-26<br>
 * ��ñ�������б�
 * 
 * @author admin
 * 
 */
public class InitSoftwareTracker extends AInvokeTracker {

	private static final String TAG = "InitSoftwareTracker";

	public InitSoftwareTracker(IResultReceiver iReceiver) {
		super(iReceiver);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleResult(OperateResult result) {
		// TODO Auto-generated method stub
		InitSoftwareSummaryTaskMark taskWraper = (InitSoftwareSummaryTaskMark) result.getTaskMark();
		// ���óɹ�
		List<Software> softwareList = (List<Software>) result.getResultData();
		if (softwareList != null) {
			// ��ӵ�����
			marketManager.setSoftwareList(softwareList);
		}

		LogUtil.d(TAG, "handleResult items: " + (softwareList == null ? "null" : softwareList.size()) + " taskWraper: "
				+ taskWraper);

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
