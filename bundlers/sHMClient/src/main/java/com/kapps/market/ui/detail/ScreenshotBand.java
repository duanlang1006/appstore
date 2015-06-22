package com.kapps.market.ui.detail;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kapps.market.MApplication;
import com.kapps.market.R;
import com.kapps.market.TaskMarkPool;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.MImageType;
import com.kapps.market.cache.AssertCacheManager;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppImageTaskMark;
import com.kapps.market.task.mark.MultipleTaskMark;
import com.kapps.market.ui.ScreenPieceView;
import com.kapps.market.util.Constants;

/**
 * 2010-8-9<br>
 * �����ͼСͼ���
 * 
 * @author admin
 * 
 */
public class ScreenshotBand extends FrameLayout implements IResultReceiver, OnClickListener {

	public static final String TAG = "ScreenshotFrame";

	private MApplication marketContext = MApplication.getInstance();
	private AssertCacheManager assertCacheManager = marketContext.getAssertCacheManager();
	private TaskMarkPool taskMarkPool = marketContext.getTaskMarkPool();
	// ������Ϣͨ��
	private int messageMark = -1;

	public static final int SHOT_FRAME_ID = 1111;
	public static final int MANUAL_LOAD_ID = 2222;
	public static final int SCREEN_PIECE_ID = 1111;
	public static final int PIECE_INDEX = -49;
	private boolean manualLoad;

	private AppItem appItem;

	/**
	 * @param context
	 */
	public ScreenshotBand(Context context) {
		super(context);

	}

