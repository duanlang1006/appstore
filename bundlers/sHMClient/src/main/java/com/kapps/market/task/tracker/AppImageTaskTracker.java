package com.kapps.market.task.tracker;


import com.kapps.market.bean.MImageType;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.AppImageTaskMark;

/**
 * 2010-7-25 <br>
 * Ӧ�õ�ͼƬ����
 * 
 * @author Administrator
 * 
 */
public class AppImageTaskTracker extends AInvokeTracker {

	public static final String TAG = "AppImageTaskTracker";

	/**
	 * @param resultReceiver
	 */
	public AppImageTaskTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hiapk.market.task.tracker.AInvokeTracker#TAG()
	 */
	@Override
	public String TAG() {
		return TAG;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hiapk.market.task.tracker.AInvokeTracker#handleResult(com.hiapk.market
	 * .task.OperateResult)
	 */
	@Override
	public void handleResult(OperateResult result) {
		AppImageTaskMark taskMark = (AppImageTaskMark) result.getTaskMark();
		byte[] bytes = (byte[]) result.getResultData();
		if (taskMark.getType() == MImageType.APP_SCREENSHOT) {
			assertCacheManager.addScreenshotsByteToCache(taskMark.getUrl(), bytes);

		} else if (taskMark.getType() == MImageType.APP_ICON || taskMark.getType() == MImageType.APK_ICON) {
			assertCacheManager.addAppIconByteToCache(taskMark.getId(), bytes);

		} else if (taskMark.getType() == MImageType.APP_ADVERTISE_ICON) {
			assertCacheManager.addAdvertiseIconByteToCache(taskMark.getId(), bytes);

		} else if (taskMark.getType() == MImageType.CATEGORY_ICON) {
			assertCacheManager.addCategoryIconByteToCache(taskMark.getId(), bytes);
		}
	}

}
