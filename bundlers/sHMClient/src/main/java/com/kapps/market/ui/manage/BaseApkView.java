package com.kapps.market.ui.manage;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapps.market.R;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.Software;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.ui.ListItemsBroswer;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

/**
 * ����apk/�ɱ���/�ѱ���<br>
 * 
 */
public abstract class BaseApkView extends ListItemsBroswer {

	public static final String TAG = "BaseApkView";

	// ���ظ�����ݵ�����
	protected ATaskMark detailInfoTask;

	/**
	 * ���췽��
	 * 
	 * @param context
	 */
	public BaseApkView(Context context) {
		super(context);
		detailInfoTask = getDetailInfoTaskMark();
	}

	@Override
	protected boolean isCanLoadItemImage() {
		return (mTaskMark.getTaskStatus() != ATaskMark.HANDLE_DOING && detailInfoTask.getTaskStatus() != ATaskMark.HANDLE_DOING);
	}

	@Override
	protected boolean isNeedDispatchItemClick(Object item) {
		return !marketManager.isInHandling((BaseApp) item);
	}

	/**
	 * ������ϸ��ϸ ����Ĭ���Ǽ��ر���apk����Ϣ�������Ҫ��Ҫ���ǡ�
	 */
	protected void doLoadMoreInfo() {
		serviceWraper.initApkDetailInfoList(this, detailInfoTask, getSummaryInfoList(), getCacheMark());
		updateTopFrameStatus(detailInfoTask);
	}

	@Override
	protected void tryQueryNewItems() {
		// �������Ѿ�������ϣ����Լ�����ϸ��Ϣ��
		if (mTaskMark.getTaskStatus() == ATaskMark.HANDLE_DOING) {
			// �ӹܽӹ�
			serviceWraper.forceTakeoverTask(this, mTaskMark);
			// �����ͼ
			updateTopFrameStatus(mTaskMark);

		} else if (mTaskMark.getTaskStatus() == ATaskMark.HANDLE_OVER
				&& detailInfoTask.getTaskStatus() == ATaskMark.HANDLE_WAIT) {
			doLoadMoreInfo();

		} else if (detailInfoTask.getTaskStatus() == ATaskMark.HANDLE_DOING) {
			serviceWraper.forceTakeoverTask(this, detailInfoTask);
			updateTopFrameStatus(detailInfoTask);

		} else {
			// �����ͼ
			updateTopFrameStatus(mTaskMark);
		}
	}

	@Override
	public void flushView(int what) {
		notifyDataSetChanged();
		super.flushView(what);
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		super.receiveResult(taskMark, exception, trackerResult);

		if (taskMark == mTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER
				&& detailInfoTask.getTaskStatus() == ATaskMark.HANDLE_WAIT) {
			doLoadMoreInfo();

		} else if (taskMark == detailInfoTask && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			flushView(Constants.NONE_VIEW);
			updateTopFrameStatus(detailInfoTask);
		}
	}

	/**
	 * ����һ�������� (�ڲ���������)
	 */
	@Override
	protected BaseAdapter createItemAdapter() {
		return new ApklistAdapter();
	}

