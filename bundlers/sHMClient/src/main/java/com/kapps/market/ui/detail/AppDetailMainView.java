package com.kapps.market.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.kapps.market.AppDetailFrame;
import com.kapps.market.MApplication;
import com.kapps.market.MarketManager;
import com.kapps.market.R;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.cache.AppCahceManager;
import com.kapps.market.task.mark.AppFavorTaskMark;
import com.kapps.market.ui.TabableAppView;
import com.kapps.market.util.Constants;

/**
 * 应锟斤拷锟斤拷锟揭筹拷锟�锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟绞癸拷谩锟�
 * 
 * @author admin
 * 
 */
public class AppDetailMainView extends TabableAppView {

	public static final String TAG = "AppDetailMainView";
	// 锟斤拷锟斤拷锟斤拷锟�
	public static final int APP_INTRODUCE_VIEW = 0;
	public static final int APP_COMMENT_VIEW = 1;
	public static final int APP_DEVELPER_VIEW = 2;
	protected static final int ANIMOTION_PIC = 1;

	private Button appIntroduceButton, appCommentButton, develperButton;

	// 锟斤拷示锟斤拷锟斤拷锟�
	private AppItem appItem;
	// 锟斤拷前模式
	private boolean isAuthorMode;
	private boolean isHistoryAppMode;
	private MarketManager marketManager;
	private int ONE_TAB_WIDTH = 0;
	private ImageView mTabIndicator;

