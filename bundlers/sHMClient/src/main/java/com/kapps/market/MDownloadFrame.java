package com.kapps.market;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.kapps.market.log.LogUtil;
import com.kapps.market.ui.CommonView;
import com.kapps.market.ui.manage.TaskManageView;
import com.kapps.market.util.Constants;

/**
 * 2010-8-11 <br>
 * Top-right menu-->Download manager.
 * 
 * @author Administrator
 * 
 */
public class MDownloadFrame extends MarketActivity {

	public static final String TAG = "AppDownloadFrame";

	private CommonView commonView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m_quick_page);


		doShowDownloadTaskView();
//		setHandleAllMessage(true);
	}

//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		menu.findItem(R.id.logoutMenuItem).setVisible(false);
//		menu.findItem(R.id.closeMenuItem).setVisible(false);
//		return super.onPrepareOptionsMenu(menu);
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);    
    }
	
	// 锟斤拷示锟斤拷锟斤拷锟斤拷图
	private void doShowDownloadTaskView() {
		TaskManageView taskView = new TaskManageView(this);

//		// title
//		TextView textView = (TextView) findViewById(R.id.quickViewTitle);
//		textView.setText(getString(R.string.app_download_table));

		// 锟斤拷锟斤拷锟斤拷锟�
		FrameLayout contentFrame = (FrameLayout) findViewById(R.id.contentFrame);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		contentFrame.addView(taskView, layoutParams);

		commonView = taskView;
	}

	@Override
	public void subHandleMessage(Message msg) {
		LogUtil.d(TAG, "msg: " + msg);

		switch (msg.what) {
		case Constants.M_UNINSTALL_APK:
		case Constants.M_INSTALL_APK:
		case Constants.M_DOWNLOAD_PROGRESS:
		case Constants.M_DOWNLOAD_CANCEL:
		case Constants.M_DOWNLOAD_COMPLETED:
		case Constants.M_DOWNLOAD_FAIL:
		case Constants.M_DOWNLOAD_STOP:
		case Constants.M_QUICK_DOWNLOAD_APP:
		case Constants.M_QUICK_PAYMENT_APP:
		case Constants.M_DOWNLOAD_RETRY:
		case Constants.M_BATCH_DOWNLOAD_APP_OK:
			if (commonView != null) {
				commonView.flushView(msg.what);
			}
			break;
		}
	}

	@Override
	protected void initCustomActionbar() {
		ActionBar ab = getSupportActionBar();
		ab.setTitle(R.string.app_download_table);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(false);
	}
}
