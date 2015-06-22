package com.kapps.market.ui;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.kapps.market.AppDetailFrame;
import com.kapps.market.MApplication;
import com.kapps.market.R;
import com.kapps.market.TaskMarkPool;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.MImageType;
import com.kapps.market.cache.AppCahceManager;
import com.kapps.market.cache.AssertCacheManager;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.MarketServiceWraper;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppAdvertiseTaskMark;
import com.kapps.market.task.mark.AppImageTaskMark;
import com.kapps.market.task.mark.MultipleTaskMark;
import com.kapps.market.util.Constants;
import com.kapps.market.util.ResourceEnum;

/**
 * 图片轮播
 * 
 * @author shuizhu
 * 
 */
public class ConverFlow extends View implements GestureDetector.OnGestureListener, IResultReceiver {

	public static final String TAG = "ConverFlow";

	private boolean mShouldStopFling = false;
	// 锟斤拷锟斤拷
	private int mPieceCount;
	private int mAnimationDuration = 400;
	private int appNameFontSize;
	private int NAME_PAD;
	private int TOP_PAD;
	private int mPieceWidth;
	private int mPieceHeight;
	private int mBgWidth;
	private int mBgHeight;
	private int bgPadLeft;
	private int bgPadTop;
	// 锟斤拷锟叫碉拷斜锟角讹拷
	private double wAng = 5 * Math.PI / 180;
	private int SEP_WIDTH;
	private int nearestChildIndex = 3;
	private ArrayList<RangeInfo> rList = new ArrayList<RangeInfo>();
	private GestureDetector mGestureDetector;
	private FlingRunnable mFlingRunnable;
	private Paint mPaint;
	private Drawable bgDrawable;
	private Drawable emptyDrawable;
	private int nameYPos;

	// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
	private AppCahceManager appCahceManager;
	private AssertCacheManager assertCacheManager;
	private TaskMarkPool taskMarkPool;
	private MarketServiceWraper serviceWraper;
	private ATaskMark taskMark;
	private volatile boolean inInit;
	private volatile boolean needShowApp;
	// 应锟斤拷循锟斤拷指锟斤拷
	private int appRIndex = 0;

	// 锟斤拷示锟斤拷一锟斤拷
	public static final int NEXT_SHOT_DELAY = 3000;
	private int SHOW_NEXT_SLOT_MSG = 11;
	private NextSlotHandle nextSlotHandle;
	private volatile boolean needScrollNextSlot;
	// 锟杰的癸拷锟斤拷锟斤拷锟斤拷
	private int totalAutoScrollCount;