	/**
	 * apk�ļ��б� (�ڲ���������)
	 */
	private class ApklistAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return getListItems().size();
		}

		@Override
		public Software getItem(int position) {
			return getListItems().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// ��ǰλ����ͼ
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.apk, parent, false);
				ApkViewHolder viewHolder = new ApkViewHolder();
				// ��ͼ��
				viewHolder.iconView = (ImageView) convertView.findViewById(R.id.appIconView);
				// ����
				viewHolder.nameView = (TextView) convertView.findViewById(R.id.appNameLabel);
				// �汾
				viewHolder.versionView = (TextView) convertView.findViewById(R.id.appVersionLabel);
				// ��С
				viewHolder.sizeView = (TextView) convertView.findViewById(R.id.appSizeLabel);

				// ��Ϣ
				viewHolder.infoView = (TextView) convertView.findViewById(R.id.appInfoLabel);

				convertView.setTag(viewHolder);

			}
			// ��ó�ʼ���õ���ͼ
			Software software = getItem(position);
			if (software != null) {
				initApkView(convertView, software, position);

			}
			return convertView;
		}

		// ��ʼ����ͼͼ
		protected void initApkView(View convertView, Software apk, int position) {
			ApkViewHolder viewHolder = (ApkViewHolder) convertView.getTag();

			// ����
			viewHolder.nameView.setText(apk.getName());
			// �汾
			viewHolder.versionView.setText(getResources().getString(R.string.version_colon) + apk.getVersion());
			// ��С
			viewHolder.sizeView.setText(getResources().getString(R.string.size_colon) + Util.getSizeDes(apk));

			// ״̬, ������ʱ�򲻴����Ա���������
			if (scrolling) {
				// �������Ϣ
				viewHolder.infoView.setText("");

			} else {
				if (apk.getState() == BaseApp.APP_INSTALLING) {
					viewHolder.infoView.setText(R.string.installing);

				} else {
					int state = marketManager.getJointSoftwareState(apk);
					if (state == BaseApp.APP_INSTALLED) {
						viewHolder.infoView.setText(R.string.installed);

					} else {
						viewHolder.infoView.setText("");
					}
				}
			}

			// ͼ��
			Drawable icon = assertCacheManager.getAppIconFromCache(apk.getIconId());
			if (icon == null) {
				viewHolder.iconView.setImageDrawable(marketContext.emptyAppIcon);
			} else {
				viewHolder.iconView.setImageDrawable(icon);
			}
		}

		// ���ܿ���
		private class ApkViewHolder {
			ImageView iconView;
			TextView versionView;
			TextView nameView;
			TextView sizeView;
			TextView infoView;
		}
	}

	@Override
	protected void handleRealItemClick(Object item) {
		final Software software = (Software) item;
		int state = marketManager.getJointSoftwareState(software);
		Dialog dialog = null;
		if (state == BaseApp.APP_INSTALLED) {
			dialog = new AlertDialog.Builder(getContext()).setTitle(software.getName()).setItems(
					R.array.apk_installed_click, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							if (whichButton == 0) {
								Log.d(TAG, "----------software.getApkPath(): " + software.getApkPath());
								marketManager.installSoftware(software.getApkPath());

							} else if (whichButton == 1) {
								marketManager.openSoftware(software.getPackageName());

							} else if (whichButton == 2) {
								handleDeleteApkFile(software);

							} else {
								String pname = software.getPackageName();
								int versionCode = software.getVersionCode();
								AppItem appItem = appCahceManager.getAppItemByPackageVersion(pname, versionCode);
								if (appItem != null) {
									marketContext.handleShowAppDetail(appItem.getId());

								} else {
									marketContext.handleShowAppDetail(pname, versionCode);
								}
							}
							dialog.dismiss();
						}
					}).create();

		} else {
			dialog = new AlertDialog.Builder(getContext()).setTitle(software.getName()).setItems(R.array.apk_click,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							if (whichButton == 0) {
								marketManager.installSoftware(software.getApkPath());

							} else if (whichButton == 1) {
								handleDeleteApkFile(software);

							} else {
								String pname = software.getPackageName();
								int versionCode = software.getVersionCode();
								AppItem appItem = marketContext.getAppCahceManager().getAppItemByPackageVersion(pname,
										versionCode);
								if (appItem != null) {
									marketContext.handleShowAppDetail(appItem.getId());

								} else {
									marketContext.handleShowAppDetail(pname, versionCode);
								}
							}
							dialog.dismiss();
						}
					}).create();
		}

		dialog.show();
	}

	// ɾ�����
	protected void handleDeleteApkFile(Software software) {
	}

	@Override
	protected void handleLoadNewItems(ATaskMark taskMark) {
	}

	// //////////////////// ���󷽷��� /////////////////////////

	/**
	 * �����ϸ��Ϣ��������
	 * 
	 * @return
	 */
	protected abstract ATaskMark getDetailInfoTaskMark();

	/**
	 * ����ͷ��״̬��Ϣ(��ť)
	 * 
	 * @param aTaskMark
	 */
	protected abstract void updateTopFrameStatus(ATaskMark aTaskMark);

	/**
	 * ��Ҫ������ϸ��Ϣ���б�
	 * 
	 * @return
	 */
	protected abstract List<Software> getSummaryInfoList();

	/**
	 * ��û�����
	 * 
	 * @return
	 */
	protected abstract String getCacheMark();

	/**
	 * ��ʾ������
	 * 
	 * @return
	 */
	protected abstract List<Software> getListItems();

}
