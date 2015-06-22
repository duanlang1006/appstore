package com.kapps.market.ui.app;

import java.util.List;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kapps.market.R;
import com.kapps.market.bean.AppCategory;
import com.kapps.market.bean.PageInfo;
import com.kapps.market.cache.AppCahceManager;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppListTaskMark;
import com.kapps.market.task.mark.CategoryTaskMark;
import com.kapps.market.ui.TabableAppView;
import com.kapps.market.util.Constants;
import com.kapps.market.util.ResourceEnum;

/**
 * Ӧ�������ҳ��
 * 
 * @author admin
 * 
 */
public class AppCategoryPage extends TabableAppView implements IResultReceiver {

	public static final String TAG = "AppCategoryPage";
	// ����
	private AppCahceManager appCahceManager = marketContext.getAppCahceManager();

	// �����Ӧ����ʾ��ͼ
	public static final int APP_BROSWER_VIEW = 1;
	// ��һ�����ͼ
	public static final int SINGLE_BROSWER_VIEW = 2;
	// ���������б�Ҫע��
	public static final int PROGRESS_VIEW = 3;
	// ����б�
	public static final int CATEGORY_VIEW = 4;

	/**
	 * @param context
	 */
	public AppCategoryPage(Context context) {
		super(context);

		addView(R.layout.m_category_page);

		registerView(CATEGORY_VIEW);
		registerView(APP_BROSWER_VIEW);
		registerView(SINGLE_BROSWER_VIEW);

		// �����ͼ
		showChoosedView(PROGRESS_VIEW);

		initCategoryLabel(getResources().getString(R.string.software_category));

		initCategory();

	}

	// ��ʼ�����Ŀ¼
	private void initCategory() {
		List<AppCategory> appCategoryList = appCahceManager.getCategoryList();
		// �Ѿ����ع���,Ĭ��Ϊ3����
		if (appCategoryList.size() == 3) {
			doInitCategoryView();

		} else {
			serviceWraper.getAppCategoryList(this, marketContext.getTaskMarkPool().getCategoryTask());
		}
	}

	// ����ǩ
	public void initCategoryLabel(String pLabel) {
		// �������
		TextView textView = (TextView) findViewById(R.id.titleLabel);
		textView.setText(pLabel);

	}

	// ʵ�ʵĳ�ʼ��
	private void doInitCategoryView() {
		showChoosedView(CATEGORY_VIEW);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.ui.CommonAppView#rotateContentView()
	 */
	@Override
	public boolean rotateContentView() {
		int viewMark = getCurrentTabMark();
		// �۵�
		if (viewMark == APP_BROSWER_VIEW || viewMark == SINGLE_BROSWER_VIEW) {
			showChoosedView(CATEGORY_VIEW);
			return true;

		} else {
			return false;
		}
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		LogUtil.d(TAG, "receive result taskMark: " + taskMark + " exception: " + exception);
		// ��ʼ�����
		if (taskMark instanceof CategoryTaskMark) {
			if (taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
				doInitCategoryView();

			} else {
				View view = findViewById(R.id.loadButton);
				if (view != null) {
					view.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	@Override
	protected View createContentView(int viewMark) {
		switch (viewMark) {
		case CATEGORY_VIEW:
			AppCCBrowsePage ccBrowsePage = new AppCCBrowsePage(getContext(), this);
			ccBrowsePage.initCCBrowse(appCahceManager.getCategoryList());
			return ccBrowsePage;

		case APP_BROSWER_VIEW:
			return new AppCABrowseView(getContext());

		case SINGLE_BROSWER_VIEW:
			return new AppListSingleView(getContext());

		case PROGRESS_VIEW:
			View proView = LayoutInflater.from(getContext()).inflate(R.layout.category_loadbar_view, null);
			proView.findViewById(R.id.loadButton).setOnClickListener(this);
			return proView;
		}
		return null;
	}

	@Override
	public void handleChainMessage(Message msg) {
		switch (msg.what) {
		case Constants.M_SHOW_CATEGORY_APP:
			doShowCategoryApp((AppCategory) msg.obj);
			break;

		default:
			break;
		}
	}

	/**
	 * ��ʾ����µ����
	 * 
	 * @param appCategory
	 */
	private void doShowCategoryApp(AppCategory appCategory) {
		// ��ͨ���
		initCategoryLabel(appCategory.getName());
		// ��ͨ���
		if (appCategory.getType() == ResourceEnum.CATEGORY_TYPE_COMMON) {
			showChoosedView(APP_BROSWER_VIEW, appCategory);

		} else if (appCategory.getType() == ResourceEnum.CATEGORY_TYPE_PICKED) {
			showChoosedView(SINGLE_BROSWER_VIEW, appCategory);
		}
	}

	@Override
	protected void onAfterShowView(int viewMark, Object data) {
		switch (viewMark) {
		case CATEGORY_VIEW:
			initCategoryLabel(getResources().getString(R.string.software_category));
			break;

		case APP_BROSWER_VIEW:
			// ��ͨ��ͼ
			AppCABrowseView appBrowseView = (AppCABrowseView) getCurrentTab();
			appBrowseView.initAppBrowse((AppCategory) data);
			break;

		case SINGLE_BROSWER_VIEW:
			// ʹ�þɵ�view�������³�ʼ������
			AppListSingleView appListView = (AppListSingleView) getCurrentTab();
			appListView.initSingleView((AppCategory) data);
			break;
		}
	}

	/*
	 * (non-Javadoc) û��trigger
	 */
	@Override
	protected int getShowViewMark(View trigger) {
		throw new UnsupportedOperationException("app category page: getShowViewMark");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.ui.TabableAppView#onClick(android.view.View)
	 */
	@Override
	public void onClick(View trigger) {
		if (trigger.getId() == R.id.loadButton) {
			CategoryTaskMark taskMark = marketContext.getTaskMarkPool().getCategoryTask();
			trigger.setVisibility(View.INVISIBLE);
			if (taskMark.getTaskStatus() == ATaskMark.HANDLE_ERROR) {
				initCategory();
			}

		} else {
			super.onClick(trigger);
		}
	}

	// �б��µ�Ӧ���б�
	private class AppListSingleView extends AppListView {
		/**
		 * @param context
		 */
		public AppListSingleView(Context context) {
			super(context, true);
		}

		public void initSingleView(AppCategory appCategory) {
			initLoadleList(taskMarkPool.getAppListTaskMark(appCategory.getId(), ResourceEnum.SORT_DALL_NUM_DESC,
					ResourceEnum.FEE_NONE_TYPE));
		}

		@Override
		protected void handleLoadNewItems(ATaskMark taskMark) {
			AppListTaskMark listTaskMark = (AppListTaskMark) taskMark;
			PageInfo pageInfo = listTaskMark.getPageInfo();
			serviceWraper.getAppListByRecommend(this, listTaskMark, listTaskMark.getCategory(), listTaskMark
					.getSortType(), pageInfo.getNextPageIndex(), pageInfo.getPageSize());
		}
	}

}