	public ConverFlow(Context context, AttributeSet attrSet) {
		super(context, attrSet);

	
		bgDrawable = context.getResources().getDrawable(R.drawable.cf_bg);
		mGestureDetector = new GestureDetector(context, this);
		mGestureDetector.setIsLongpressEnabled(false);
		mFlingRunnable = new FlingRunnable();

	
		NAME_PAD = getResources().getDimensionPixelOffset(R.dimen.NAME_PAD);
		TOP_PAD = getResources().getDimensionPixelOffset(R.dimen.TOP_PAD);
		mPieceWidth = getResources().getDimensionPixelOffset(R.dimen.M_PIECE_WIDTH);
		mPieceHeight = getResources().getDimensionPixelOffset(R.dimen.M_PIECE_HEIGHT);
		mBgWidth = getResources().getDimensionPixelOffset(R.dimen.M_BG_WIDTH);
		mBgHeight = getResources().getDimensionPixelOffset(R.dimen.M_BG_HEIGHT);
		bgPadLeft = getResources().getDimensionPixelOffset(R.dimen.BG_PAD_LEFT);
		bgPadTop = getResources().getDimensionPixelOffset(R.dimen.BG_PAD_TOP);
		appNameFontSize = getResources().getDimensionPixelOffset(R.dimen.APP_NAME_FONT_SIZE);
		SEP_WIDTH = getResources().getDimensionPixelOffset(R.dimen.SEP_WIDTH);

		nameYPos = TOP_PAD + mBgHeight + NAME_PAD;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(appNameFontSize);
		mPaint.setTextAlign(Align.CENTER);

		// 应锟斤拷锟斤拷锟�
		MApplication mApplication = MApplication.getInstance();
		emptyDrawable = mApplication.emptyADAppIcon;
		appCahceManager = mApplication.getAppCahceManager();
		assertCacheManager = mApplication.getAssertCacheManager();
		taskMarkPool = mApplication.getTaskMarkPool();
		serviceWraper = mApplication.getServiceWraper();
		taskMark = taskMarkPool.getAppAdvertiseTaskMark(ResourceEnum.AD_TYPE_TOP);
		serviceWraper.forceTakeoverTask(this, taskMark);

		// 锟皆讹拷锟斤拷锟斤拷
		nextSlotHandle = new NextSlotHandle();
		nextSlotHandle.sendEmptyMessageDelayed(SHOW_NEXT_SLOT_MSG, NEXT_SHOT_DELAY + 2);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
				getResources().getDimensionPixelOffset(R.dimen.CONVER_FLOW_HEIGHT), MeasureSpec.EXACTLY));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (rList.size() != mPieceCount || inInit) {
			return;
		}

		// 锟斤拷锟斤拷
		mPaint.setColor(0xff393939);
		canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);

		// 锟斤拷锟斤拷
		Drawable d = null;
		RangeInfo rInfo = null;
		// right
		for (int index = mPieceCount - 1; index > nearestChildIndex; index--) {
			rInfo = rList.get(index);
			bgDrawable.setBounds(rInfo.bX, rInfo.bY, rInfo.bX + rInfo.bWidth, rInfo.bY + rInfo.bHeight);
			bgDrawable.setAlpha(rInfo.alpha);
			bgDrawable.draw(canvas);

			d = rInfo.drawable;
			d.setBounds(rInfo.x, rInfo.y, rInfo.x + rInfo.width, rInfo.y + rInfo.height);
			d.setAlpha(rInfo.alpha);
			d.draw(canvas);
		}

		// left + center
		for (int index = 0; index <= nearestChildIndex; index++) {
			rInfo = rList.get(index);
			bgDrawable.setBounds(rInfo.bX, rInfo.bY, rInfo.bX + rInfo.bWidth, rInfo.bY + rInfo.bHeight);
			bgDrawable.setAlpha(rInfo.alpha);
			bgDrawable.draw(canvas);

			d = rInfo.drawable;
			d.setBounds(rInfo.x, rInfo.y, rInfo.x + rInfo.width, rInfo.y + rInfo.height);
			d.setAlpha(rInfo.alpha);
			d.draw(canvas);
		}

		// 锟斤拷锟斤拷应锟斤拷锟斤拷锟斤拷
		// AppItem appItem = rList.get(nearestChildIndex).appItem;
		// mPaint.setColor(Color.WHITE);
		// if (appItem != null) {
		// canvas.drawText(appItem.getName(), getWidth() / 2, nameYPos, mPaint);
		// } else {
		// canvas.drawText("", getWidth() / 2, nameYPos, mPaint);
		// }

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		// Log.d(TAG, "onSizeChanged w: " + w + " h:" + h);
		// 锟斤拷始锟斤拷片锟斤拷息
		initPieceInfo(w, h);
	}

	// 锟斤拷始锟斤拷片锟斤拷息
	protected void initPieceInfo(int w, int h) {
		// Log.d(TAG, "init piece info w: " + w + " h:" + h);
		if (w == 0 || h == 0) {
			return;
		}
		inInit = true;
		rList.clear();

		// 锟斤拷么锟斤拷碌锟斤拷锟叫憋拷锟�
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			wAng = 5 * Math.PI / 180;
		} else {
			wAng = 8 * Math.PI / 180;
		}

		// 确锟斤拷锟杰癸拷要锟斤拷片
		RangeInfo rangeInfo = null;
		int gCenter = getGalleryCenter();
		int leftPos = gCenter - mPieceWidth / 2;
		int rightPos = gCenter + mPieceWidth / 2;
		mPieceCount = (leftPos / SEP_WIDTH + 2) * 2 + 1;
		// Log.d(TAG, "init piece info mPieceCount: " + mPieceCount);
		int avg = mPieceCount / 2;
		// 确锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷幕锟斤拷锟斤拷锟斤拷锟�
		totalAutoScrollCount = appCahceManager.getAppItemCount(taskMark);

		int iPos = 1;
		// 锟斤拷锟�
		for (int index = avg - 1; index >= 0; index--) {
			rangeInfo = new RangeInfo(index);
			rangeInfo.x = leftPos - iPos * SEP_WIDTH;
			adjustRangeInfo(mPieceWidth, mPieceHeight, wAng, SEP_WIDTH * iPos, rangeInfo);
			adjustBgInfo(rangeInfo);
			rList.add(rangeInfo);
			iPos++;
		}

		// 锟揭憋拷
		iPos = 1;
		for (int index = avg + 1; index < mPieceCount; index++) {
			rangeInfo = new RangeInfo(index);
			// 锟斤拷锟絰只锟角革拷锟斤拷锟斤拷锟斤拷锟斤拷锟绞撅拷锟斤拷辖锟�
			rangeInfo.x = rightPos + iPos * SEP_WIDTH;
			adjustRangeInfo(mPieceWidth, mPieceHeight, wAng, SEP_WIDTH * iPos, rangeInfo);
			rangeInfo.x -= rangeInfo.width;
			adjustBgInfo(rangeInfo);
			rList.add(rangeInfo);
			iPos++;
		}

		// 锟叫硷拷
		rangeInfo = new RangeInfo(avg);
		rangeInfo.width = mPieceWidth;
		rangeInfo.height = mPieceHeight;
		rangeInfo.x = gCenter - mPieceWidth / 2;
		rangeInfo.y = TOP_PAD;
		rangeInfo.alpha = 255;
		rangeInfo.bX = rangeInfo.x - bgPadLeft;
		rangeInfo.bY = rangeInfo.y - bgPadTop;
		rangeInfo.bWidth = mBgWidth;
		rangeInfo.bHeight = mBgHeight;
		rList.add(rangeInfo);

		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
		Collections.sort(rList);

		// 锟斤拷始锟斤拷锟斤拷锟斤拷图片
		MultipleTaskMark mTaskMark = new MultipleTaskMark();
		for (RangeInfo rInfo : rList) {
			updateRangeAppInfo(rInfo);
		}
		// 锟斤拷始锟斤拷锟斤拷锟斤拷锟斤拷
		// Log.d("initPieceInfo: ", "image task size: " +
		// mTaskMark.getTaskMarkList().size());
		if (mTaskMark.getTaskMarkList().size() != 0) {
			serviceWraper.scheduleAppImageResourceTask(this, mTaskMark, null);
		}

		// 锟叫硷拷
		nearestChildIndex = rList.size() / 2;

		inInit = false;
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		if (taskMark instanceof AppImageTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			for (RangeInfo rInfo : rList) {
				// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟姐，锟斤拷锟斤拷锟斤拷芏锟斤拷rinfo锟斤拷示同一锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷斜锟斤拷锟斤拷锟斤拷锟斤拷锟叫★拷
				if (rInfo.appItem != null && rInfo.appItem.getId() == ((AppImageTaskMark) taskMark).getId()) {
					Drawable d = assertCacheManager.getAdvertiseIconFromCache(rInfo.appItem.getIconId());
					if (d != null) {
						rInfo.drawable = d;
						invalidate();
					}
				}
			}

		} else if (taskMark instanceof AppAdvertiseTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			int w = getWidth();
			int h = getHeight();
			initPieceInfo(w, h);
		}
	}

	// 锟斤拷锟劫癸拷锟斤拷
	private void trackMotionScroll(double delta) {
		if (Math.abs(delta) == 0 || rList.size() != mPieceCount) {
			return;
		}

		// 锟角凤拷锟斤拷锟斤拷
		boolean left = false;
		int gCenter = getGalleryCenter();
		int leftPos = gCenter - mPieceWidth / 2;
		int rightPos = gCenter + mPieceWidth / 2;
		RangeInfo rangeInfo = null;
		for (int index = 0; index < rList.size(); index++) {
			rangeInfo = rList.get(index);
			left = (rangeInfo.x + rangeInfo.width / 2) > getGalleryCenter() ? false : true;
			if (left) {
				rangeInfo.x = (int) Math.round(rangeInfo.x + delta);
				adjustRangeInfo(mPieceWidth, mPieceHeight, wAng, (leftPos - rangeInfo.x), rangeInfo);

			} else {
				// 锟揭憋拷x锟斤拷锟侥硷拷锟斤拷锟斤拷肟硷拷堑锟斤拷锟饺的变化锟斤拷
				int newRightX = (int) Math.round(rangeInfo.x + rangeInfo.width + delta);
				adjustRangeInfo(mPieceWidth, mPieceHeight, wAng, newRightX - rightPos, rangeInfo);
				rangeInfo.x = newRightX - rangeInfo.width;
			}
			adjustBgInfo(rangeInfo);
		}

		// 锟斤拷锟侥筹拷锟斤拷丫锟斤拷锟斤拷锟斤拷锟斤拷锟揭边凤拷围锟斤拷锟斤拷么锟狡筹拷锟斤拷诺锟斤拷锟揭伙拷锟饺ワ拷锟�
		// right
		if (delta > 0) {
			rangeInfo = rList.get(rList.size() - 1);
			if (rangeInfo.x > getMaxLimitedXPos()) {
				rangeInfo = rList.remove(rList.size() - 1);
				RangeInfo fristRange = rList.get(0);
				rangeInfo.x = Math.round(fristRange.x - SEP_WIDTH);
				adjustRangeInfo(mPieceWidth, mPieceHeight, wAng, leftPos - rangeInfo.x, rangeInfo);
				adjustBgInfo(rangeInfo);
				rList.add(0, rangeInfo);

				// 锟斤拷锟斤拷锟斤拷锟絩ange锟斤拷应锟斤拷锟斤拷息
				updateRangeAppInfo(rangeInfo);
			}

		} else {
			rangeInfo = rList.get(0);
			if (rangeInfo.x + rangeInfo.width < getMinLimitedXPos()) {
				rangeInfo = rList.remove(0);
				RangeInfo lastRange = rList.get(rList.size() - 1);
				int newRightX = Math.round(lastRange.x + lastRange.width + SEP_WIDTH);
				adjustRangeInfo(mPieceWidth, mPieceHeight, wAng, newRightX - rightPos, rangeInfo);
				rangeInfo.x = newRightX - rangeInfo.width;
				adjustBgInfo(rangeInfo);
				rList.add(rangeInfo);

				// 锟斤拷锟斤拷锟斤拷锟絩ange锟斤拷应锟斤拷锟斤拷息
				updateRangeAppInfo(rangeInfo);
			}
		}
		// Log.d(TAG, "nearestChildIndex: " + nearestChildIndex);

		// 锟斤拷锟斤拷锟斤拷锟斤拷
		int minCenterDX = Integer.MAX_VALUE;
		for (int index = 0; index < rList.size(); index++) {
			rangeInfo = rList.get(index);
			int testCenterDX = Math.abs(gCenter - (rangeInfo.x + rangeInfo.width / 2));
			if (minCenterDX > testCenterDX) {
				nearestChildIndex = index;
				minCenterDX = testCenterDX;
			}
		}

		invalidate();
	}

	// 锟斤拷锟斤拷图片
	private void updateRangeAppInfo(RangeInfo rangeInfo) {
		appRIndex++;
		int count = appCahceManager.getAppItemCount(taskMark);
		if (count == 0) {
			rangeInfo.drawable = emptyDrawable;

		} else {
			AppItem appItem = appCahceManager.getAppItemByMarkIndex(taskMark, appRIndex % count);
			// Log.d(TAG, "appRIndex: " + appRIndex + " appItem: " +
			// appItem.toString());
			rangeInfo.appItem = appItem;
			Drawable d = assertCacheManager.getAdvertiseIconFromCache(appItem.getIconId());
			if (d != null) {
				rangeInfo.drawable = d;

			} else {
				rangeInfo.drawable = emptyDrawable;
				AppImageTaskMark imageTaskMark = taskMarkPool.createAppImageTaskMark(appItem.getId(),
						appItem.getAdIcon(), MImageType.APP_ADVERTISE_ICON);
				serviceWraper.getAppImageResource(this, imageTaskMark, null, imageTaskMark.getId(),
						imageTaskMark.getUrl(), imageTaskMark.getType());
			}
		}
	}

	// 锟斤拷锟斤拷围锟斤拷息
	private void adjustRangeInfo(int w, int h, double wAng, double offSet, RangeInfo rangeInfo) {
		// Log.d(TAG, "toSmall w:" + w + " h:" + h + " wAng: " + wAng +
		// " offSet: " + offSet);
		rangeInfo.height = Math.min(((int) Math.round(h - 2 * offSet * Math.tan(wAng))), h);
		rangeInfo.width = Math.min(((int) Math.round(rangeInfo.height / (double) h * w)), w);
		rangeInfo.y = Math.max((Math.round((h - rangeInfo.height) / 2) + TOP_PAD), TOP_PAD);
		if (rangeInfo.height == h) {
			rangeInfo.alpha = 255;
		} else {
			rangeInfo.alpha = Math.round(rangeInfo.height / (h * 1.3f) * 255);
		}

	}

	// 锟斤拷锟姐背锟斤拷图
	private void adjustBgInfo(RangeInfo rangeInfo) {
		// 锟斤拷锟�锟斤拷
		rangeInfo.bX = Math.round(rangeInfo.x - bgPadLeft * ((float) rangeInfo.width / mPieceWidth));
		rangeInfo.bY = Math.round(rangeInfo.y - bgPadTop * ((float) rangeInfo.height / mPieceHeight));
		rangeInfo.bWidth = Math.round(mBgWidth * ((float) rangeInfo.width / mPieceWidth));
		rangeInfo.bHeight = Math.round(mBgHeight * ((float) rangeInfo.height / mPieceHeight));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean rest = mGestureDetector.onTouchEvent(event);
		int action = event.getAction();
		// TODO android2.2 锟斤拷锟斤拷锟街柑э拷锟斤拷潜锟窖★拷锟揭伙拷锟斤拷锟斤拷屎系木锟斤拷小锟�
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			if (mFlingRunnable.mScroller.isFinished()) {
				scrollIntoSlots(nearestChildIndex);
			}
		}
		return rest;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// Log.d(TAG, "GestureDetector -> onDown.......");
		mFlingRunnable.stop(false);
		needShowApp = false;
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// Log.d(TAG, "GestureDetector -> onFling.......");
		// Fling the gallery!
		mFlingRunnable.startUsingVelocity((int) -(velocityX * 0.4));
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// Log.d(TAG, "GestureDetector -> onLongPress.......");
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// Log.d(TAG, "GestureDetector -> onScroll distanceX: " + distanceX +
		// " distanceY: " + distanceY);
		trackMotionScroll(-distanceX);
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// Log.d(TAG, "GestureDetector -> onShowPress.......");

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// Log.d(TAG, "GestureDetector -> onSingleTapUp......e: " + e);
		RangeInfo hitInfo = getHitRangeInfo(e);
		if (hitInfo != null) {
			// Log.d(TAG, "onSingleTapUp......hitInfo: " + hitInfo);
			needShowApp = true;
			scrollIntoSlots(rList.indexOf(hitInfo));
		}

		return false;
	}

	// 锟斤拷锟剿拷锟窖★拷锟�
	private RangeInfo getHitRangeInfo(MotionEvent e) {
		int x = Math.round(e.getX());
		int y = Math.round(e.getY());
		RangeInfo fInfo = null, nInfo = null;
		Rect hitRect;

		// 锟叫硷拷
		RangeInfo centInfo = rList.get(nearestChildIndex);
		hitRect = new Rect(centInfo.x, centInfo.y, centInfo.x + centInfo.width, y + centInfo.height);
		if (hitRect.contains(x, y)) {
			return centInfo;

		} else {
			// 锟斤拷锟�
			if (x < getGalleryCenter()) {
				for (int index = 1; index <= nearestChildIndex; index++) {
					fInfo = rList.get(index - 1);
					nInfo = rList.get(index);
					hitRect = new Rect(fInfo.x, fInfo.y, nInfo.x, fInfo.y + fInfo.height);
					if (hitRect.contains(x, y)) {
						return fInfo;
					}
				}

				// 锟揭憋拷
			} else {
				for (int index = nearestChildIndex + 1; index < rList.size(); index++) {
					fInfo = rList.get(index - 1);
					nInfo = rList.get(index);
					hitRect = new Rect(fInfo.x + fInfo.width, nInfo.y, nInfo.x + nInfo.width, nInfo.y + nInfo.height);
					if (hitRect.contains(x, y)) {
						return nInfo;
					}
				}
			}
		}
		return null;
	}

	// 锟斤拷示锟斤拷锟斤拷锟斤拷锟较�
	public void handleShowAppDetail(AppItem appItem) {
		Intent intent = new Intent(getContext(), AppDetailFrame.class);
		if (appItem != null) {
			intent.putExtra(Constants.APP_ID, appItem.getId());
			((Activity) getContext()).startActivityForResult(intent, Constants.ACTIVITY_RCODE_APPDETAIL);
		}
	}

	// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
	private void scrollIntoSlots(int hitIndex) {
		// Log.d(TAG, "scrollIntoSlots......hitIndex: " + hitIndex);
		RangeInfo rangeInfo = rList.get(hitIndex);
		int gCenter = getGalleryCenter();
		int deta = (rangeInfo.x + rangeInfo.width / 2) - gCenter;
		// right
		if (deta > 0) {
			int rightPos = gCenter + mPieceWidth / 2;
			mFlingRunnable.startUsingDistance(rangeInfo.x + rangeInfo.width - rightPos);

		} else if (deta < 0) {
			int leftPos = gCenter - mPieceWidth / 2;
			mFlingRunnable.startUsingDistance(-(leftPos - rangeInfo.x));

		} else if (needShowApp) {
			needShowApp = false;
			handleShowAppDetail(rangeInfo.appItem);
		}
	}

	// 锟斤拷锟斤拷锟斤拷锟斤拷一锟斤拷
	private void scrollNextSlots() {
		if (rList.size() > 0) {
			scrollIntoSlots(Math.abs(nearestChildIndex - 1) % rList.size());
		}
	}

	private int getGalleryCenter() {
		return getWidth() / 2;
	}

	private int getMaxLimitedXPos() {
		return getWidth();
	}

	private int getMinLimitedXPos() {
		return 0;
	}

	// 撞锟斤拷
	private class FlingRunnable implements Runnable {

		private final int MAX_VELOCITY = 600;
		private final int MAX_NAV_VELOCITY = -600;
		/**
		 * Tracks the decay of a fling scroll
		 */
		private Scroller mScroller;
		/**
		 * X value reported by mScroller on the previous fling
		 */
		private int mLastFlingX;

		public FlingRunnable() {
			mScroller = new Scroller(getContext());
		}

		private void startCommon() {
			// Remove any pending flings
			removeCallbacks(this);
		}

		public void startUsingVelocity(int initialVelocity) {
			if (initialVelocity == 0) {
				return;
			}
			// Log.d(TAG, "startUsingVelocity: " + initialVelocity);
			startCommon();
			int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
			if (initialVelocity > MAX_VELOCITY) {
				initialVelocity = MAX_VELOCITY;

			} else if (initialVelocity < MAX_NAV_VELOCITY) {
				initialVelocity = MAX_NAV_VELOCITY;
			}
			mLastFlingX = initialX;
			mScroller.fling(initialX, 0, initialVelocity, 0, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			post(this);
		}

		public void startUsingDistance(int distance) {
			// Log.d(TAG, "startUsingDistance: " + distance);
			if (distance == 0) {
				return;
			}
			startCommon();
			mLastFlingX = 0;
			mScroller.startScroll(0, 0, distance, 0, mAnimationDuration);
			post(this);
		}

		public void stop(boolean scrollIntoSlots) {
			removeCallbacks(this);
			endFling(scrollIntoSlots);
		}

		private void endFling(boolean scrollIntoSlots) {
			mScroller.forceFinished(true);
			if (scrollIntoSlots) {
				scrollIntoSlots(nearestChildIndex);
			}
		}

		@Override
		public void run() {
			if (rList.size() != mPieceCount) {
				endFling(true);
				return;
			}

			mShouldStopFling = false;

			final Scroller scroller = mScroller;
			boolean more = scroller.computeScrollOffset();
			final int x = scroller.getCurrX();

			// Flip sign to convert finger direction to list items direction
			// (e.g. finger moving down means list is moving towards the
			// top)
			int delta = mLastFlingX - x;

			// Pretend that each frame of a fling scroll is a touch scroll
			if (delta > 0) {
				// Don't fling more than 1 screen
				delta = (int) Math.min(getMaxLimitedXPos() * 0.3f - 1, delta);
			} else {
				// Don't fling more than 1 screen
				delta = (int) Math.max(-(getMaxLimitedXPos() * 0.3f - 1), delta);
			}

			trackMotionScroll(delta);

			if (more && !mShouldStopFling) {
				mLastFlingX = x;
				post(this);
			} else {
				endFling(true);
			}
		}
	}

	// 锟斤拷锟斤拷图片锟斤拷锟斤拷锟斤拷锟斤拷息
	private class RangeInfo implements Comparable<RangeInfo> {
		private int index;
		// 图片
		public int x;
		public int y;
		public int width;
		public int height;
		// 锟斤拷锟斤拷
		public int bX;
		public int bY;
		public int bWidth;
		public int bHeight;
		// 透锟斤拷锟斤拷
		public int alpha;
		// 锟斤拷前应锟斤拷要锟斤拷示锟斤拷图片
		public Drawable drawable;
		// 锟斤拷前锟斤拷应锟斤拷应锟斤拷id
		public AppItem appItem;

		public RangeInfo(int index) {
			this.index = index;

		}

		@Override
		public String toString() {
			return "RangeInfo [index=" + index + ", height=" + height + ", width=" + width + ", x=" + x + ", y=" + y
					+ "]";
		}

		@Override
		public int compareTo(RangeInfo o) {
			if (index == o.index) {
				return 0;
			} else {
				return index > o.index ? 1 : -1;
			}
		}

	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		needScrollNextSlot = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		needScrollNextSlot = false;
	}

	// 锟斤拷一锟斤拷
	private class NextSlotHandle extends Handler {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == SHOW_NEXT_SLOT_MSG) {
				removeMessages(msg.what);
				if (rList.size() == mPieceCount && !inInit && needScrollNextSlot) {
					scrollNextSlots();
					totalAutoScrollCount--;
				}
				// 实锟绞癸拷锟斤拷锟斤拷么锟斤拷锟斤拷锟酵Ｖ�
//				if (totalAutoScrollCount >= 0) 
				{
					sendEmptyMessageDelayed(SHOW_NEXT_SLOT_MSG, NEXT_SHOT_DELAY);
				}
			}
		}
	}

}
