package com.kapps.market.ui;

import android.content.Context;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsSpinner;

import com.kapps.market.MarketManager;
import com.kapps.market.bean.Iconable;
import com.kapps.market.cache.AppCahceManager;
import com.kapps.market.cache.AssertCacheManager;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppImageTaskMark;
import com.kapps.market.task.mark.MultipleTaskMark;

/**
 * 2010-7-28 <br>
 * 锟斤拷锟斤拷锟斤拷实锟斤拷斜?锟斤拷晒锟斤拷锟斤拷谋锟斤拷锟酵硷拷锟斤拷锟截等★拷
 * 
 * @author admin
 * 
 */
public abstract class BaseItemList extends LoadableList implements OnScrollListener, OnTouchListener {

	// 锟斤拷源
	protected AssertCacheManager assertCacheManager = marketContext.getAssertCacheManager();
	// 锟斤拷锟�
	protected AppCahceManager appCahceManager = marketContext.getAppCahceManager();
	// 锟斤拷锟斤拷
	protected MarketManager marketManager = marketContext.getMarketManager();

	// 锟斤拷锟斤拷锟斤拷
	protected boolean scrolling;
	// 锟角凤拷锟揭伙拷锟斤拷锟酵硷拷锟斤拷丫锟斤拷锟斤拷
	// 锟斤拷锟斤拷使锟矫癸拷锟斤拷停止锟斤拷时锟津触凤拷锟斤拷锟斤拷图锟疥，锟斤拷锟叫碉拷一锟轿硷拷锟斤拷锟斤拷锟斤拷锟揭拷侄锟斤拷锟斤拷锟斤拷锟�
	protected boolean firstTriggerLoadIconOver;
	// 通知图锟斤拷锟斤拷锟�
	private static final int NOTIFY_UPDATE_FOR_IMAGE = 36663;
	private static final int NOTIFY_LOAD_IMAGE = 47774;
	private static final int IMAGE_HANDLE_DELAY = 100;

	public BaseItemList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BaseItemList(Context context, boolean useLongClick) {
		super(context, useLongClick);
	}

	/**
	 * @param context
	 */
	public BaseItemList(Context context) {
		super(context);
	}

	@Override
	public void initLoadleList(ATaskMark taskWraper) {
		initLoadleList(taskWraper, true, true, false);
	}

	@Override
	public void initLoadleList(ATaskMark taskWraper, boolean loadInit) {
		initLoadleList(taskWraper, true, loadInit, false);
	}

	@Override
	public void initLoadleList(ATaskMark taskWraper, boolean autoLoad, boolean loadInit, boolean forceLoad) {
		if (mTaskMark != taskWraper) {
			firstTriggerLoadIconOver = false;
		}
		super.initLoadleList(taskWraper, autoLoad, loadInit, forceLoad);
	}

	@Override
	protected void initView(Context context) {
		super.initView(context);

		// 锟斤拷锟斤拷AbsListView 锟斤拷锟斤拷touch锟铰硷拷锟叫讹拷锟角凤拷要锟斤拷锟斤拷图锟斤拷
		// 锟角呵ｏ拷锟斤拷锟斤拷锟斤拷锟紸bsSpinner只锟斤拷锟斤拷锟斤拷锟斤拷锟�
		if (adapterView instanceof AbsSpinner) {
			adapterView.setOnTouchListener(this);

		} else {
			((AbsListView) adapterView).setOnScrollListener(this);
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v instanceof AbsSpinner && event.getAction() == MotionEvent.ACTION_UP) {
			handleNeedUpdateIcon();
		}
		return false;
	}

	/**
	 * 锟斤拷锟斤拷锟铰碉拷锟斤拷锟斤拷锟斤拷锟斤拷锟今，碉拷锟斤拷锟斤拷锟饺硷拷锟斤拷欠锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷印锟�锟斤拷浅锟斤拷锟揭拷锟�
	 */
	@Override
	protected void tryQueryNewItems() {
		LogUtil.d(TAG, "tryQueryNewItems** taskMark : " + mTaskMark);
		// 锟饺硷拷锟斤拷欠锟斤拷锟揭拷锟斤拷锟�
		if (!mTaskMark.isLoadEnd() && mTaskMark.getTaskStatus() != ATaskMark.HANDLE_DOING
				&& mTaskMark.getTaskStatus() != ATaskMark.HANDLE_ERROR) {
			if (isAutoLoad()) {
				handleLoadNewItems(mTaskMark);

			} else {
				mTaskMark.setTaskStatus(ATaskMark.HANDLE_WAIT);
			}

		} else if (mTaskMark.getTaskStatus() == ATaskMark.HANDLE_DOING) {
			serviceWraper.forceTakeoverTask(this, mTaskMark);
		}

		updateViewStatus(mTaskMark);
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		super.receiveResult(taskMark, exception, trackerResult);

		if (taskMark instanceof AppImageTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER && !scrolling) {
			sendUniqueEmptyQueueMessage(NOTIFY_UPDATE_FOR_IMAGE, IMAGE_HANDLE_DELAY);

		} else if (mTaskMark == taskMark) {
			// 通知锟斤拷莞谋锟�
			notifyDataSetChanged();
			// 锟斤拷锟斤拷锟酵�
			updateViewStatus(taskMark);
		}
	}

