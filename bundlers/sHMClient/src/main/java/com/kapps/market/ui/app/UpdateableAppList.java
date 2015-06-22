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
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kapps.market.R;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.Software;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.SoftwareUpdateTaskMark;
import com.kapps.market.ui.ListItemsBroswer;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

/**
 * 2011-3-19<br>
 * �ɸ�������б�
 * 
 * @author admin
 * 
 */
public class UpdateableAppList extends ListItemsBroswer implements OnClickListener {

	public UpdateableAppList(Context context) {
		super(context, true, false);

	}

	@Override
	protected View createTopFrame(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.common_view_title, null);
		Button button = (Button) view.findViewById(R.id.titleFunButton);
		button.setText(R.string.update_all);
		button.setOnClickListener(this);

		return view;
	}

	@Override
	protected void handleLoadNewItems(ATaskMark taskMark) {
		if (taskMark.getTaskStatus() == ATaskMark.HANDLE_DOING) {
			serviceWraper.forceTakeoverTask(this, taskMark);

		} else {
			marketContext.checkSoftwareUpdate(false);
			serviceWraper.forceTakeoverTask(this, taskMark);
		}
	}

	@Override
	protected BaseAdapter createItemAdapter() {
		return new UpdateableListApdapter(mTaskMark);
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		super.receiveResult(taskMark, exception, trackerResult);
		if (taskMark instanceof SoftwareUpdateTaskMark) {
			marketContext.receiveResult(taskMark, exception, trackerResult);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.titleFunButton) {
			Adapter adapter = getMAdapter();
			int count = adapter.getCount();
			if (count == 0) {
				Toast.makeText(getContext(), getContext().getString(R.string.none_software_need_update), 200).show();
				return;
			}

			// ���ѡ���Ӧ��
			List<AppItem> itemList = new ArrayList<AppItem>();
			BaseApp baseApp = null;
			AppItem appItem = null;
			for (int index = 0; index < count; index++) {
				appItem = (AppItem) adapter.getItem(index);
				// �ڱ���
				baseApp = marketManager.getLcoalAppItem(appItem);
				if (baseApp == null) {
					itemList.add(appItem);
				}
			}

			if (itemList.size() > 0) {
				// ִ������
				Message message = Message.obtain();
				message.what = Constants.M_BATCH_UPDATE_APP;
				message.obj = itemList;
				marketContext.handleMarketMessage(message);

			} else {
				Message message = Message.obtain();
				message.what = Constants.M_BATCH_UPDATE_APP_LOCAL;
				marketContext.handleMarketMessage(message);
			}

			notifyDataSetChanged();
		}

	}

	// �ɸ�������б�������
	protected class UpdateableListApdapter extends AAppItemAdapter implements OnClickListener {

		/**
		 * @param aTaskMark
		 */
		public UpdateableListApdapter(ATaskMark aTaskMark) {
			super(aTaskMark);
		}

		@Override
		protected View createItemView(ViewGroup parent) {
			View convertView = LayoutInflater.from(getContext()).inflate(R.layout.software, null);
			SoftwareViewHolder viewHolder = new SoftwareViewHolder();
			viewHolder.iconView = ((ImageView) convertView.findViewById(R.id.appIconView));
			viewHolder.versionView = (TextView) convertView.findViewById(R.id.appVersionLabel);
			viewHolder.nameView = (TextView) convertView.findViewById(R.id.appNameLabel);
			viewHolder.sizeView = (TextView) convertView.findViewById(R.id.sizeLabel);
			viewHolder.infoView = (TextView) convertView.findViewById(R.id.infoLabel);
			viewHolder.funView = convertView.findViewById(R.id.funButton);
			viewHolder.funView.setOnClickListener(this);
			convertView.setTag(viewHolder);

			return convertView;
		}

		@Override
		protected void initItemView(View convertView, AppItem appItem, int position) {
			initItemBg(convertView, position);

			BaseApp software = appItem;
			SoftwareViewHolder viewHolder = (SoftwareViewHolder) convertView.getTag();
			// ����
			viewHolder.nameView.setText(software.getName());
			// �汾
			viewHolder.versionView.setText(getResources().getString(R.string.version_colon) + software.getVersion());
			// ��С
			viewHolder.sizeView.setText(getResources().getString(R.string.size_colon) + Util.getSizeDes(software));

			// ״̬
			if (!scrolling) {
				String des = marketManager.getAppViewDescribe(software);
				if (des != null) {
					viewHolder.infoView.setText(des);
				} else {
					viewHolder.infoView.setText(R.string.updatable);
				}

			} else {
				viewHolder.infoView.setText(R.string.updatable);
			}

			// ��ʾ
			viewHolder.funView.setTag(appItem);
			updateFunView(software, viewHolder.funView);

			// ͼ��
			Drawable icon = assertCacheManager.getAppIconFromCache(software.getIconId());
			if (icon == null) {
				viewHolder.iconView.setImageDrawable(marketContext.emptyAppIcon);
			} else {
				viewHolder.iconView.setImageDrawable(icon);
			}

		}

		protected void updateFunView(BaseApp baseApp, View funView) {
			Button button = (Button) funView;

			int state = marketManager.getJointSoftwareState(baseApp);
			button.setEnabled(!marketManager.isInHandling(state));
			if (state == BaseApp.APP_DOWNLOADED) {
				button.setText(R.string.install);

			} else {
				button.setText(R.string.update);
			}
		}

		@Override
		protected void loadNewItems() {

		}

		// �������ܿ���
		private class SoftwareViewHolder {
			ImageView iconView;
			TextView versionView;
			TextView nameView;
			TextView sizeView;
			TextView infoView;
			View funView;
		}

		@Override
		public void onClick(View v) {
			AppItem baseApp = (AppItem) v.getTag();
			int state = marketManager.getJointSoftwareState(baseApp);
			if (state == BaseApp.APP_DOWNLOADED) {
				String apkPath = marketManager.getLcoalApkPath(baseApp);
				if (apkPath != null) {
					marketManager.installSoftware(apkPath);
				}

			} else {
				Message message = Message.obtain();
				message.what = Constants.M_QUICK_DOWNLOAD_APP;
				message.obj = baseApp;
				message.arg1 = getMessageMark();
				marketContext.handleMarketMessage(message);
			}
		}

	}

	@Override
	protected void handleRealItemClick(Object item) {
		final AppItem appItem = (AppItem) item;
		Dialog dialog = new AlertDialog.Builder(getContext()).setTitle(appItem.getName())
				.setItems(R.array.update_item_click, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {

						if (whichButton == 0) {
							marketContext.handleShowAppDetail(appItem.getId());

						} else if (whichButton == 1) {
							Software software = marketManager.getSoftware(appItem.getPackageName());
							if (software != null) {
								marketContext.handleShowAppDetail(software.getPackageName(), software.getVersionCode());
							}
						}

						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}

}
