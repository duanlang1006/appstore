package com.kapps.market.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

import com.kapps.market.MApplication;
import com.kapps.market.MarketActivity;
import com.kapps.market.TaskMarkPool;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.MarketServiceWraper;
import com.kapps.market.task.mark.ATaskMark;

/**
 * 2010-6-8 <br>
 * ��ͨ��ͼ������
 * 
 * 2011-3-16 UI�ع�
 */
public abstract class CommonView extends LinearLayout {
	public static final String TAG = "CommonAppView";

	// �г�����
	protected MApplication marketContext = MApplication.getInstance();
	protected MarketServiceWraper serviceWraper;
	protected TaskMarkPool taskMarkPool;

	// ��ʱ��ʼ����handler, �������๲��
	private Handler delayHandler;
	// ֧�ֵ�������
	private PopupView popupView;

	/**
	 * @param context
	 */
	public CommonView(Context context) {
		super(context);
		setOrientation(LinearLayout.VERTICAL);
		serviceWraper = marketContext.getServiceWraper();
		taskMarkPool = marketContext.getTaskMarkPool();

	}

	public CommonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);
		serviceWraper = marketContext.getServiceWraper();
		taskMarkPool = marketContext.getTaskMarkPool();
	}

	/**
	 * 
	 * @param layoutResID
	 */
	public void addView(int layoutResID) {
		// ���ݲ���
		LayoutInflater.from(getContext()).inflate(layoutResID, this);
		
	}

	/**
	 * ��Ҫʱˢ����ͼ, ������ʾ���û���ʱ�����
	 * 
	 * @param what
	 *            ΪʲôҪˢ��
	 */
	public void flushView(int what) {

	}

	/**
	 * ���������ͼ���Ƴ������˵�ʱ��
	 */
	public void releaseView() {

	}

	/**
	 * �����Ϣ�ı�ǣ���һcontent��������ͼ��Activity��hashCodeΪ��ǡ�
	 */
	public int getMessageMark() {
		return getContext().hashCode();
	}

	/**
	 * ����ѡ����ͼ��һ�������û����»��˰�ťʱ�����Ϊ��
	 * 
	 * @return �Ƿ��Ѿ� ������
	 */
	public boolean rotateContentView() {
		return false;
	}

	/**
	 * ��ݴ����mark���к���Ĵ�����ͼ
	 * 
	 * @param mark
	 */
	public void handleChainMessage(Message message) {

	}

	/**
	 * ��ʽִ�н����
	 * 
	 * @param viewMark
	 * @param taskMark
	 * @param exception
	 * @param trackerResult
	 */
	protected void chainReceiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {

	}

	// TODO m b ����1.5�ȴ�����Ļ�����л�

	/**
	 * ���ֻ��/������һЩ״̬����仯ʱ�����ķ���
	 */
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		handleOrientationChange(newConfig.orientation);
	}

	/**
	 * ����(���ױ�����CommonAppView����)������Ϣ������ͼ������δ��ɷ���Ϣ��<br>
	 * ������Ϣ��handleChainMessage(Message message)���?
	 * 
	 * @see handleChainMessage(message)
	 */
	protected void notifyMessageToParent(Message msg) {
		ViewParent viewParent = getParent();
		// int layer = 0;
		while (viewParent instanceof View) {
			// Log.d(TAG, "viewParent: " + viewParent + " layer" + layer++);
			if (viewParent instanceof CommonView) {
				CommonView parent = (CommonView) viewParent;
				// ����ֻ�ܵ���handleChainMessage
				parent.handleChainMessage(msg);
			}

			viewParent = viewParent.getParent();
		}

		// ֪ͨ��activity
		if (getContext() instanceof MarketActivity) {
			((MarketActivity) getContext()).subHandleMessage(msg);
		}
	}

	/**
	 * ������ͼ�ı�
	 * 
	 * @param orientation
	 */
	protected void handleOrientationChange(int orientation) {

	}

	/**
	 * ������ʱ����Ϣ
	 * 
	 * @param message
	 */
	protected void sendQueueMessage(Message message) {
		if (delayHandler == null) {
			delayHandler = new DelayHandler();
		}
		delayHandler.sendMessage(message);
	}

	protected void sendEmptyQueueMessage(int what) {
		if (delayHandler == null) {
			delayHandler = new DelayHandler();
		}
		delayHandler.sendEmptyMessage(what);
	}

	/**
	 * �Ƴ�ɵ���Ϣ
	 * 
	 * @param what
	 * @param delay
	 */
	protected void sendUniqueEmptyQueueMessage(int what, long delay) {
		removeEmptyMessage(what);
		delayHandler.sendEmptyMessageDelayed(what, delay);
	}

	/**
	 * �Ƴ�ĳ����Ϣ
	 * 
	 * @param what
	 */
	protected void removeEmptyMessage(int what) {
		if (delayHandler == null) {
			delayHandler = new DelayHandler();
		}
		delayHandler.removeMessages(what);
	}

	// ��ʱ������
	private class DelayHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			handleChainMessage(msg);
		}

	}

	protected void showPopwindow(View anchor, View contentView) {
		if (popupView == null) {
			popupView = new PopupView(getContext());
		}
		popupView.setWidth(anchor.getWidth());
		popupView.setWindowLayoutMode(anchor.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
		popupView.setContentView(contentView);
		popupView.showAsDropDown(anchor);

	}

	protected void showPopwindow(View anchor, View contentView, int xOff, int yOff) {
		if (popupView == null) {
			popupView = new PopupView(getContext());
		}
		popupView.setWidth(anchor.getWidth());
		popupView.setWindowLayoutMode(anchor.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
		popupView.setContentView(contentView);
		popupView.showAsDropDown(anchor, xOff, yOff);

	}

	public void hidePopupView() {
		if (popupView != null && popupView.isShowing()) {
			popupView.dismiss();
			popupView = null;
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		hidePopupView();
		super.onWindowFocusChanged(hasWindowFocus);
	}

	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		hidePopupView();
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}

	@Override
	protected void onAttachedToWindow() {
		hidePopupView();
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		hidePopupView();
		super.onDetachedFromWindow();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		hidePopupView();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/** {@inheritDoc} */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (popupView != null && popupView.isShowing()) {
			detectEventOutside(event);

			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				hidePopupView();
				return true;
			} else {
				return super.dispatchTouchEvent(event);
			}

		} else {
			return super.dispatchTouchEvent(event);
		}
	}

	// ����Ƿ�Ҫ�رյ�����
	protected void detectEventOutside(MotionEvent event) {
		if (popupView != null && event.getAction() == MotionEvent.ACTION_DOWN) {
			Rect mRect = new Rect();
			int[] locations = new int[] { 0, 0 };
			popupView.getContentView().getLocationOnScreen(locations);
			popupView.getContentView().getHitRect(mRect);
			mRect.left = mRect.left + locations[0];
			mRect.top = mRect.top + locations[1];
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			boolean contain = mRect.contains(x, y);
			if (!contain) {
				event.setAction(MotionEvent.ACTION_OUTSIDE);
			}
		}
	}

}
