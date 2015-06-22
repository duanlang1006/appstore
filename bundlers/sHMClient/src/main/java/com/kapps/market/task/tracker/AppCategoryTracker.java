package com.kapps.market.task.tracker;

import com.kapps.market.bean.AppCategory;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;

import java.util.List;



/**
 * 2010-6-26 <br>
 * Ӧ�����Ĵ��?
 * 
 * @author admin
 * 
 */
public class AppCategoryTracker extends AInvokeTracker {

	public static final String TAG = "AppCategoryTracker";

	/**
	 * @param iReference
	 */
	public AppCategoryTracker(IResultReceiver resultReceiver) {
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
		List<AppCategory> categoryList = (List<AppCategory>) result.getResultData();
		if (categoryList != null) {
			appCahceManager.setCategoryList(categoryList);
		}
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
