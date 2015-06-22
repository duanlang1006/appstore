package com.kapps.market;

import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.kapps.market.log.LogUtil;
import com.kapps.market.ui.CommonView;
import com.kapps.market.ui.app.UpdateableAppList;
import com.kapps.market.util.Constants;

/**
 * 2010-8-11 <br>
 * 
 * @author admin
 * 
 */
public class UpdateableFrame extends MarketActivity {

	public static final String TAG = "SoftUpdateFrame";

	private CommonView commonAppView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m_quick_page);

		doShowSoftwareView();

//		setHandleAllMessage(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.logoutMenuItem).setVisible(false);
		menu.findItem(R.id.closeMenuItem).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}

	// ���Ը��µ����������ͼ
	private void doShowSoftwareView() {
		UpdateableAppList listView = new UpdateableAppList(this);
		// ��������б���Ҫ����ȡ�ı������������������ĸ���
		listView.initLoadleList(taskMarkPool.getSoftwareUpdateTaskMark(false));
		// title
//		TextView textView = (TextView) findViewById(R.id.quickViewTitle);
//		textView.setText(getString(R.string.upateable_software_list));

		// �������
		FrameLayout contentFrame = (FrameLayout) findViewById(R.id.contentFrame);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		contentFrame.addView(listView, layoutParams);

		commonAppView = listView;
	}

	/**
	 * ���Ǹ�����������������д����Ҫ���յ�����Ϣ
	 * 
	 */
	@Override
	public void subHandleMessage(Message msg) {
		LogUtil.d(TAG, "handleMessage: " + msg);

		switch (msg.what) {
		// ע�������ʵ�֣�����������װ��ж�ػ�Ҫ����taskview
		// ������صľ�ֻҪ����SOFT_MANANGE_VIEW �� TASK_LIST_VIEW
		case Constants.M_UNINSTALL_APK:
		case Constants.M_INSTALL_APK:
		case Constants.M_DOWNLOAD_CANCEL:
		case Constants.M_DOWNLOAD_ACCEPT:
		case Constants.M_DOWNLOAD_COMPLETED:
		case Constants.M_DOWNLOAD_FAIL:
		case Constants.M_DOWNLOAD_STOP:
		case Constants.M_QUICK_DOWNLOAD_APP:
		case Constants.M_QUICK_PAYMENT_APP:
		case Constants.M_DOWNLOAD_RETRY:
		case Constants.M_BATCH_DOWNLOAD_APP_OK:
			if (commonAppView != null) {
				commonAppView.flushView(msg.what);
			}
			break;

		case Constants.M_BATCH_UPDATE_APP_OK:
		case Constants.M_BATCH_UPDATE_APP_LOCAL:
			finish();
			break;
		}

	}

	@Override
	protected void initCustomActionbar() {
		// TODO Auto-generated method stub
		
	}

}
