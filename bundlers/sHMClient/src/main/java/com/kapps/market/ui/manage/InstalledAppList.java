package com.kapps.market.ui.manage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapps.market.R;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.Software;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.local.InitSoftwareSummaryTaskMark;
import com.kapps.market.task.mark.local.SoftwareDetailInfoTaskMark;
import com.kapps.market.ui.ListItemsBroswer;
import com.kapps.market.util.Util;

/**
 * 2011-3-19
 * 
 * @author admin
 * 
 */
// �Ѱ�װ����б����ʵ��
public class InstalledAppList extends ListItemsBroswer {

	// ���ظ�����ݵ�����
	private ATaskMark detailInfoTask;

	public InstalledAppList(Context context) {
		super(context);

		detailInfoTask = taskMarkPool.getSoftwareDetailInfoTaskMark();

	}

	@Override
	protected void tryQueryNewItems() {
		// �������Ѿ�������ϣ����Լ�����ϸ��Ϣ��
		if (mTaskMark.getTaskStatus() == ATaskMark.HANDLE_OVER
				&& detailInfoTask.getTaskStatus() == ATaskMark.HANDLE_WAIT) {
			serviceWraper.initSoftwareDetailInfoList(this, detailInfoTask,
					marketManager.getSoftwareList());

		} else if (detailInfoTask.getTaskStatus() == ATaskMark.HANDLE_DOING) {
			serviceWraper.forceTakeoverTask(this, detailInfoTask);

		} else if (mTaskMark.getTaskStatus() == ATaskMark.HANDLE_DOING) {
			serviceWraper.forceTakeoverTask(this, mTaskMark);
			updateViewStatus(mTaskMark);
		}
	}

	@Override
	protected void handleLoadNewItems(ATaskMark taskMark) {
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception,
			Object trackerResult) {
		super.receiveResult(taskMark, exception, trackerResult);

		if (taskMark instanceof InitSoftwareSummaryTaskMark
				&& taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER
				&& detailInfoTask.getTaskStatus() == ATaskMark.HANDLE_WAIT) {
			serviceWraper.initSoftwareDetailInfoList(this, detailInfoTask,
					marketManager.getSoftwareList());

		} else if (taskMark instanceof SoftwareDetailInfoTaskMark) {
			// ֪ͨ��ݸı�
			notifyDataSetChanged();
		}
	}

	@Override
	protected BaseAdapter createItemAdapter() {
		return new SoftwareAdapter();
	}

	// ���
	private class SoftwareAdapter extends BaseAdapter implements
			OnClickListener {

		@Override
		public int getCount() {
			return marketManager.getSoftwareList().size();
		}

		@Override
		public Object getItem(int position) {
			return marketManager.getSoftwareList().get(position);
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

		protected View createItemView(int position, ViewGroup parent) {
			View convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.software, null);
			SoftwareViewHolder viewHolder = new SoftwareViewHolder();
			viewHolder.iconView = ((ImageView) convertView
					.findViewById(R.id.appIconView));
			viewHolder.versionView = (TextView) convertView
					.findViewById(R.id.appVersionLabel);
			viewHolder.nameView = (TextView) convertView
					.findViewById(R.id.appNameLabel);
			viewHolder.sizeView = (TextView) convertView
					.findViewById(R.id.sizeLabel);
			viewHolder.infoView = (TextView) convertView
					.findViewById(R.id.infoLabel);
			viewHolder.funView = convertView.findViewById(R.id.funButton);
			viewHolder.funView.setOnClickListener(this);
			convertView.setTag(viewHolder);

			return convertView;
		}

		protected void updateFunView(BaseApp baseApp, View funView) {
			if (marketManager.isInHandling(baseApp)) {
				funView.setEnabled(false);
			} else {
				funView.setEnabled(true);
			}
		}

		protected void initItemView(View convertView, int position, Object item) {
			Software software = (Software) item;
			SoftwareViewHolder viewHolder = (SoftwareViewHolder) convertView
					.getTag();
			// ����
			viewHolder.nameView.setText(software.getName());
			// �汾
			viewHolder.versionView.setText(getResources().getString(
					R.string.version_colon)
					+ software.getVersion());
			// ��С
			viewHolder.sizeView.setText(getResources().getString(
					R.string.size_colon)
					+ Util.getSizeDes(software));

			// ��ʾ
			if (scrolling) {
				viewHolder.infoView.setText(R.string.installed);

			} else {
				if (software.isUpdate()) {
					viewHolder.infoView.setText(R.string.updatable);
				} else {
					viewHolder.infoView.setText(R.string.installed);
				}
			}

			viewHolder.funView.setTag(position);
			updateFunView(software, viewHolder.funView);

			// ͼ��
			Drawable icon = assertCacheManager.getAppIconFromCache(software
					.getIconId());
			if (icon == null) {
				viewHolder.iconView
						.setImageDrawable(marketContext.emptyAppIcon);
			} else {
				viewHolder.iconView.setImageDrawable(icon);
			}

		}

		@Override
		public void onClick(View v) {
			int index = (Integer) v.getTag();
			Adapter baseAdapter = getMAdapter();
			if (baseAdapter.getCount() > index) {
				BaseApp software = (BaseApp) baseAdapter.getItem(index);
				marketContext.getMarketManager().uninstallSoftware(
						software.getPackageName());
			}
		}

		// �������ܿ���
		private class SoftwareViewHolder {
			ImageView iconView;
			TextView versionView;
			TextView nameView;
			TextView infoView;
			TextView sizeView;
			View funView;
		}
	}

	@Override
	protected void handleRealItemClick(Object item) {
		final Software software = (Software) item;
		Dialog dialog = new AlertDialog.Builder(getContext())
				.setTitle(software.getName())
				.setItems(R.array.software_click,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {

								if (whichButton == 0) {
									marketManager.uninstallSoftware(software
											.getPackageName());

								} else if (whichButton == 1) {
									marketManager.openSoftware(software
											.getPackageName());

								} else if (whichButton == 2) {
									String pname = software.getPackageName();
									int versionCode = software.getVersionCode();
									marketContext.handleShowAppDetail(pname,
											versionCode);

								} else {
									marketManager.showInstalledAppDetail(
											(Activity) getContext(),
											software.getPackageName());
								}

								dialog.dismiss();
							}
						}).create();

		dialog.show();
	}

}
