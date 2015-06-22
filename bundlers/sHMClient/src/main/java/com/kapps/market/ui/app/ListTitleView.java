package com.kapps.market.ui.app;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapps.market.R;
import com.kapps.market.bean.AppItem;
import com.kapps.market.ui.ListElement;
import com.kapps.market.ui.ListItemsBroswer;
import com.kapps.market.ui.RatingView;
import com.kapps.market.ui.SectionListElement;
import com.kapps.market.util.Constants;

public abstract class ListTitleView extends ListItemsBroswer {
	public static final String TAG = "AppListView";

	/**
	 * @param context
	 */
	public ListTitleView(Context context) {
		super(context);

	}

	/**
	 * @param context
	 */
	public ListTitleView(Context context, boolean useLongClick) {
		super(context, useLongClick);

	}

	@Override
	protected BaseAdapter createItemAdapter() {
		BaseAdapter appListAdapter = new AppListApdapter(getContext());
		return appListAdapter;
	}

	protected class AppListApdapter extends BaseAdapter {
		private Context context;
		protected ArrayList<ListElement> resultList;

		private LayoutInflater layoutInflater;

		public AppListApdapter(Context context) {
			super();
			this.context = context;
			this.layoutInflater = (LayoutInflater) context
					.getSystemService("layout_inflater");
			this.resultList = new ArrayList<ListElement>();
			
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			 return appCahceManager.getAppItemCount(mTaskMark);
//			return this.resultList.size();
		}

		@Override
		 public AppItem getItem(int position) {
//		public Object getItem(int position) {
			// TODO Auto-generated method stub
			 return appCahceManager.getAppItemByMarkIndex(mTaskMark,position);
//			return this.resultList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		/*
		 * (non-Javadoc) ����ֻ��app count ��Ϊ0��ʱ��Żᱻ���á�
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// ����Ƿ�Ҫ�����µ���
			 if (position == (getCount() - 1)) {
			 tryQueryNewItems();
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
//			return this.resultList.get(position).getViewForListElement(
//					layoutInflater, context, convertView);
			 return convertView;
		}

		public void addList(List<ListElement> elements) {

			this.resultList.addAll(elements);

		}

		@Override
		public boolean isEnabled(int position) {

			return this.resultList.get(position).isClickable();
		}
		public void addSectionHeaderItem(String text) {

			SectionListElement element = new SectionListElement();

			element.setText(text);

			this.resultList.add(element);

		}
	}

	// ��������ͼ
	protected View createItemView(ViewGroup parent) {
		View convertView = LayoutInflater.from(getContext()).inflate(
				R.layout.app_item, parent, false);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.iconView = ((ImageView) convertView
				.findViewById(R.id.appIconView));
		viewHolder.ratingView = ((RatingView) convertView
				.findViewById(R.id.appRatingView));
		viewHolder.nameLabel = (TextView) convertView
				.findViewById(R.id.appNameLabel);
		viewHolder.develpoerView = (TextView) convertView
				.findViewById(R.id.appDeveloperLabel);
		viewHolder.versionView = (TextView) convertView
				.findViewById(R.id.appVersionLabel);
		viewHolder.appPaymentView = (TextView) convertView
				.findViewById(R.id.appPaymentLabel);
		convertView.setTag(viewHolder);

		return convertView;
	}

	// ��ʼ������ͼ
	protected void initItemView(View convertView, AppItem appItem, int position) {
		ViewHolder viewHolder = (ViewHolder) convertView.getTag();

		// ��ı���
		initItemBg(convertView, position);

		// ����Ϣ
		initBaseItemInfo(viewHolder, appItem);

		// ״̬, ������ʱ�򲻴����Ա���������
		if (scrolling) {
			if (appItem.isFree()) {
				viewHolder.appPaymentView.setText(getResources().getString(
						R.string.free));

			} else {
				viewHolder.appPaymentView.setText("��" + appItem.getPrice());
			}

		} else {
			String viweing = marketManager.getAppViewDescribe(appItem);
			if (viweing != null) {
				viewHolder.appPaymentView.setText(viweing);

			} else if (appItem.isFree()) {
				viewHolder.appPaymentView.setText(getResources().getString(
						R.string.free));

			} else {
				viewHolder.appPaymentView.setText("��" + appItem.getPrice());
			}
		}

		// ͼ��
		Drawable icon = assertCacheManager.getAppIconFromCache(appItem.getId());
		if (icon == null) {
			viewHolder.iconView.setImageDrawable(marketContext.emptyAppIcon);
		} else {
			viewHolder.iconView.setImageDrawable(icon);
		}
	}

	// ����Ϣ
	protected void initBaseItemInfo(ViewHolder viewHolder, AppItem appItem) {
		// ����
		viewHolder.nameLabel.setText(appItem.getName());
		// ������
		viewHolder.develpoerView.setText(appItem.getAuthorName());
		// �汾
		viewHolder.versionView.setText(getResources().getString(
				R.string.version_colon)
				+ appItem.getVersion());
		// ����
		viewHolder.ratingView.setRating(appItem.getRating());
	}

	/**
	 * ֻΪ���������б����<br>
	 * ����֮�ã���ʵ��
	 */
	@Override
	protected void initItemBg(View convertView, int position) {
		if (position % 2 == 0) {
			convertView.setBackgroundResource(R.drawable.list_item_even_bg);

		} else {
			convertView.setBackgroundResource(R.drawable.list_item_odd_bg);
		}

	}

	// ��ͼ���
	protected class ViewHolder {
		public ImageView iconView;
		public RatingView ratingView;
		public TextView nameLabel;
		public TextView versionView;
		public TextView develpoerView;
		public TextView appPaymentView;
	}

	@Override
	protected void handleRealItemClick(Object item) {
		marketContext.handleShowAppDetail(((AppItem) item).getId());
	}

	@Override
	protected void handleRealItemLongClick(Object item) {
		Dialog dialog = null;
		final AppItem appItem = (AppItem) item;
		// ����ֱ�����ص����
		if (appItem.isFree()) {
			dialog = new AlertDialog.Builder(getContext())
					.setTitle(appItem.getName())
					.setItems(R.array.app_can_download_long_click,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									if (whichButton == 0) {
										Message message = Message.obtain();
										message.what = Constants.M_FAVOR_ADDED;
										message.arg1 = getMessageMark();
										message.obj = appItem;
										marketContext
												.handleMarketMessage(message);

									} else {
										Message message = Message.obtain();
										message.what = Constants.M_QUICK_DOWNLOAD_APP;
										message.obj = appItem;
										message.arg1 = getMessageMark();
										marketContext
												.handleMarketMessage(message);

									}
									dialog.dismiss();
								}
							}).create();
			dialog.show();
		}
	}

}
