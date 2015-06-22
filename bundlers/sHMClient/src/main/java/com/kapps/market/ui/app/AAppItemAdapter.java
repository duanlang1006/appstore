package com.kapps.market.ui.app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kapps.market.MApplication;
import com.kapps.market.bean.AppItem;
import com.kapps.market.cache.AppCahceManager;
import com.kapps.market.task.mark.ATaskMark;

/**
 * 2011-3-21 <br>
 * 
 * @author admin
 * 
 */
public abstract class AAppItemAdapter extends BaseAdapter {

	private AppCahceManager appCahceManager;
	private ATaskMark aTaskMark;

	public AAppItemAdapter(ATaskMark aTaskMark) {
		this.aTaskMark = aTaskMark;
		appCahceManager = MApplication.getInstance().getAppCahceManager();
	}

	@Override
	public int getCount() {
		return appCahceManager.getAppItemCount(aTaskMark);
	}

	@Override
	public AppItem getItem(int position) {
		return appCahceManager.getAppItemByMarkIndex(aTaskMark, position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ����Ƿ�Ҫ�����µ���
		if (position == (getCount() - 1)) {
			loadNewItems();
		}
		// ��ǰλ����ͼ
		if (convertView == null) {
			convertView = createItemView(parent);
		}
		// ��ó�ʼ���õ���ͼ
		AppItem appItem = getItem(position);
		if (appItem != null) {
			initItemView(convertView, appItem, position);
		}

		return convertView;
	}

	/**
	 * @param convertView
	 * @param appItem
	 */
	protected abstract void initItemView(View convertView, AppItem appItem, int position);

	/**
	 * @param parent
	 * @return
	 */
	protected abstract View createItemView(ViewGroup parent);

	/**
	 * �����µ���
	 */
	protected abstract void loadNewItems();

}
