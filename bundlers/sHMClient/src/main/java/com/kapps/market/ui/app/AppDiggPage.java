package com.kapps.market.ui.app;

import android.content.Context;
import android.view.View;

import com.kapps.market.R;
import com.kapps.market.bean.PageInfo;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppListTaskMark;
import com.kapps.market.ui.TabableAppView;
import com.kapps.market.util.ResourceEnum;

/**
 * ����ҳ��
 * 
 * @author admin
 * 
 */
public class AppDiggPage extends TabableAppView {

	public static final String TAG = "AppDiggPage";

	public static final int TOP_DIGG_VIEW = 0;

	/**
	 * @param context
	 */
	public AppDiggPage(Context context) {
		super(context);
		addView(R.layout.m_digg_page);

		registerView(TOP_DIGG_VIEW);

		// Ĭ��ѡ���һ��
		showChoosedView(TOP_DIGG_VIEW);
	}

	/*
	 * 
	 * ��ͼ��Ǽ�ΪӦ�û����ǡ�<br> ����ʵ����ÿ�ζ������¡�<br> ��������������������ֻ������һ�Ρ�
	 */
	@Override
	protected View createContentView(int viewMark) {
		switch (viewMark) {
		case TOP_DIGG_VIEW:
			AppListView topFreeListView = new DiggAppListView(getContext());
			topFreeListView.initLoadleList(marketContext.getTaskMarkPool().getAppListTaskMark(
					ResourceEnum.SORT_YEAR_DOWNLOAD, ResourceEnum.INVALID));
			return topFreeListView;

		default:
			return null;
		}
	}

	@Override
	protected int getShowViewMark(View trigger) {
		// TODO Auto-generated method stub
		return 0;
	}

	// Ӧ���б�
	private class DiggAppListView extends AppListView {

		/**
		 * @param context
		 */
		public DiggAppListView(Context context) {
			super(context, true);
		}

		@Override
		protected void handleLoadNewItems(ATaskMark taskMark) {
			AppListTaskMark listTaskMark = (AppListTaskMark) taskMark;
			PageInfo pageInfo = listTaskMark.getPageInfo();
			serviceWraper.getAppListByTopDownload(this, listTaskMark, listTaskMark.getSortType(),
					pageInfo.getNextPageIndex(), pageInfo.getPageSize());

		}

	}

}
