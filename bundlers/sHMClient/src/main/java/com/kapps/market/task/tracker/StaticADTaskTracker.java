package com.kapps.market.task.tracker;

import android.content.SharedPreferences;

import com.kapps.market.bean.StaticAD;
import com.kapps.market.bean.config.MarketConfig;
import com.kapps.market.cache.CacheConstants;
import com.kapps.market.cache.LocaleCacheManager;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;

/**
 * ��̬���
 * 
 * @author shuizhu
 * 
 */
public class StaticADTaskTracker extends AInvokeTracker {

	public StaticADTaskTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
	}

	@Override
	public String TAG() {
		return "StaticADTaskTracker";
	}

	// ��ʾһ�μ���Ч
	@Override
	public void handleResult(OperateResult result) {
		LocaleCacheManager localeCacheManager = marketContext.getLocaleCacheManager();
		StaticAD staticAD = (StaticAD) result.getResultData();

		if (staticAD != null) {
			SharedPreferences markPreferences = marketContext.getSharedPrefManager().getMarketConfigPref();
			long oldAdId = markPreferences.getLong(MarketConfig.STATIC_AD_ID, -1);
			if (staticAD.getId() > oldAdId) {
				markPreferences.edit().putLong(MarketConfig.STATIC_AD_ID, staticAD.getId()).commit();
				localeCacheManager.writeDataToCache(CacheConstants.STATIC_AD_INFO, staticAD);

			} else {
				localeCacheManager.deleteCacheData(CacheConstants.STATIC_AD_INFO);
			}

		} else {
			localeCacheManager.deleteCacheData(CacheConstants.STATIC_AD_INFO);
		}

	}

}
