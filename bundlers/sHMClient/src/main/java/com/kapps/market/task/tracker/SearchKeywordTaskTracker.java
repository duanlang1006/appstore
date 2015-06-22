package com.kapps.market.task.tracker;

import com.kapps.market.bean.KeyWord;
import com.kapps.market.cache.AppCahceManager;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;

import java.util.List;

public class SearchKeywordTaskTracker extends AInvokeTracker {

	public SearchKeywordTaskTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
	}

	@Override
	public String TAG() {
		return "StaticADTaskTracker";
	}

	// ��ʾһ�μ���Ч
	@Override
	public void handleResult(OperateResult result) {
		AppCahceManager appCacheManager = marketContext.getAppCahceManager();
		List<KeyWord> keyword = (List<KeyWord>)result.getResultData();
		appCacheManager.addKeyWordToCache(keyword);
	}


}
