package com.kapps.market.ui.app;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.kapps.market.R;
import com.kapps.market.bean.AppCategory;
import com.kapps.market.bean.PageInfo;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppListTaskMark;
import com.kapps.market.ui.TabableAppView;
import com.kapps.market.util.Constants;
import com.kapps.market.util.ResourceEnum;

/**
 * App list of a Category, for example:  日常实用.
 * 
 * @author admin
 * 
 */
public class AppCABrowseView extends TabableAppView {

	public static final String TAG = "AppCABrowseView";
	// ������id
	private AppCategory appCategory;
	// ����
	private Button tab1Button, tab2Button, tab3Button;

	public static final int VIEW_TAB1_TYPE = 1;
	public static final int VIEW_TAB2_TYPE = 2;
	public static final int VIEW_TAB3_TYPE = 3;

	/**
	 * @param context
	 */
	public AppCABrowseView(Context context) {
		super(context);
		addView(R.layout.app_browse_view);

		// ��ʼ�����ť���򵥵�����ע��������Ա�����ͼ��ʱ��
		// ������ͼ��ѡ��
		registerView(VIEW_TAB1_TYPE);
		registerView(VIEW_TAB2_TYPE);
		registerView(VIEW_TAB3_TYPE);

		// ���
		tab1Button = (Button) findViewById(R.id.tab1Button);
		registerTrigger(tab1Button);

		tab2Button = (Button) findViewById(R.id.tab2Button);
		registerTrigger(tab2Button);

		tab3Button = (Button) findViewById(R.id.tab3Button);
		registerTrigger(tab3Button);

	}

	/**
	 * ��ʼ������б�
	 * 
	 * @param mainType
	 */
	public void initAppBrowse(AppCategory appCategory) {
		Log.d(TAG, "appCategory: " + appCategory);
		this.appCategory = appCategory;

		// Ĭ��ѡ���һ��
		showChoosedView(VIEW_TAB1_TYPE);
	}

	@Override
	protected void onBeforeShowView(int viewMark, Object data) {
		int oldMark = getCurrentTabMark();
		setButtonSelected(oldMark, false);
		setButtonSelected(viewMark, true);
	}

	private void setButtonSelected(int mark, boolean selected) {
		Button currentButton = null;
		switch (mark) {
		case VIEW_TAB1_TYPE:
			currentButton = tab1Button;
			break;

		case VIEW_TAB2_TYPE:
			currentButton = tab2Button;
			break;

		case VIEW_TAB3_TYPE:
			currentButton = tab3Button;
			break;
		}
		if (currentButton != null) {
			currentButton.setSelected(selected);
		}
	}

	// �����µ�

	@Override
	protected View createContentView(int viewMark) {
		switch (viewMark) {
		case VIEW_TAB1_TYPE:
			return new CaAppListView(getContext());

		case VIEW_TAB2_TYPE:
			return new CaAppListView(getContext());

		case VIEW_TAB3_TYPE:
			return new CaAppListView(getContext());

		default:
			return null;
		}

	}

	@Override
	protected void onAfterShowView(int viewMark, Object data) {
		AppListView appListView = null;
		switch (viewMark) {
		case VIEW_TAB1_TYPE:
			// ��ͨ��ͼ
			appListView = (AppListView) getCurrentTab();
			appListView.initLoadleList(taskMarkPool.getAppListTaskMark(appCategory.getId(),
					ResourceEnum.SORT_DAY_DOWNLOAD, ResourceEnum.FEE_FREE_TYPE));
			break;

		case VIEW_TAB2_TYPE:
			// ʹ�þɵ�view�������³�ʼ������
			appListView = (AppListView) getCurrentTab();
			appListView.initLoadleList(taskMarkPool.getAppListTaskMark(appCategory.getId(),
					ResourceEnum.SORT_USER_RATING, ResourceEnum.FEE_FREE_TYPE));
			break;

		case VIEW_TAB3_TYPE:
			// ʹ�þɵ�view�������³�ʼ������
			appListView = (AppListView) getCurrentTab();
			appListView.initLoadleList(taskMarkPool.getAppListTaskMark(appCategory.getId(),
					ResourceEnum.SORT_ADD_TIME_DESC, ResourceEnum.FEE_FREE_TYPE));
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.ui.TabableAppView#getShowViewMark(android.view.View)
	 */
	@Override
	protected int getShowViewMark(View trigger) {
		switch (trigger.getId()) {
		case R.id.tab1Button:
			return VIEW_TAB1_TYPE;

		case R.id.tab2Button:
			return VIEW_TAB2_TYPE;

		case R.id.tab3Button:
			return VIEW_TAB3_TYPE;

		default:
			return Constants.NONE_VIEW;
		}
	}

	// �б��µ�Ӧ���б�
	private class CaAppListView extends AppListView {

		public CaAppListView(Context context) {
			super(context, true);
		}

		@Override
		protected void handleLoadNewItems(ATaskMark taskMark) {
			AppListTaskMark listTaskMark = (AppListTaskMark) taskMark;
			PageInfo pageInfo = listTaskMark.getPageInfo();
			serviceWraper.getAppListByCategory(this, listTaskMark, listTaskMark.getCategory(), listTaskMark
					.getSortType(), listTaskMark.getFeeType(), pageInfo.getNextPageIndex(), pageInfo.getPageSize(),
					true);
		}

	}

}
