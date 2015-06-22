package com.kapps.market.ui.detail;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kapps.market.R;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.PageInfo;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AuthorAppTaskMark;
import com.kapps.market.ui.app.AppListView;

/**
 * Ӧ�õĿ�����Ա<br>
 * 
 * @author admin
 */
public class AppDeveloperView extends AppListView {

	public static final String TAG = "AppDeveloperView";

	private AppItem appItem;

	/**
	 * @param context
	 */
	public AppDeveloperView(Context context, AppItem appItem) {
		super(context, true);
		this.appItem = appItem;

	}

	@Override
	protected BaseAdapter createItemAdapter() {
		return new AuthorAppApdapter();
	}

	@Override
	protected void addListHeader(ListView listView) {
		// TODO Auto-generated method stub
		View headerView = LayoutInflater.from(getContext()).inflate(R.layout.app_develper, null);
		if (appItem != null) {
			TextView textView = (TextView) headerView.findViewById(R.id.devSiteContentLabel);
			textView.setText(appItem.getAppDetail().getAuthorSite());

			textView = (TextView) headerView.findViewById(R.id.emailContentLabel);
			textView.setText(appItem.getAppDetail().getAuthorEmail());
		}
		listView.addHeaderView(headerView, null, false);
	}

	// ������
	private class AuthorAppApdapter extends AppListApdapter {
		private List<AppItem> itemList = new ArrayList<AppItem>();

		/**
		 * ����۳�ǰӦ��
		 */
		@Override
		public int getCount() {
			if (itemList.size() == 0) {
				int count = appCahceManager.getAppItemCount(mTaskMark);
				if (count > 1) { // �ų�����
					List<AppItem> autorAppLit = appCahceManager.getAppItemList(mTaskMark);
					for (AppItem item : autorAppLit) {
						if (appItem.getId() != item.getId()) {
							itemList.add(item);
						}
					}
				}
			}
			return itemList.size();
		}

		/**
		 * ���ǰӦ��
		 */
		@Override
		public AppItem getItem(int position) {
			AppItem item = itemList.get(position);
			return item;
		}
	}

	@Override
	protected void handleLoadNewItems(ATaskMark taskMark) {
		AuthorAppTaskMark authorTaskMark = (AuthorAppTaskMark) taskMark;
		PageInfo pageInfo = authorTaskMark.getPageInfo();
		serviceWraper.getAppListByDeveloper(this, authorTaskMark, appItem.getId(), authorTaskMark.getAuthor(),
				pageInfo.getNextPageIndex(), pageInfo.getPageSize());
	}
}
