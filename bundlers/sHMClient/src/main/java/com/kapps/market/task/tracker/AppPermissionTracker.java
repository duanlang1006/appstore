package com.kapps.market.task.tracker;

import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.AppPermission;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.AppPermissionTaskMark;

import java.util.List;

/**
 * 2010-6-26<br>
 * Ӧ���������ṹ��������
 * 
 * @author admin
 * 
 */
public class AppPermissionTracker extends AInvokeTracker {

	private static final String TAG = "AppPermissionTracker";

	public AppPermissionTracker(IResultReceiver iReceiver) {
		super(iReceiver);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleResult(OperateResult result) {
		AppPermissionTaskMark taskMark = (AppPermissionTaskMark) result.getTaskMark();
		AppItem appItem = appCahceManager.getAppItemById(taskMark.getAppId());
		List<AppPermission> permissionList = (List<AppPermission>) result.getResultData();
		if (permissionList != null) {
			appItem.setPermissionList(permissionList);
		}
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