	@Override
	public void handleChainMessage(Message message) {
		super.handleChainMessage(message);
		switch (message.what) {
		case NOTIFY_UPDATE_FOR_IMAGE:
			if (!scrolling) {
				notifyDataSetChanged();
			}
			break;

		case NOTIFY_LOAD_IMAGE:
			if (!scrolling) {
				handleLoadsIcon();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (!firstTriggerLoadIconOver && visibleItemCount > 0) {
			firstTriggerLoadIconOver = true;
			onScrollStateChanged((AbsListView) adapterView, OnScrollListener.SCROLL_STATE_IDLE);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView adapterView, int scrollState) {
		switch (scrollState) {
		// 锟斤拷锟斤拷锟斤拷锟斤拷时锟斤拷锟斤拷锟斤拷图锟斤拷
		case OnScrollListener.SCROLL_STATE_IDLE:
			scrolling = false;
			handleNeedUpdateIcon();
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			scrolling = true;
			break;
		case OnScrollListener.SCROLL_STATE_FLING:
			scrolling = true;
			break;
		}
	}

	/**
	 * 锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷图锟斤拷锟斤拷
	 */
	public void handleNeedUpdateIcon() {
		sendUniqueEmptyQueueMessage(NOTIFY_LOAD_IMAGE, IMAGE_HANDLE_DELAY);
	}

	/**
	 * 锟斤拷锟斤拷要锟揭筹拷图锟斤拷锟剿ｏ拷锟斤拷锟街伙拷锟斤拷诩锟斤拷锟斤拷锟侥匡拷锟斤拷
	 */
	public void handleQuitUpdateIcon() {
		removeEmptyMessage(NOTIFY_LOAD_IMAGE);
	}

	/**
	 * 锟斤拷椴拷锟斤拷锟酵硷拷锟侥达拷锟斤拷锟竭硷拷锟斤拷
	 */
	private void handleLoadsIcon() {
		// ? 为什么adapterView锟斤拷锟叫匡拷锟杰筹拷锟斤拷null锟斤拷锟斤拷锟斤拷
		if (adapterView == null || !isCanLoadItemImage()) {
			notifyDataSetChanged();
			return;
		}

		int firstVisiblePos = adapterView.getFirstVisiblePosition();
		int visibleCount = adapterView.getLastVisiblePosition() - firstVisiblePos + 1;
		int totalCount = getDataItemCount();
		visibleCount = visibleCount > totalCount ? totalCount - 1 : visibleCount;
		firstVisiblePos = firstVisiblePos >= totalCount ? visibleCount : firstVisiblePos;
		// LogUtil.d(TAG, this + " \ncheck load icon visiblecount: " +
		// visibleCount + " \nfirstVisiblePos: "
		// + firstVisiblePos + " lastVisiblePosition: " +
		// adapterView.getLastVisiblePosition());
		MultipleTaskMark mTaskMark = new MultipleTaskMark();
		Iconable itemAble = null;
		Object obj = null;

		// 锟斤拷锟斤拷模锟酵猴拷锟斤拷图锟斤拷莸牟锟揭伙拷锟斤拷锟缴碉拷锟届常锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷浴锟�
		try {
			AppImageTaskMark imageTaskMark = null;
			for (int index = 0; index < visibleCount; index++) {
				obj = adapterView.getItemAtPosition(firstVisiblePos);
				firstVisiblePos++;
				if (obj instanceof Iconable) {
					itemAble = (Iconable) obj;
					// 锟节达拷-锟斤拷锟斤拷锟斤拷-锟斤拷远锟斤拷
					// 锟叫憋拷只锟斤拷锟斤拷锟节达拷锟叫碉拷图片锟斤拷锟皆憋拷锟斤拷锟教度碉拷锟斤拷锟斤拷锟斤拷应锟劫度★拷
					imageTaskMark = getImageTastMark(itemAble);
					// LogUtil.d(TAG, this + " \nimageTaskMark: " +
					// imageTaskMark);
					if (imageTaskMark != null) {
						mTaskMark.addSubTaskMark(imageTaskMark);
					}
				}
			}
			// 锟斤拷锟斤拷图锟斤拷
			if (mTaskMark.getTaskMarkList().size() > 0) {
				marketContext.getServiceWraper().scheduleAppImageResourceTask(this, mTaskMark, null);
			}
		} catch (Exception e) {
			// 锟斤拷锟斤拷
			e.printStackTrace();
		}

		// 锟斤拷锟斤拷锟斤拷图锟斤拷锟斤拷锟酵拷碌锟斤拷锟斤拷斜锟斤拷锟斤拷锟斤拷囟锟斤拷锟较拷锟斤拷锟斤拷锟�
		notifyDataSetChanged();
	}

	@Override
	public void flushView(int what) {
		super.flushView(what);
		// 刷锟斤拷图锟斤拷
		handleNeedUpdateIcon();
	}

	/**
	 * 锟角凤拷锟斤拷约锟斤拷锟酵硷拷辏拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟截讹拷锟斤拷锟斤拷锟斤拷欠锟斤拷屎辖锟斤拷锟酵硷拷锟斤拷锟斤拷
	 * 
	 * @return
	 */
	protected boolean isCanLoadItemImage() {
		return true;
	}

	/**
	 * 锟斤拷眉锟斤拷锟酵计拷锟斤拷锟斤拷锟�
	 * 
	 * @param iconId
	 * @param iconable
	 * @return
	 */
	protected AppImageTaskMark getImageTastMark(Iconable iconable) {
		if (!assertCacheManager.isItemIconExist(iconable.getIconType(), iconable.getIconId())) {
			return marketContext.getTaskMarkPool().createAppImageTaskMark(iconable.getIconId(), iconable.getIconUrl(),
					iconable.getIconType());

		} else {
			return null;
		}
	}

	/**
	 * 锟斤拷锟洁处锟斤拷锟斤拷锟斤拷锟侥硷拷锟斤拷
	 * 
	 * @param taskMark
	 */
	protected abstract void handleLoadNewItems(ATaskMark taskMark);

	@Override
	protected boolean isNeedDispatchItemClick(Object item) {
		return (item instanceof Iconable);
	}

}
