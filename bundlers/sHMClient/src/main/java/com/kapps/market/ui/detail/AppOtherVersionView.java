package com.kapps.market.ui.detail;

import com.kapps.market.bean.AppItem;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.ui.app.AppListView;

import android.content.Context;

/**
 * 2012-05-2 <br>
 * ��ʷ���
 * 
 * @author LL
 * 
 */
public class AppOtherVersionView extends AppListView {

	private AppItem appItem;

	public AppOtherVersionView(Context context, AppItem appItem) {
		super(context, true);
		this.appItem = appItem;
	}


	@Override
	protected void handleLoadNewItems(ATaskMark taskMark) {
		marketContext.getServiceWraper().getHistoryAppList(this, taskMark, appItem.getId(), appItem.getPackageName());
	}

}
