package com.kapps.market.ui.manage;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kapps.market.R;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.PageInfo;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppFavorTaskMark;
import com.kapps.market.ui.ListItemsBroswer;
import com.kapps.market.ui.app.AAppItemAdapter;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

/**
 * 锟揭碉拷喜锟斤拷<br>
 * 
 * @author admin
 * 
 */
public class AppFavorView extends ListItemsBroswer implements OnClickListener {

	public static final String TAG = "AppFavorView";

	/**
	 * @param context
	 */
	public AppFavorView(Context context) {
		super(context);

	}

	@Override
	protected View createTopFrame(Context context) {
		return super.createTopFrame(context);
		/* by shuizhu
		View view = LayoutInflater.from(context).inflate(R.layout.title_band, null);
		Button button = (Button) view.findViewById(R.id.titleFunButton);
		button.setText(R.string.download_choosed_software);
		button.setOnClickListener(this);
		view.findViewById(R.id.iconLabel).setVisibility(INVISIBLE);
		view.findViewById(R.id.chooseBox).setVisibility(INVISIBLE);
		funcView = view;
		funcView.setVisibility(View.INVISIBLE);
		return view;
		*/
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.titleFunButton) {
			Adapter adapter = getMAdapter();
			int count = adapter.getCount();
			if (count == 0) {
				Toast.makeText(marketContext, marketContext.getString(R.string.favoliten_choose_need), 200).show();
				return;
			}

			// 锟斤拷锟窖★拷锟斤拷应锟斤拷
			List<AppItem> itemList = new ArrayList<AppItem>();
			AppItem tempItem = null;
			for (int index = 0; index < count; index++) {
				tempItem = (AppItem) adapter.getItem(index);
				if (tempItem.isChoose()) {
					itemList.add(tempItem);
					tempItem.setChoose(false);
				}
			}

			// 锟窖达拷锟节憋拷锟截碉拷全锟斤拷锟狡筹拷
			List<AppItem> localedList = new ArrayList<AppItem>();
			BaseApp baseApp = null;
			for (AppItem appItem : itemList) {
				// 锟节憋拷锟斤拷
				baseApp = marketManager.getLcoalAppItem(appItem);
				if (baseApp != null) {
					localedList.add(appItem);
				}
			}
			itemList.removeAll(localedList);

			if (itemList.size() > 0) {
				// 执锟斤拷锟斤拷锟斤拷
				Message message = Message.obtain();
				message.what = Constants.M_BATCH_DOWNLOAD_APP;
				message.arg1 = getMessageMark();
				message.obj = itemList;
				marketContext.handleMarketMessage(message);

			} else {
				Toast.makeText(marketContext, marketContext.getString(R.string.favoliten_choose_need), 200).show();
			}

			notifyDataSetChanged();
		}

	}

	@Override
	protected void handleLoadNewItems(ATaskMark taskMark) {
		Log.d("test", "handleLoadNewItems->in");
		AppFavorTaskMark favorTaskMark = (AppFavorTaskMark) taskMark;
		PageInfo pageInfo = favorTaskMark.getPageInfo();
		if (!marketContext.isSessionLocalValid()) {  //change by linhanye 20150307   去掉点击"下载任务tab" 要求登入问题
			//marketContext.handleSessionTimeOut(true);
			//Toast.makeText(AppFavorView.this.getContext(), getResources().getString(R.string.use_personal_center_must_login), 500).show();
		}else serviceWraper.getAppFavorList(this, favorTaskMark, null, pageInfo.getNextPageIndex(), pageInfo.getPageSize());
	}

	@Override
	protected BaseAdapter createItemAdapter() {
		Log.d("test", "createAdapter->");
		return new ApklistAdapter(mTaskMark);
	}

	@Override
	public void handleRealItemClick(final Object item) {
		final AppItem appItem = (AppItem) item;
		final String path = marketManager.getLcoalApkPath(appItem);
		Dialog dialog = null;
		if (path == null) {
			dialog = new AlertDialog.Builder(getContext()).setTitle(appItem.getName())
					.setItems(R.array.app_favor_click, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							if (whichButton == 0) {
								handleFavorDeleteItem(appItem);
								notifyDataSetChanged();
							} else if (whichButton == 1) {
								Message message = Message.obtain();
								message.what = Constants.M_QUICK_DOWNLOAD_APP;
								message.obj = appItem;
								message.arg1 = getMessageMark();
								marketContext.handleMarketMessage(message);

							} else {
								marketContext.handleShowAppDetail(appItem.getId());

							}
							dialog.dismiss();
						}
					}).create();

		} else {
			dialog = new AlertDialog.Builder(getContext()).setTitle(appItem.getName())
					.setItems(R.array.app_favor_downloaded_click, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							if (whichButton == 0) {
								handleFavorDeleteItem(appItem);

							} else if (whichButton == 1) {
								marketManager.installSoftware(path);

							} else {
								marketContext.handleShowAppDetail(appItem.getId());

							}
							dialog.dismiss();
						}
					}).create();
		}

		dialog.show();
	}

	private void handleFavorDeleteItem(AppItem appItem) {
		// 通知删锟斤拷
		Message message = Message.obtain();
		message.what = Constants.M_FAVOR_DELETE;
		message.obj = appItem;
		marketContext.handleMarketMessage(message);

		LogUtil.d(TAG, "favor : delete item = " + appItem);
	}

	/**
	 * apk锟侥硷拷锟叫憋拷 (锟节诧拷锟斤拷锟斤拷锟斤拷锟斤拷)
	 */
	private class ApklistAdapter extends AAppItemAdapter implements OnClickListener {

		public ApklistAdapter(ATaskMark aTaskMark) {
			super(aTaskMark);
		}

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.chooseBox) {
				AppItem appItem = (AppItem) view.getTag();
				if (appItem != null) {
					CheckBox checkBox = (CheckBox) view;
					appItem.setChoose(checkBox.isChecked());
				}
			}
		}

		// 锟斤拷锟杰匡拷锟斤拷
		private class ApkViewHolder {
			ImageView iconView;
			TextView versionView;
			TextView nameView;
			TextView sizeView;
			TextView infoView;
			CheckBox chooseBox;
		}

		@Override
		protected View createItemView(ViewGroup parent) {
			View convertView = LayoutInflater.from(getContext()).inflate(R.layout.favor_app, parent, false);
			ApkViewHolder viewHolder = new ApkViewHolder();
			// 锟斤拷图锟斤拷
			viewHolder.iconView = (ImageView) convertView.findViewById(R.id.appIconView);
			// 锟斤拷锟斤拷
			viewHolder.nameView = (TextView) convertView.findViewById(R.id.appNameLabel);
			// 锟芥本
			viewHolder.versionView = (TextView) convertView.findViewById(R.id.appVersionLabel);
			// 锟斤拷小
			viewHolder.sizeView = (TextView) convertView.findViewById(R.id.appSizeLabel);
			// 锟斤拷息
			viewHolder.infoView = (TextView) convertView.findViewById(R.id.appInfoLabel);
			// 锟斤拷装锟斤拷钮
			viewHolder.chooseBox = (CheckBox) convertView.findViewById(R.id.chooseBox);
			if (viewHolder.chooseBox != null) {
				viewHolder.chooseBox.setOnClickListener(this);
			}

			convertView.setTag(viewHolder);
			return convertView;
		}

		@Override
		protected void initItemView(View convertView, AppItem appItem, int position) {
			initItemBg(convertView, position);

			// updateItemBg(convertView, position);
			ApkViewHolder viewHolder = (ApkViewHolder) convertView.getTag();

			// 锟斤拷锟斤拷
			viewHolder.nameView.setText(appItem.getName());
			// 锟芥本
			viewHolder.versionView.setText(getResources().getString(R.string.version_colon) + appItem.getVersion());
			// 锟斤拷小
			viewHolder.sizeView.setText(getResources().getString(R.string.size_colon) + Util.getSizeDes(appItem));

			// 锟斤拷应item锟斤拷位锟斤拷
			if (viewHolder.chooseBox != null) {
				viewHolder.chooseBox.setTag(appItem);
				viewHolder.chooseBox.setChecked(appItem.isChoose());
				viewHolder.chooseBox.setEnabled(!marketManager.isInHandling(appItem));
			}

			// 状态, 锟斤拷锟斤拷锟斤拷时锟津不达拷锟斤拷锟皆憋拷锟斤拷锟斤拷锟斤拷锟斤拷
			if (scrolling) {
				// 锟斤拷锟斤拷锟斤拷锟较�
				viewHolder.infoView.setText("");

			} else {
				String viweing = marketManager.getAppViewDescribe(appItem);
				if (viweing != null) {
					viewHolder.infoView.setText(viweing);

				} else {
					viewHolder.infoView.setText("");
				}

			}

			// 图锟斤拷
			Drawable icon = assertCacheManager.getAppIconFromCache(appItem.getId());
			if (icon == null) {
				viewHolder.iconView.setImageDrawable(marketContext.emptyAppIcon);
			} else {
				viewHolder.iconView.setImageDrawable(icon);
			}

		}

		@Override
		protected void loadNewItems() {
			Log.d("test", "appfavor,adapter");
			tryQueryNewItems();
		}

	}
	

}
