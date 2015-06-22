package com.kapps.market.ui.manage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kapps.market.R;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.DownloadItem;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

import java.util.List;

/**
 * 2011-3-19<br>
 * ��������б����ʵ��
 * 
 * @author admin
 * 
 */
public class DownloadAppList extends MLocalPieceList implements OnClickListener {

	public DownloadAppList(Context context, int titleRes, List<? extends BaseApp> itemList) {
		super(context, titleRes, itemList);
	}

	@Override
	protected void handleLoadNewItems(ATaskMark taskMark) {
	}

	@Override
	protected View createItemView(int position, ViewGroup parent) {
		View convertView = LayoutInflater.from(getContext()).inflate(R.layout.download_task, null);
		TaskViewHolder viewHolder = new TaskViewHolder();
		viewHolder.iconView = (ImageView) convertView.findViewById(R.id.appIconView);
		viewHolder.nameView = (TextView) convertView.findViewById(R.id.appNameLabel);
		viewHolder.versionView = (TextView) convertView.findViewById(R.id.appVersionLabel);
		viewHolder.sizeView = (TextView) convertView.findViewById(R.id.sizeLabel);
		viewHolder.stateView = (TextView) convertView.findViewById(R.id.downloadStateLabel);
		viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
		viewHolder.merFunButton = (Button) convertView.findViewById(R.id.merFunButton);
		viewHolder.merFunButton.setOnClickListener(this);
		viewHolder.merFun2Button = (Button) convertView.findViewById(R.id.merFun2Button);
		viewHolder.merFun2Button.setOnClickListener(this);
		convertView.setTag(viewHolder);

		return convertView;
	}

	@Override
	protected void initItemView(View convertView, int position, Object item) {
		DownloadItem downloadItem = (DownloadItem) item;
		TaskViewHolder viewHolder = (TaskViewHolder) convertView.getTag();
		// ����
		viewHolder.nameView.setText(downloadItem.getName());
		// �汾
		viewHolder.versionView.setText(getResources().getString(R.string.version_colon) + downloadItem.getVersion());
		// ��С
		viewHolder.sizeView.setText(getResources().getString(R.string.size_colon) + Util.getSizeDes(downloadItem));

		// ��Ӧitem��λ��
		viewHolder.merFunButton.setTag(position);
		viewHolder.merFun2Button.setTag(position);

		// ���״̬���ø���Ӧ��
		int state = downloadItem.getState();
		if (state == BaseApp.APP_INSTALLING || state == BaseApp.APP_INSTALLED) {
			viewHolder.merFunButton.setVisibility(View.INVISIBLE);
			viewHolder.merFun2Button.setVisibility(View.INVISIBLE);
			viewHolder.progressBar.setVisibility(View.VISIBLE);
			viewHolder.progressBar.setIndeterminate(true);
			viewHolder.stateView.setText(R.string.installing);

		} else {
			// ���ؽ����Ϣ
			viewHolder.merFunButton.setVisibility(View.VISIBLE);
			if (state == BaseApp.APP_DOWNLOADED) {
				viewHolder.stateView.setText(R.string.downloaded);
				viewHolder.progressBar.setVisibility(View.INVISIBLE);
				viewHolder.merFun2Button.setVisibility(View.INVISIBLE);
				viewHolder.merFunButton.setText(getResources().getString(R.string.install));

			} else {
				// �����
				viewHolder.progressBar.setProgress((int) (100.0 * downloadItem.getdSize() / downloadItem.getSize()));
				if (state == BaseApp.APP_DOWNLOADING) {
					viewHolder.stateView.setText(R.string.downloading);
					viewHolder.merFunButton.setText(getResources().getString(R.string.cancel));
					viewHolder.merFun2Button.setVisibility(View.VISIBLE);
					viewHolder.merFun2Button.setText(getResources().getString(R.string.stop));
					viewHolder.progressBar.setVisibility(View.VISIBLE);

				} else if (state == BaseApp.APP_DOWNLOAD_STOP) {
					viewHolder.stateView.setText(R.string.stop);
					viewHolder.merFun2Button.setVisibility(View.INVISIBLE);
					viewHolder.merFunButton.setText(getResources().getString(R.string.retry));
					viewHolder.progressBar.setVisibility(View.VISIBLE);
				}
			}
		}
		// ͼ��
		Drawable icon = assertCacheManager.getAppIconFromCache(downloadItem.getAppId());
		if (icon == null) {
			viewHolder.iconView.setImageDrawable(marketContext.emptyAppIcon);
		} else {
			viewHolder.iconView.setImageDrawable(icon);
		}
	}