	public ScreenshotBand(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * ��ʼ�������ͼframe
	 * 
	 * @param appItem
	 * @param messageMark
	 *            ��Ϣ�ı�ʾ�� ������Ϣͨ��
	 */
	public void initScreenshotFrame(AppItem appItem, int messageMark) {
		this.appItem = appItem;
		this.messageMark = messageMark;

		// ��ͼ��
		String[] screentshotUrls = appItem.getAppDetail().getScreenshots();
		if (screentshotUrls != null && screentshotUrls.length > 0) {
			LinearLayout shotFrame = new LinearLayout(getContext());
			shotFrame.setId(SHOT_FRAME_ID);
			shotFrame.setGravity(Gravity.CENTER);
			Screenshot screenshot = null;
			LinearLayout.LayoutParams layoutParam = null;
			MultipleTaskMark mTaskMark = new MultipleTaskMark();
			for (String shotUrl : screentshotUrls) {
				screenshot = new Screenshot(getContext());
				screenshot.setId(SCREEN_PIECE_ID);
				screenshot.setOnClickListener(this);
				layoutParam = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(
						R.dimen.screenshot_width), getResources().getDimensionPixelSize(R.dimen.screenshot_height));
				layoutParam.rightMargin = getResources().getDimensionPixelSize(R.dimen.screenshot_right_margin);
				screenshot.setLayoutParams(layoutParam);
				screenshot.setTag(shotUrl);
				shotFrame.addView(screenshot);

				// ����ͼƬ, ��Ϊ�����ֶ��������б��뿼�Ǳ��ػ��棬�ظ�ʹ��֮ǰ���صĽ�ͼ��
				Drawable drawable = assertCacheManager.getScreenshotsFromCache(shotUrl, true, true);
				if (drawable == null) {
					screenshot.setImageDrawable(marketContext.emptyScreenshot);
					mTaskMark.addSubTaskMark(taskMarkPool.createAppImageTaskMark(appItem.getId(), shotUrl,
							MImageType.APP_SCREENSHOT));
					if (marketContext.getMarketConfig().isLoadAppScreenshot()) {
						screenshot.showProgressView();
					}

				} else {
					screenshot.setImageDrawable(drawable);
				}
			}

			FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT);
			frameLayoutParams.gravity = Gravity.CENTER;
			shotFrame.setLayoutParams(frameLayoutParams);
			addView(shotFrame);

			// ���ؽ�ͼ
			if (mTaskMark.getTaskMarkList().size() > 0) {
				if (marketContext.getMarketConfig().isLoadAppScreenshot()) {
					marketContext.getServiceWraper().scheduleAppImageResourceTask(this, mTaskMark, null);
					changeBigView();

				} else {
					shotFrame.setVisibility(INVISIBLE);
					TextView noteLabel = new TextView(getContext());
					noteLabel.setId(MANUAL_LOAD_ID);
					noteLabel.setOnClickListener(this);
					noteLabel.setPadding(3, 0, 0, 0);
					noteLabel.setTextColor(getResources().getColor(R.color.app_introduce));
					noteLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
							getResources().getDimensionPixelSize(R.dimen.app_info_content_font_size));
					noteLabel.setText(String.format(getResources().getString(R.string.manual_load_screenshot_note),
							screentshotUrls.length));
					layoutParam = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT);
					noteLabel.setLayoutParams(layoutParam);
					addView(noteLabel);
				}

			} else {
				// ������еĽ�ͼ���Ǵ��ڵ���ôֱ����ʾ������manualLoadΪtrue�Ա����ˢ�¡�
				manualLoad = true;
				changeBigView();
			}

		} else {
			TextView noneViewLabel = new TextView(getContext());
			noneViewLabel.setText(getResources().getString(R.string.none_screenshot_note));
			noneViewLabel.setPadding(3, 0, 0, 0);
			noneViewLabel.setTextColor(getResources().getColor(R.color.app_introduce));
			noneViewLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimensionPixelSize(R.dimen.app_info_content_font_size));
			LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT);
			noneViewLabel.setLayoutParams(layoutParam);
			addView(noneViewLabel);
		}
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		if (taskMark instanceof AppImageTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			AppImageTaskMark appImageTaskMark = (AppImageTaskMark) taskMark;
			updateScreenShowView(appImageTaskMark.getUrl());
		}
	}

	// ����
	private void updateScreenShowView(String url) {
		Screenshot screenshotPicec = null;
		ViewGroup shotFrame = (ViewGroup) findViewById(SHOT_FRAME_ID);
		int count = shotFrame.getChildCount();
		for (int index = 0; index < count; index++) {
			screenshotPicec = (Screenshot) shotFrame.getChildAt(index);
			if (screenshotPicec.getTag().equals(url)) {
				screenshotPicec.hideProgressView();
				screenshotPicec.setImageDrawable(assertCacheManager.getScreenshotsFromCache(url, true, true));
				break;
			}
		}
	}

	// ˢ�½�ͼ
	public void flushScreenshot() {
		marketContext.getServiceWraper().forceTakeoverImageScheduleTask(this);
		ViewGroup screenshotFrame = (ViewGroup) findViewById(SHOT_FRAME_ID);
		if (screenshotFrame != null) {
			// �ֶ������Զ�����
			if (manualLoad || marketContext.getMarketConfig().isLoadAppScreenshot()) {
				Screenshot screenshotPiece = null;
				MultipleTaskMark mTaskMark = new MultipleTaskMark();
				String url = null;
				Drawable drawable = null;
				int count = screenshotFrame.getChildCount();
				for (int index = 0; index < count; index++) {
					screenshotPiece = (Screenshot) screenshotFrame.getChildAt(index);
					if (screenshotPiece.getDrawable() == marketContext.emptyScreenshot) {
						url = screenshotPiece.getTag().toString();
						drawable = assertCacheManager.getScreenshotsFromCache(screenshotPiece.getTag().toString(),
								true, true);
						if (drawable != null) {
							screenshotPiece.hideProgressView();
							screenshotPiece.setImageDrawable(drawable);

						} else {
							mTaskMark.addSubTaskMark(taskMarkPool.createAppImageTaskMark(appItem.getId(), url,
									MImageType.APP_SCREENSHOT));
							// ��Ϊ״̬һ���ͳ�ʼ���ˣ���������ֻ�����ֶ����ص����
							if (manualLoad) {
								screenshotPiece.showProgressView();
							}
						}
					}
				}

				if (mTaskMark.getTaskMarkList().size() > 0) {
					marketContext.getServiceWraper().scheduleAppImageResourceTask(this, mTaskMark, null);
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == SCREEN_PIECE_ID) {
			// �������
			LinearLayout linearLayout = (LinearLayout) findViewById(SHOT_FRAME_ID);
			int index = linearLayout.indexOfChild(v);
			Message message = Message.obtain();
			message.what = Constants.M_SCREEN_SHOTS_SHOW;
			message.arg1 = messageMark;
			message.obj = index;
			marketContext.handleMarketMessage(message);

			// ����Ϊ����ͼ�Ա������ȫͼʱ�����ڴ濪��
			changeToEmptyScreenShot();
			marketContext.handleMarketEmptyMessage(Constants.M_DELAY_GC);

		} else if (v.getId() == MANUAL_LOAD_ID) {
			// �Ƴ��ֶ����صİ�ť
			manualLoad = true;
			removeView(v);
			findViewById(SHOT_FRAME_ID).setVisibility(VISIBLE);
			changeBigView();
			flushScreenshot();
		}
	}

	// ����Ϊ����ͼ�Ա������ȫͼʱ�����ڴ濪��
	private void changeToEmptyScreenShot() {
		ViewGroup screenshotFrame = (ViewGroup) findViewById(SHOT_FRAME_ID);
		if (screenshotFrame != null) {
			Screenshot screenshot = null;
			int count = screenshotFrame.getChildCount();
			for (int index = 0; index < count; index++) {
				screenshot = (Screenshot) screenshotFrame.getChildAt(index);
				if (screenshot.getDrawable() != marketContext.emptyScreenshot) {
					Drawable drawable = screenshot.getDrawable();
					if (drawable instanceof BitmapDrawable) {
						((BitmapDrawable) drawable).getBitmap().recycle();
					}
					screenshot.setImageDrawable(marketContext.emptyScreenshot);
				}
			}
		}
	}

	// ����ͼ
	private void changeBigView() {
		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		layoutParams.height = getResources().getDimensionPixelSize(R.dimen.screenshot_frame_big_height);
		requestLayout();
	}

	// ��ͼСƬ
	private class Screenshot extends FrameLayout {

		private ScreenPieceView imageView;
		private View progressView;

		/**
		 * @param context
		 */
		public Screenshot(Context context) {
			super(context);
			imageView = new ScreenPieceView(context);
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT);
			imageView.setLayoutParams(layoutParams);
			addView(imageView);

			layoutParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER;
			progressView = LayoutInflater.from(context).inflate(R.layout.b_progress, null);
			layoutParams.topMargin = 10;
			progressView.setVisibility(GONE);
			progressView.setLayoutParams(layoutParams);
			addView(progressView);
		}

		public void showProgressView() {
			progressView.setVisibility(VISIBLE);
		}

		public void hideProgressView() {
			progressView.setVisibility(GONE);
		}

		public void setImageDrawable(Drawable drawable) {
			imageView.setImageDrawable(drawable);
		}

		public Drawable getDrawable() {
			return imageView.getDrawable();
		}

	}
}
