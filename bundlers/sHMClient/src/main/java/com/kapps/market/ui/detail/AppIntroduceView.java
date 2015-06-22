package com.kapps.market.ui.detail;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.kapps.market.R;
import com.kapps.market.bean.AppDetail;
import com.kapps.market.bean.AppItem;
import com.kapps.market.ui.CommonView;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

/**
 * @author admin<br>
 *         应锟矫的斤拷锟斤拷
 */
public class AppIntroduceView extends CommonView implements OnClickListener {

	public static final String TAG = "AppIntroduceView";
	// 锟斤拷锟斤拷锟斤拷应锟斤拷
	private AppItem appItem;

	/**
	 * @param context
	 */
	public AppIntroduceView(Context context, AppItem appItem) {
		super(context);

		this.appItem = appItem;

		addView(R.layout.app_introduce);

		if (appItem != null) {
			initIntroduceView();
		}
	}

	// 锟斤拷锟斤拷锟斤拷锟�
	private void initIntroduceView() {
		// 锟斤拷锟街硷拷锟�
		AppDetail appDetail = appItem.getAppDetail();
		TextView infoLabel = (TextView) findViewById(R.id.statLabel);
		//锟斤拷锟斤拷锟斤拷锟斤拷锟
		infoLabel.append(getResources().getString(R.string.download_count_colon) + Util.getDownlaodCount(appDetail) + "\n");
		//锟斤拷锟斤拷时锟斤拷
		infoLabel.append(getResources().getString(R.string.app_auth_time) + appDetail.getAuditingTime());


		ScreenshotBand screenshotFrame = (ScreenshotBand) findViewById(R.id.screenshotFrame);
		screenshotFrame.initScreenshotFrame(appItem, getMessageMark());

		infoLabel = (TextView) findViewById(R.id.introduceLabel);
		infoLabel.setText(appDetail.getDescribe());
		
		// 锟斤拷锟斤拷
		findViewById(R.id.shareAppButton).setOnClickListener(this);
		findViewById(R.id.permssionButton).setOnClickListener(this);
		findViewById(R.id.reportButton).setOnClickListener(this);
		findViewById(R.id.otherVerAppButton).setOnClickListener(this);
	}

	@Override
	public int getMessageMark() {
		if (appItem != null) {
			return (appItem.getPackageName() + appItem.getVersionCode()).hashCode();
		} else {
			return 0;
		}
	}

	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		if (viewId == R.id.shareAppButton) {
			doShareApp();

		} else if (viewId == R.id.permssionButton) {
			Message message = Message.obtain();
			message.what = Constants.M_PERMISSION_SHOW_VIEW;
			notifyMessageToParent(message);

		} else if (viewId == R.id.reportButton) {
			Message message = Message.obtain();
			message.what = Constants.M_BADNESS_SHOW_VIEW;
			notifyMessageToParent(message);
		
		} else if (v.getId() == R.id.otherVerAppButton) {
			Message message = Message.obtain();
			message.what = Constants.M_SHOW_APP_OTHER_VERSION;
			notifyMessageToParent(message);

		} 
		
	}

	// 锟斤拷锟酵癸拷锟斤拷
	private void doShareApp() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string.share_app),
				appItem.getName(), appItem.getPackageName(), appItem.getVersionCode()));
		intent = Intent.createChooser(intent, getResources().getString(R.string.share_info_tof));
		try {
			getContext().startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getContext(), getResources().getString(R.string.can_not_share), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void flushView(int what) {
		ScreenshotBand screenshotFrame = (ScreenshotBand) findViewById(R.id.screenshotFrame);
		screenshotFrame.flushScreenshot();
	}

}
