package com.kapps.market.task.tracker;


import com.kapps.market.bean.AppItem;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AddFavorTaskMark;
import com.kapps.market.task.mark.DeleteFavorTaskMark;

/**
 * 2010-6-27<br>
 * ��ӻ���ɾ���ղ�
 * 
 * @author admin
 * 
 */
public class ADFavorTracker extends AInvokeTracker {

	public static final String TAG = "ADFavorTracker";

	/**
	 * @param resultReceiver
	 */
	public ADFavorTracker(IResultReceiver resultReceiver) {
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
		return TAG;
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

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ck.market.task.tracker.AInvokeTracker#handleFault(com.ck.market
	 * .task.OperateResult)
	 */
	@Override
	public void handleFault(OperateResult result) {
		AppItem appItem = (AppItem) result.getAttach();
		ATaskMark taskMark = result.getTaskMark();
		ATaskMark favorTaskMark = marketContext.getTaskMarkPool().getAppFavorTaskMark();
		if (taskMark instanceof DeleteFavorTaskMark) {
			appCahceManager.addAppItemToCache(favorTaskMark, appItem);

		} else if (taskMark instanceof AddFavorTaskMark) {
			appCahceManager.deleteAppItemIndexFromCache(favorTaskMark, appItem);

		}
	}

}