	/**
	 * @param context
	 */
	public AppDetailMainView(Context context, AppItem appItem, boolean isAuthorMode, boolean isHistoryAppMode) {
		super(context);

		marketManager = marketContext.getMarketManager();
		this.isAuthorMode = isAuthorMode;
		this.isHistoryAppMode = isHistoryAppMode;

		addView(R.layout.detail_main_view);
		
       
		this.appItem = appItem;
		if (appItem != null) {
			mTabIndicator = (ImageView) findViewById(R.id.animation_pic);
			android.view.ViewGroup.LayoutParams params = mTabIndicator.getLayoutParams();
			Display display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
	        int screenW = display.getWidth();
	        ONE_TAB_WIDTH = screenW / 3;
			params.width = ONE_TAB_WIDTH;  
			mTabIndicator.setLayoutParams(params);  
			
			registerView(APP_INTRODUCE_VIEW);
			appIntroduceButton = (Button) findViewById(R.id.introduceButton);
			registerTrigger(appIntroduceButton);

			appCommentButton = (Button) findViewById(R.id.commentButton);
			registerView(APP_COMMENT_VIEW);
			registerTrigger(appCommentButton);

			// 锟介看锟斤拷锟竭碉拷应锟矫碉拷时锟斤拷锟斤拷锟斤拷页锟斤拷乇锟�
			develperButton = (Button) findViewById(R.id.develperButton);
			if (!isAuthorMode && !isHistoryAppMode) {
				registerView(APP_DEVELPER_VIEW);
				registerTrigger(develperButton);

			} else {
				develperButton.setVisibility(View.GONE);
				// TODO 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟绞�
				appCommentButton.setBackgroundResource(R.drawable.right_tab_btn);
				appCommentButton.setTextColor(getContext().getResources().getColor(R.color.band_common_content));
				appCommentButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(
						R.dimen.top_type_button_font_szie));
				appCommentButton.setFocusable(false);
			}
			if (appItem.getAppDetail().getCommentCount() > 0) {
				appCommentButton.setText(getContext().getResources().getString(R.string.watch_comment) + 
						"("+appItem.getAppDetail().getCommentCount()+")");
			}

			// 锟斤拷始锟斤拷锟斤拷图状态
			updateFunBandState();

			// 锟斤拷锟斤拷锟斤拷示锟斤拷锟斤拷锟斤拷锟�
			showChoosedView(APP_INTRODUCE_VIEW);
		}

//		new Thread(){
//        	public void run() {
//        		Message msg = new Message();
//				msg.what = ANIMOTION_PIC;
//				hander.sendMessage(msg);
//        	};
//        }.start();
	}

	// 锟斤拷始状态
	public void updateFunBandState() {
		// 锟斤拷锟杰帮拷钮
		
		Button favorButton = (Button)findViewById(R.id.favorFunButton);
		AppCahceManager appCahceManager = new AppCahceManager();

		AppFavorTaskMark favorMark = taskMarkPool.getAppFavorTaskMark();
		boolean exist = appCahceManager.isAppItemInCache(favorMark, appItem);
		if (exist) {
			favorButton.setText(R.string.favoliten);
		}
		
		favorButton.setOnClickListener(this);
		Button openButton = (Button) findViewById(R.id.openFunButton);
		Button merButton = (Button) findViewById(R.id.merFunButton);
		merButton.setOnClickListener(this);
		// 锟斤拷锟斤拷锟斤拷锟斤拷应锟侥憋拷锟斤拷锟斤拷息
		int state = marketManager.getJointSoftwareState(appItem);
		if (state == BaseApp.APP_INSTALLED) {
			merButton.setEnabled(true);
			openButton.setVisibility(View.VISIBLE);
			openButton.setOnClickListener(this);
			merButton.setText(getResources().getString(R.string.uninstall));

		} else if (state == BaseApp.APP_DOWNLOADED) {
			merButton.setEnabled(true);
			merButton.setText(getResources().getString(R.string.install));
			openButton.setOnClickListener(null);
			openButton.setVisibility(View.GONE);

		} else if (state == BaseApp.APP_DOWNLOADING) {
			merButton.setEnabled(false);
			merButton.setText(getResources().getString(R.string.downloading));
			openButton.setOnClickListener(null);
			openButton.setVisibility(View.GONE);

		} else if (state == BaseApp.APP_INSTALLING) {
			merButton.setEnabled(false);
			merButton.setText(getResources().getString(R.string.installing));
			openButton.setOnClickListener(null);
			openButton.setVisibility(View.GONE);

		} else {
			merButton.setEnabled(true);
			openButton.setOnClickListener(null);
			openButton.setVisibility(View.GONE);
			if (marketManager.isAppUpdateInfo(appItem)) {
				merButton.setText(getResources().getString(R.string.update));

			} else {
				merButton.setText(getResources().getString(R.string.download));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.ui.TabableAppView#onClick(android.view.View)
	 */
	@Override
	public void onClick(View trigger) {
		switch (trigger.getId()) {
		case R.id.favorFunButton:
			Message addFavorMsg = Message.obtain();
			addFavorMsg.what = Constants.M_FAVOR_ADDED;
			addFavorMsg.arg1 = getMessageMark();
			addFavorMsg.obj = appItem;
			marketContext.handleMarketMessage(addFavorMsg);
			break;

		case R.id.openFunButton:
			handleOpenFunction();
			break;

		case R.id.merFunButton:
			handleMerFunction();
			break;

		default:
			super.onClick(trigger);
		}
	}

	@Override
	public int getMessageMark() {
		if (appItem != null) {
			return (appItem.getPackageName() + appItem.getVersionCode()).hashCode();
		} else {
			return 0;
		}
	}

	private void handleOpenFunction() {
		marketManager.openSoftware(appItem.getPackageName());

	}

	// 锟斤拷锟斤拷喙︼拷馨锟脚�
	private void handleMerFunction() {
		// 锟斤拷锟阶刺�
		int state = marketManager.getJointSoftwareState(appItem);

		switch (state) {
		case BaseApp.APP_INSTALLED:
			marketManager.uninstallSoftware(appItem.getPackageName());
			break;

		case BaseApp.APP_NEW:
		case BaseApp.APP_DOWNLOAD_STOP:
			View button = findViewById(R.id.merFunButton);
			button.setEnabled(false);
			boolean ok = marketManager.checkSDCardStateAndNote();
			if (ok) {
				Message message = Message.obtain();
				message.what = Constants.M_DOWNLOAD_ACCEPT;
				message.obj = appItem;
				message.arg1 = getMessageMark();
				marketContext.handleMarketMessage(message);
			}
			button.setEnabled(true);
			break;

		case BaseApp.APP_DOWNLOADED:
			String apkPath = marketManager.getJointApkSavePath(appItem.getPackageName(), appItem.getVersionCode());
			if (apkPath != null) {
				marketManager.installSoftware(apkPath);
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void onBeforeShowView(int viewMark, Object data) {
		int oldMark = getCurrentTabMark();
		
		Animation animation = null;
		animation = new TranslateAnimation(ONE_TAB_WIDTH*oldMark, ONE_TAB_WIDTH*viewMark, 0, 0);

		animation.setFillAfter(true);
		animation.setDuration(100);
		mTabIndicator.startAnimation(animation);
		
		setButtonSelected(oldMark, false);
		setButtonSelected(viewMark, true);
	}

	private void setButtonSelected(int mark, boolean selected) {
		switch (mark) {
		case APP_INTRODUCE_VIEW:
			appIntroduceButton.setSelected(selected);
			break;

		case APP_COMMENT_VIEW:
			appCommentButton.setSelected(selected);
			break;

		case APP_DEVELPER_VIEW:
			develperButton.setSelected(selected);
			break;
		}


	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.TabableActivity#createContentView(int)
	 */
	@Override
	protected View createContentView(int viewMark) {
		switch (viewMark) {
		case APP_INTRODUCE_VIEW:
			// 锟斤拷锟斤拷锟劫憋拷
			AppIntroduceView introduceView = new AppIntroduceView(getContext(), appItem);
			return introduceView;

		case APP_COMMENT_VIEW:
			// 锟斤拷锟斤拷
			AppCommentView commentView = new AppCommentView(getContext(), appItem);
			commentView.initLoadleList(marketContext.getTaskMarkPool().getCommentsMark(appItem.getId()));
			return commentView;

		case APP_DEVELPER_VIEW:
			// 锟斤拷锟斤拷锟斤拷锟斤拷锟�
			AppDeveloperView develperView = new AppDeveloperView(getContext(), appItem);
			String author = appItem.getAuthorName();
			author = (author == null || author.trim().length() == 0) ? Constants.NONE_AUTHOR : appItem.getAuthorName();
			develperView.initLoadleList(marketContext.getTaskMarkPool().getAuthorAppMark(author));
			return develperView;

		default:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ck.market.ui.CommonAppView#handleChainMessage(android.os.Message)
	 */
	@Override
	public void handleChainMessage(Message message) {
		int what = message.what;
		switch (what) {

		case Constants.M_INSTALL_APK:
		case Constants.M_UNINSTALL_APK:
		case Constants.M_DOWNLOAD_COMPLETED:
		case Constants.M_DOWNLOAD_STOP:
		case Constants.M_DOWNLOAD_FAIL:
			updateFunBandState();
			break;

		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.TabableActivity#getShowViewMark(android.view.View)
	 */
	@Override
	protected int getShowViewMark(View trigger) {
		switch (trigger.getId()) {
		case R.id.introduceButton:
			return APP_INTRODUCE_VIEW;

		case R.id.commentButton:
			return APP_COMMENT_VIEW;

		case R.id.develperButton:
			return APP_DEVELPER_VIEW;

		default:
			return Constants.NONE_VIEW;
		}
	}
}
