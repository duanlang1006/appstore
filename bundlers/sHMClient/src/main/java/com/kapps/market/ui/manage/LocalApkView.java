package com.kapps.market.ui.manage;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kapps.market.R;
import com.kapps.market.bean.Software;
import com.kapps.market.bean.config.MarketConfig;
import com.kapps.market.cache.CacheConstants;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.ui.DirsChooser;

/**
 * ����apk<br>
 * 2011-3-21<br>
 * 
 * @author admin
 * 
 */
public class LocalApkView extends BaseApkView implements OnClickListener {

	public static final String TAG = "LocalApkView";

	// ˢ�°�ť��ID��
	public static final int FLUSH_APK_ID = 494949;

	// ˢ�¹�����ͼ
	private TextView flushApkView;

	/**
	 * ���췽��
	 * 
	 * @param context
	 */
	public LocalApkView(Context context) {
		super(context);
	}

	@Override
	protected String getCacheMark() {
		return CacheConstants.LOCAK_APK_INFO;
	}

	@Override
	protected ATaskMark getDetailInfoTaskMark() {
		return taskMarkPool.getLocalApkDetailInfoTaskMark();
	}

	@Override
	protected List<Software> getListItems() {
		return marketManager.getLocalApkList();
	}

	@Override
	protected List<Software> getSummaryInfoList() {
		return marketManager.getLocalApkList();
	}

	@Override
	protected void addListHeader(ListView listView) {
		flushApkView = new TextView(getContext());
		flushApkView.setId(FLUSH_APK_ID);
		flushApkView.setGravity(Gravity.CENTER);
		flushApkView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.refresh), null,
				null, null);
		flushApkView.setTextSize(getResources().getDimensionPixelOffset(R.dimen.fun_btn_font_size));
		flushApkView.setText(R.string.load_wait);
		flushApkView.setOnClickListener(this);

		LinearLayout linearLayout = new LinearLayout(getContext());
		linearLayout.addView(flushApkView);
		linearLayout.setGravity(Gravity.CENTER);
		AbsListView.LayoutParams absLayoutParams = new AbsListView.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		linearLayout.setLayoutParams(absLayoutParams);
		listView.addHeaderView(linearLayout);
	}

	@Override
	protected View createTopFrame(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.common_view_title, null);
		Button button = (Button) view.findViewById(R.id.titleFunButton);
		button.setText(R.string.local_apk_dir);
		button.setOnClickListener(this);

		return view;
	}

	@Override
	protected void updateTopFrameStatus(ATaskMark aTaskMark) {
		if (aTaskMark == mTaskMark && aTaskMark.getTaskStatus() == ATaskMark.HANDLE_DOING) {
			doUpdateTopFrame(R.string.wait_for_progress, false);

		} else if (aTaskMark == getDetailInfoTaskMark() && aTaskMark.getTaskStatus() == ATaskMark.HANDLE_DOING) {
			doUpdateTopFrame(R.string.load_apk_more_info, false);

		} else {
			doUpdateTopFrame(R.string.local_apk_dir, true);
		}
	}

	// ����ʵ�ʸ���
	private void doUpdateTopFrame(int noteRes, boolean enabled) {
		Button button = (Button) findViewById(R.id.titleFunButton);
		button.setText(noteRes);
		button.setEnabled(enabled);
		flushApkView.setEnabled(enabled);
	}

	@Override
	protected void tryQueryNewItems() {
		if (mTaskMark.getTaskStatus() == ATaskMark.HANDLE_OVER
				&& detailInfoTask.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			// ����apk��ͼ�����û�ˢ�����
			updateTopFrameStatus(mTaskMark);

		} else {
			super.tryQueryNewItems();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == FLUSH_APK_ID) {
			handleManualLoadItem();

		} else if (v.getId() == R.id.titleFunButton) {
			chooseLocalApkDir();

		}
	}

	@Override
	protected void handleManualLoadItem() {
		// ��ʼ��apk�б�
		boolean ok = marketContext.getMarketManager().checkSDCardStateAndNote();
		if (ok) {
			List<Software> oldApkList = marketManager.getLocalApkList();
			serviceWraper.initLocalApkSummaryInfoList(this, mTaskMark, oldApkList);
			// ��վ����
			marketManager.setLocalApkList(new ArrayList<Software>());
			detailInfoTask.setTaskStatus(ATaskMark.HANDLE_WAIT);

			updateTopFrameStatus(mTaskMark);
			notifyDataSetChanged();

		}
	}

	// ɾ�����
	@Override
	protected void handleDeleteApkFile(Software software) {
		marketManager.deleteLocalApk(software);
		marketManager.deleteApkFile(software.getApkPath());
		notifyDataSetChanged();
	}

	/**
	 * APK���Ŀ¼��ť����ݰ�װ�Ի���
	 */
	private void chooseLocalApkDir() {
		final DirsChooser dirsChooser = new DirsChooser(getContext());
		final AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle(
				getResources().getString(R.string.local_apk_dir)).setView(dirsChooser).setOnKeyListener(
				new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK && (event.getAction() == KeyEvent.ACTION_UP)) {
							boolean ok = dirsChooser.showPrefixPath();
							if (!ok) {
								dialog.dismiss();
							}
							return true;
						} else {
							return false;
						}
					}
				}).setCancelable(false).create();

		dirsChooser.findViewById(R.id.okButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String choosePath = dirsChooser.getCurrentDirNoneRoot();
				if (choosePath != null) {
					SharedPreferences configPreferences = marketContext.getSharedPrefManager().getMarketConfigPref();
					Editor editor = configPreferences.edit();
					editor.putString(MarketConfig.LOCAK_APK_DIR, choosePath);
					editor.commit();

					MarketConfig marketConfig = marketContext.getMarketConfig();
					marketConfig.setLocalApkDir(choosePath);

					handleManualLoadItem(); // ����͵����ǵ���ˢ�°�ť

					dialog.dismiss();
				}
			}
		});

		dirsChooser.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();

	}

}
