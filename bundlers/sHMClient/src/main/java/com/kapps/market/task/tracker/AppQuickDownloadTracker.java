package com.kapps.market.task.tracker;


import com.kapps.market.bean.AppItem;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.AppQuickDownloadTaskMark;

/**
 * 2011-5-11<br>
 * �ڻ�������Ҫ���������
 * 
 * @author shuizhu
 * 
 */
public class AppQuickDownloadTracker extends AppSummaryTracker {

	/**
	 * @param resultReceiver
     */
	public AppQuickDownloadTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hiapk.marketmob.tracker.AppSummaryTracker#handleResult(com.hiapk.
	 * marketmob .task.OperateResult)
	 */
	@Override
	public void handleResult(OperateResult result) {
		super.handleResult(result);

		if (trackerResult != null) {
			AppItem appItem = (AppItem) trackerResult;
			LogUtil.e(TAG, appItem.getName() + "dfdfdfdf");
			// �������Դ
			AppQuickDownloadTaskMark qdTaskMark = (AppQuickDownloadTaskMark) result.getTaskMark();

			marketContext.handleSimpleDownloadBaseItem(appItem);
			
		}
	}
}
