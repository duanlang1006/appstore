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
public class BatchBackupSoftwareTracker extends AInvokeTracker {

	private static final String TAG = "BatchBackupSoftwareTracker";

	public BatchBackupSoftwareTracker(IResultReceiver iReceiver) {
		super(iReceiver);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleResult(OperateResult result) {
		ATaskMark taskWraper = result.getTaskMark();
		// �����ӵı���apk
		List<Software> backupedList = (List<Software>) result.getResultData();
		if (backupedList != null) {
			// ��ӵ�����
			for (Software software : backupedList) {
				marketManager.addBackupApk(software);
			}
		}

		LogUtil.d(TAG, "*****************back backup apk size: " + marketManager.getBackupApkList().size()
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
