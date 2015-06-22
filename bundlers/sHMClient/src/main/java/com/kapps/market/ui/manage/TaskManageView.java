package com.kapps.market.ui.manage;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kapps.market.MarketManager;
import com.kapps.market.R;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.DownloadItem;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.local.InitDownloadTaskMark;
import com.kapps.market.ui.BaseItemList;
import com.kapps.market.ui.CommonView;
import com.kapps.market.ui.LoadableList;
import com.kapps.market.util.Constants;

import java.util.List;

/**
 * 2011-3-24
 * 
 * @author admin
 * 
 */
public class TaskManageView extends CommonView implements IResultReceiver {

	private MarketManager marketManager;

	// �������б�
	private DownloadAppList downloadAppList;
	private DownloadAppList downloadingAppList;
	private DownloadAppList downloadfailAppList;

	// û�����Ҫʾ�Ե���ʾ��
	private LinearLayout noDateNoteLayout;

	/**
	 * @param context
	 */
	public TaskManageView(Context context) {
		super(context);

		marketManager = marketContext.getMarketManager();

		initPage(context);

		validList();

		// �ӹܹ�ע������
		serviceWraper.forceTakeoverTask(this, taskMarkPool.getInitDownloadTaskMark());
	}

	// init three list view here.
	private void initPage(Context context) {
		LinearLayout rootLayout = new LinearLayout(getContext());
		rootLayout.setOrientation(LinearLayout.VERTICAL);

		// downloading
		List<DownloadItem> downloadedList = marketManager.getTaskMap().get(BaseApp.APP_DOWNLOADING);
		downloadingAppList = new DownloadAppList(context, R.string.downloading, downloadedList);
		downloadingAppList.initLoadleList(taskMarkPool.getInitDownloadTaskMark());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		rootLayout.addView(downloadingAppList, layoutParams);

		// download stop
		downloadedList = marketManager.getTaskMap().get(BaseApp.APP_DOWNLOAD_STOP);
		downloadfailAppList = new DownloadAppList(context, R.string.download_task_stop, downloadedList);
		downloadfailAppList.initLoadleList(taskMarkPool.getInitDownloadTaskMark());
		layoutParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		rootLayout.addView(downloadfailAppList, layoutParams);

		// has download
		downloadedList = marketManager.getTaskMap().get(BaseApp.APP_DOWNLOADED);
		downloadAppList = new DownloadAppList(context, R.string.downloaded, downloadedList);
		downloadAppList.initLoadleList(taskMarkPool.getInitDownloadTaskMark());
		layoutParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		rootLayout.addView(downloadAppList, layoutParams);

		ViewGroup.LayoutParams lParams = new ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT);
		ScrollView scrollView = new CusScrollView(getContext());

		// ���û�������ʾ�򣨿�ʼ��
		TextView tv = new TextView(getContext());
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(18);
		tv.setTextColor(R.color.band_title_text_color);
		tv.setText(R.string.empty_task_item);
		tv.setBackgroundResource(R.drawable.intro_title);

		noDateNoteLayout = new LinearLayout(getContext());
		noDateNoteLayout.setOrientation(LinearLayout.VERTICAL);
		noDateNoteLayout.addView(tv, layoutParams);
		rootLayout.addView(noDateNoteLayout);
		// ���û�������ʾ�򣨽���

		scrollView.addView(rootLayout, lParams);

		MarginLayoutParams scrollLayoutParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(scrollView, scrollLayoutParams);

		showNoDataNote();
	}

	/**
	 * �Զ��ж��費��Ҫ��ʾ"û�������ʾ��"
	 */
	private void showNoDataNote() {
		if (downloadingAppList.getDataItemCount() == 0 && downloadfailAppList.getDataItemCount() == 0
				&& downloadAppList.getDataItemCount() == 0) {
			noDateNoteLayout.setVisibility(VISIBLE);

		} else {
			noDateNoteLayout.setVisibility(GONE);

		}
	}

	@Override
	public void flushView(int what) {
        Log.d("temp", "TaskMgrView flushView what:" + what);
		if (what == Constants.M_DOWNLOAD_PROGRESS) { // �������Ͽ�������ϸ��һ��
			validList(downloadingAppList);
            downloadingAppList.notifyDataSetChanged();
		} else {
			validList();
			notifyLoadIcon(downloadAppList);
			notifyLoadIcon(downloadingAppList);
			notifyLoadIcon(downloadfailAppList);
			showNoDataNote();
		}
		// Log.d(TAG, "manage page flush view.");

	}

	// ȫ�����
	private void validList() {
		validList(downloadAppList);
		validList(downloadingAppList);
		validList(downloadfailAppList);
	}

	// �ӳ�û���б����ÿһ������ô����
	private void validList(LoadableList loadableList) {
		if (loadableList.getDataItemCount() == 0) {
			loadableList.setVisibility(GONE);
		} else {
			loadableList.notifyDataSetChanged();
			loadableList.setVisibility(VISIBLE);
		}
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		if (taskMark instanceof InitDownloadTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			marketContext.receiveResult(taskMark, exception, trackerResult);
			downloadAppList.receiveResult(taskMark, exception, trackerResult);
			validList(downloadAppList);
			validList(downloadingAppList);
			validList(downloadfailAppList);
		}
	}

	// Ϊ�˴������
	public class CusScrollView extends ScrollView {

		public CusScrollView(Context context) {
			super(context);
		}

		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			super.onScrollChanged(l, t, oldl, oldt);
			// ���ֱ����Ϊ0��ô֪ͨͼ����أ�����һ������
			if (Math.abs(t - oldt) <= 5) {
				notifyLoadIcon(downloadAppList);
				notifyLoadIcon(downloadingAppList);
				notifyLoadIcon(downloadfailAppList);

			} else {
				notifyQuitLoadIcon(downloadAppList);
				notifyQuitLoadIcon(downloadingAppList);
				notifyQuitLoadIcon(downloadfailAppList);
			}
		}

	}

	// ֪ͨ����ͼ��
	private void notifyLoadIcon(BaseItemList loadableList) {
		if (loadableList.getVisibility() == VISIBLE) {
			loadableList.handleNeedUpdateIcon();
		}
	}

	private void notifyQuitLoadIcon(BaseItemList loadableList) {
		if (loadableList.getVisibility() == VISIBLE) {
			loadableList.handleQuitUpdateIcon();
		}
	}

}