	// �������ܿ���
	private class TaskViewHolder {
		ImageView iconView;
		TextView versionView;
		TextView nameView;
		TextView sizeView;
		TextView stateView;
		ProgressBar progressBar;
		Button merFunButton;
		Button merFun2Button;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.merFunButton) {
			int index = (Integer) v.getTag();
			Adapter adapter = getMAdapter();
			if (adapter.getCount() > index) {
				DownloadItem downloadItem = (DownloadItem) adapter.getItem(index);
				if (downloadItem.getState() == BaseApp.APP_DOWNLOADING) {
					marketContext.handleCancelTask(downloadItem);

				} else if (downloadItem.getState() == BaseApp.APP_DOWNLOAD_STOP) {
					Message message = Message.obtain();
					message.what = Constants.M_DOWNLOAD_RETRY;
					message.arg1 = getMessageMark();
					message.obj = downloadItem;
					marketContext.handleMarketMessage(message);

				} else if (downloadItem.getState() == BaseApp.APP_DOWNLOADED) {
					marketManager.installSoftware(downloadItem.getSavePath());
				}
				LogUtil.d(TAG, "task : handle software = " + downloadItem);
			}

		} else if (v.getId() == R.id.merFun2Button) {
			Adapter adapter = getMAdapter();
			int index = (Integer) v.getTag();
			if (adapter.getCount() > index) {
				DownloadItem downloadItem = (DownloadItem) adapter.getItem(index);
				if (downloadItem.getState() == BaseApp.APP_DOWNLOADING) {
					marketContext.handleStopTask(downloadItem);
				}
				LogUtil.d(TAG, "task : handle software = " + downloadItem);
			}
		}
	}

	@Override
	protected void handleRealItemClick(Object item) {
		final DownloadItem downloadItem = (DownloadItem) item;
		if (downloadItem.getState() == BaseApp.APP_INSTALLING) {
			return;
		}

		Dialog dialog = null;
		if (downloadItem.getState() == BaseApp.APP_DOWNLOADED) {
			dialog = new AlertDialog.Builder(getContext()).setTitle(downloadItem.getName()).setItems(
					R.array.task_downloaded_click, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							if (whichButton == 0) {
								marketManager.installSoftware(downloadItem.getSavePath());

							} else if (whichButton == 1) {
								marketContext.handleDeleteDownloadItem(downloadItem);

							} else {
								marketContext.handleShowAppDetail(downloadItem.getAppId());
							}
							dialog.dismiss();
						}
					}).create();
			dialog.show();

		} else if (downloadItem.getState() == BaseApp.APP_DOWNLOADING) {
			dialog = new AlertDialog.Builder(getContext()).setTitle(downloadItem.getName()).setItems(
					R.array.task_downloading_click, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							if (whichButton == 0) {
								// �����������ȷ��״̬
								if (downloadItem.getState() != BaseApp.APP_DOWNLOADED) {
									marketContext.handleCancelTask(downloadItem);
								}

							} else {
								marketContext.handleShowAppDetail(downloadItem.getAppId());
							}
							dialog.dismiss();
						}
					}).create();
			dialog.show();

		} else {
			dialog = new AlertDialog.Builder(getContext()).setTitle(downloadItem.getName()).setItems(
					R.array.task_downloaded_lf_click, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							if (whichButton == 0) {
								// �����������ȷ��״̬
								if (downloadItem.getState() != BaseApp.APP_DOWNLOADED) {
									marketContext.handleCancelTask(downloadItem);
								}

							} else if (whichButton == 1) {
								Message message = Message.obtain();
								message.what = Constants.M_DOWNLOAD_RETRY;
								message.arg1 = getMessageMark();
								message.obj = downloadItem;
								marketContext.handleMarketMessage(message);

							} else {
								marketContext.handleShowAppDetail(downloadItem.getAppId());
							}
							dialog.dismiss();
						}
					}).create();
			dialog.show();
		}
	}

}
