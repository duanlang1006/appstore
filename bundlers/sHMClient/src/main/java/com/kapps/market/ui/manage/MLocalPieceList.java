package com.kapps.market.ui.manage;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kapps.market.R;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.ui.ListItemsBroswer;

/**
 * 2011-3-19 <br>
 * Ƭ���� �����ڱ��ص������Ϣ
 * 
 * @author admin
 */
public abstract class MLocalPieceList extends ListItemsBroswer {
	// �б���
	protected List<? extends BaseApp> itemList = new ArrayList<BaseApp>();
	// ������Դ
	private int titleRes;
	// ͷ������
	private TextView textView;

	public MLocalPieceList(Context context, int titleRes,
			List<? extends BaseApp> itemList, boolean fiexHeght) {
		super(context, true, fiexHeght);
		this.itemList = itemList;
		this.titleRes = titleRes;
	}

	public MLocalPieceList(Context context, int titleRes,
			List<? extends BaseApp> itemList) {
		super(context, true, true);
		this.itemList = itemList;
		this.titleRes = titleRes;
	}

	@Override
	protected View createTopFrame(Context context) {
		textView = new TextView(context);
		textView.setBackgroundResource(R.drawable.intro_title);
		textView.setPadding(3, 0, 0, 0);
		textView.setTextColor(getResources().getColor(
				R.color.app_info_title_text_color));
		textView.setTextSize(16);
		textView.setGravity(Gravity.CENTER_HORIZONTAL);
		textView.setText(titleRes);
		return textView;
	}

	@Override
	protected BaseAdapter createItemAdapter() {
		return new PieceAdapter();
	}

	@Override
	protected StatusFrame createStatusFrame(Context context) {
		return null;
	}

	// ���
	private class PieceAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return itemList.size();
		}

		@Override
		public Object getItem(int position) {
			return itemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// ����
			if (convertView == null) {
				convertView = createItemView(position, parent);
			}

			initItemBg(convertView, position);

			// ��ʼ��
			initItemView(convertView, position, getItem(position));

			return convertView;
		}

	}

	/**
	 * ��������ͼ
	 */
	protected abstract View createItemView(int position, ViewGroup parent);

	/**
	 * ��ʼ������ͼ
	 */
	protected abstract void initItemView(View convertView, int position,
			Object item);

}
