package com.kapps.market.ui.detail;

import java.util.List;

import com.kapps.market.R;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.AppPermission;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.ui.CommonView;
import com.kapps.market.util.Constants;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Ӧ�õ�Ȩ��
 * 
 * @author admin
 * 
 */
public class AppPermssionView extends CommonView implements IResultReceiver, OnClickListener {

	public static final String TAG = "AppPermssionView";
	// ������Ӧ��
	private AppItem appItem;

	private ATaskMark taskMark;

	/**
	 * @param context
	 */
	public AppPermssionView(Context context, AppItem appItem) {
		super(context);

		setBackgroundColor(getResources().getColor(R.color.content_bg_color));
		this.appItem = appItem;

		// ���Ȩ���б�
		taskMark = marketContext.getTaskMarkPool().createAppPermissionTaskMark(appItem.getId());
		if (appItem.getPermissionList().size() == 0) {
			addView(R.layout.progressbar_view_l_r);
			marketContext.getServiceWraper().getAppPermissionList(this, taskMark, null, appItem.getId());
		} else {
			handleInitShowPermissionList();
		}
	}

	// ��ʼ��Ȩ���б�
	private void handleInitShowPermissionList() {
		// �б���ͼ
		removeAllViews();
		addView(R.layout.app_permission);

		// ��ʼ��Ȩ���б�
		LinearLayout showPermissionList = (LinearLayout) findViewById(R.id.showPermissionList);
		LinearLayout hidePermissionList = (LinearLayout) findViewById(R.id.hidePermissionList);
		List<AppPermission> permList = appItem.getPermissionList();
		View permissionView = null;
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		for (AppPermission appPermission : permList) {
			if (appPermission.isHide()) {
				permissionView = layoutInflater.inflate(R.layout.permision_less_item, null);
				hidePermissionList.addView(permissionView);
			} else {
				permissionView = layoutInflater.inflate(R.layout.permision_item, null);
				showPermissionList.addView(permissionView);
			}
			((TextView) permissionView.findViewById(R.id.permissionTitleLabel)).setText(appPermission.getTitle());
			((TextView) permissionView.findViewById(R.id.permissionDesLabel)).setText(appPermission.getDes());
		}

		// ��ʾ���ص�Ȩ��
		View view = findViewById(R.id.showHidePermissionView);
		if (hidePermissionList.getChildCount() == 0) {
			view.setVisibility(View.GONE);
			hidePermissionList.setVisibility(View.GONE);
		} else {
			view.setOnClickListener(this);
			// ��ʼʱ�۵���
			view.setTag(false);
		}

	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		// ��ʼ��Ȩ��
		if (taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			handleInitShowPermissionList();

		} else {
			Toast.makeText(getContext(), getResources().getString(R.string.load_permission_fail), 150).show();
			handleBack();
		}
	}

	// ����
	private void handleBack() {
		Message message = Message.obtain();
		message.what = Constants.M_PERMISSION_BACk;
		message.obj = appItem;
		notifyMessageToParent(message);
	}

	@Override
	public void releaseView() {
		// ������
		serviceWraper.forceDiscardReceiveTask(taskMark);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.showHidePermissionView:
			boolean expend = (Boolean) v.getTag();
			ImageView imageView = (ImageView) findViewById(R.id.expendableImageView);
			if (expend) {
				v.setTag(false);
				imageView.setImageResource(R.drawable.expend_s_1);
				findViewById(R.id.hidePermissionList).setVisibility(View.GONE);

			} else {
				v.setTag(true);
				imageView.setImageResource(R.drawable.expend_s_2);
				findViewById(R.id.hidePermissionList).setVisibility(View.VISIBLE);
			}
			break;
		}
	}

}
