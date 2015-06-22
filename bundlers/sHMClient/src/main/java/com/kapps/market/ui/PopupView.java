package com.kapps.market.ui;

import com.kapps.market.R;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * <p>
 * A popup window that can be used to display an arbitrary view. The popup
 * windows is a floating container that appears on top of the current activity.
 * </p>
 * 
 * @see android.widget.AutoCompleteTextView
 * @see android.widget.Spinner
 */
public class PopupView {
	/**
	 * Mode for {@link #setInputMethodMode(int)}: the requirements for the input
	 * method should be based on the focusability of the popup. That is if it is
	 * focusable than it needs to work with the input method, else it doesn't.
	 */
	public static final int INPUT_METHOD_FROM_FOCUSABLE = 0;

	/**
	 * Mode for {@link #setInputMethodMode(int)}: this popup always needs to
	 * work with an input method, regardless of whether it is focusable. This
	 * means that it will always be displayed so that the user can also operate
	 * the input method while it is shown.
	 */
	public static final int INPUT_METHOD_NEEDED = 1;

	/**
	 * Mode for {@link #setInputMethodMode(int)}: this popup never needs to work
	 * with an input method, regardless of whether it is focusable. This means
	 * that it will always be displayed to use as much space on the screen as
	 * needed, regardless of whether this covers the input method.
	 */
	public static final int INPUT_METHOD_NOT_NEEDED = 2;

	private Context mContext;
	private WindowManager mWindowManager;

	private boolean mIsShowing;
	private boolean mIsDropdown;

	private View mContentView;
	private View mPopupView;
	private boolean mFocusable;
	private int mInputMethodMode = INPUT_METHOD_FROM_FOCUSABLE;
	private int mSoftInputMode;
	private boolean mTouchable = true;
	private boolean mOutsideTouchable = false;
	private boolean mClippingEnabled = true;

	private int mWidthMode;
	private int mWidth;
	private int mHeightMode;
	private int mHeight;

	private int mPopupWidth;
	private int mPopupHeight;

	private int[] mDrawingLocation = new int[2];
	private int[] mScreenLocation = new int[2];
	private boolean mAboveAnchor;

	private OnDismissListener mOnDismissListener;
	private boolean mIgnoreCheekPress = false;

	private int mAnimationStyle = -1;

	/**
	 * <p>
	 * Create a new empty, non focusable popup window of dimension (0,0).
	 * </p>
	 * 
	 * <p>
	 * The popup does provide a background.
	 * </p>
	 */
	public PopupView(Context context) {
		mContext = context;
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	/**
	 * <p>
	 * Return the animation style to use the popup appears and disappears
	 * </p>
	 * 
	 * @return the animation style to use the popup appears and disappears
	 */
	public int getAnimationStyle() {
		return mAnimationStyle;
	}

	/**
	 * Set the flag on popup to ignore cheek press eventt; by default this flag
	 * is set to false which means the pop wont ignore cheek press dispatch
	 * events.
	 * 
	 * <p>
	 * If the popup is showing, calling this method will take effect only the
	 * next time the popup is shown or through a manual call to one of the
	 * {@link #update()} methods.
	 * </p>
	 * 
	 * @see #update()
	 */
	public void setIgnoreCheekPress() {
		mIgnoreCheekPress = true;
	}

	/**
	 * <p>
	 * Change the animation style resource for this popup.
	 * </p>
	 * 
	 * <p>
	 * If the popup is showing, calling this method will take effect only the
	 * next time the popup is shown or through a manual call to one of the
	 * {@link #update()} methods.
	 * </p>
	 * 
	 * @param animationStyle
	 *            animation style to use when the popup appears and disappears.
	 *            Set to -1 for the default animation, 0 for no animation, or a
	 *            resource identifier for an explicit animation.
	 * 
	 * @see #update()
	 */
	public void setAnimationStyle(int animationStyle) {
		mAnimationStyle = animationStyle;
	}

	/**
	 * <p>
	 * Return the view used as the content of the popup window.
	 * </p>
	 * 
	 * @return a {@link android.view.View} representing the popup's content
	 * 
	 * @see #setContentView(android.view.View)
	 */
	public View getContentView() {
		return mContentView;
	}

	/**
	 * <p>
	 * Change the popup's content. The content is represented by an instance of
	 * {@link android.view.View}.
	 * </p>
	 * 
	 * <p>
	 * This method has no effect if called when the popup is showing. To apply
	 * it while a popup is showing, call
	 * </p>
	 * 
	 * @param contentView
	 *            the new content for the popup
	 * 
	 * @see #getContentView()
	 * @see #isShowing()
	 */
	public void setContentView(View contentView) {
		if (isShowing()) {
			return;
		}

		mContentView = contentView;

		if (mContext == null) {
			mContext = mContentView.getContext();
		}

		if (mWindowManager == null) {
			mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		}
	}

	/**
	 * <p>
	 * Indicate whether the popup window can grab the focus.
	 * </p>
	 * 
	 * @return true if the popup is focusable, false otherwise
	 * 
	 * @see #setFocusable(boolean)
	 */
	public boolean isFocusable() {
		return mFocusable;
	}

	/**
	 * <p>
	 * Changes the focusability of the popup window. When focusable, the window
	 * will grab the focus from the current focused widget if the popup contains
	 * a focusable {@link android.view.View}. By default a popup window is not
	 * focusable.
	 * </p>
	 * 
	 * <p>
	 * If the popup is showing, calling this method will take effect only the
	 * next time the popup is shown or through a manual call to one of the
	 * {@link #update()} methods.
	 * </p>
	 * 
	 * @param focusable
	 *            true if the popup should grab focus, false otherwise.
	 * 
	 * @see #isFocusable()
	 * @see #isShowing()
	 * @see #update()
	 */
	public void setFocusable(boolean focusable) {
		mFocusable = focusable;
	}

	/**
	 * Return the current value in {@link #setInputMethodMode(int)}.
	 * 
	 * @see #setInputMethodMode(int)
	 */
	public int getInputMethodMode() {
		return mInputMethodMode;

	}

	/**
	 * Control how the popup operates with an input method: one of
	 * {@link #INPUT_METHOD_FROM_FOCUSABLE}, {@link #INPUT_METHOD_NEEDED}, or
	 * {@link #INPUT_METHOD_NOT_NEEDED}.
	 * 
	 * <p>
	 * If the popup is showing, calling this method will take effect only the
	 * next time the popup is shown or through a manual call to one of the
	 * {@link #update()} methods.
	 * </p>
	 * 
	 * @see #getInputMethodMode()
	 * @see #update()
	 */
	public void setInputMethodMode(int mode) {
		mInputMethodMode = mode;
	}

	/**
	 * Sets the operating mode for the soft input area.
	 * 
	 * @param mode
	 *            The desired mode, see
	 *            {@link android.view.WindowManager.LayoutParams#softInputMode}
	 *            for the full list
	 * 
	 * @see android.view.WindowManager.LayoutParams#softInputMode
	 * @see #getSoftInputMode()
	 */
	public void setSoftInputMode(int mode) {
		mSoftInputMode = mode;
	}

	/**
	 * Returns the current value in {@link #setSoftInputMode(int)}.
	 * 
	 * @see #setSoftInputMode(int)
	 * @see android.view.WindowManager.LayoutParams#softInputMode
	 */
	public int getSoftInputMode() {
		return mSoftInputMode;
	}

	/**
	 * <p>
	 * Indicates whether the popup window receives touch events.
	 * </p>
	 * 
	 * @return true if the popup is touchable, false otherwise
	 * 
	 * @see #setTouchable(boolean)
	 */
	public boolean isTouchable() {
		return mTouchable;
	}

	/**
	 * <p>
	 * Changes the touchability of the popup window. When touchable, the window
	 * will receive touch events, otherwise touch events will go to the window
	 * below it. By default the window is touchable.
	 * </p>
	 * 
	 * <p>
	 * If the popup is showing, calling this method will take effect only the
	 * next time the popup is shown or through a manual call to one of the
	 * {@link #update()} methods.
	 * </p>
	 * 
	 * @param touchable
	 *            true if the popup should receive touch events, false otherwise
	 * 
	 * @see #isTouchable()
	 * @see #isShowing()
	 * @see #update()
	 */
	public void setTouchable(boolean touchable) {
		mTouchable = touchable;
	}

	/**
	 * <p>
	 * Indicates whether the popup window will be informed of touch events
	 * outside of its window.
	 * </p>
	 * 
	 * @return true if the popup is outside touchable, false otherwise
	 * 
	 * @see #setOutsideTouchable(boolean)
	 */
	public boolean isOutsideTouchable() {
		return mOutsideTouchable;
	}

	/**
	 * <p>
	 * Controls whether the pop-up will be informed of touch events outside of
	 * its window. This only makes sense for pop-ups that are touchable but not
	 * focusable, which means touches outside of the window will be delivered to
	 * the window behind. The default is false.
	 * </p>
	 * 
	 * <p>
	 * If the popup is showing, calling this method will take effect only the
	 * next time the popup is shown or through a manual call to one of the
	 * {@link #update()} methods.
	 * </p>
	 * 
	 * @param touchable
	 *            true if the popup should receive outside touch events, false
	 *            otherwise
	 * 
	 * @see #isOutsideTouchable()
	 * @see #isShowing()
	 * @see #update()
	 */
	public void setOutsideTouchable(boolean touchable) {
		mOutsideTouchable = touchable;
	}

	/**
	 * <p>
	 * Indicates whether clipping of the popup window is enabled.
	 * </p>
	 * 
	 * @return true if the clipping is enabled, false otherwise
	 * 
	 * @see #setClippingEnabled(boolean)
	 */
	public boolean isClippingEnabled() {
		return mClippingEnabled;
	}

	/**
	 * <p>
	 * Allows the popup window to extend beyond the bounds of the screen. By
	 * default the window is clipped to the screen boundaries. Setting this to
	 * false will allow windows to be accurately positioned.
	 * </p>
	 * 
	 * <p>
	 * If the popup is showing, calling this method will take effect only the
	 * next time the popup is shown or through a manual call to one of the
	 * {@link #update()} methods.
	 * </p>
	 * 
	 * @param enabled
	 *            false if the window should be allowed to extend outside of the
	 *            screen
	 * @see #isShowing()
	 * @see #isClippingEnabled()
	 * @see #update()
	 */
	public void setClippingEnabled(boolean enabled) {
		mClippingEnabled = enabled;
	}

	/**
	 * <p>
	 * Change the width and height measure specs that are given to the window
	 * manager by the popup. By default these are 0, meaning that the current
	 * width or height is requested as an explicit size from the window manager.
	 * You can supply {@link ViewGroup.LayoutParams#WRAP_CONTENT} or
	 * {@link ViewGroup.LayoutParams#FILL_PARENT} to have that measure spec
	 * supplied instead, replacing the absolute width and height that has been
	 * set in the popup.
	 * </p>
	 * 
	 * <p>
	 * If the popup is showing, calling this method will take effect only the
	 * next time the popup is shown.
	 * </p>
	 * 
	 * @param widthSpec
	 *            an explicit width measure spec mode, either
	 *            {@link ViewGroup.LayoutParams#WRAP_CONTENT},
	 *            {@link ViewGroup.LayoutParams#FILL_PARENT}, or 0 to use the
	 *            absolute width.
	 * @param heightSpec
	 *            an explicit height measure spec mode, either
	 *            {@link ViewGroup.LayoutParams#WRAP_CONTENT},
	 *            {@link ViewGroup.LayoutParams#FILL_PARENT}, or 0 to use the
	 *            absolute height.
	 */
	public void setWindowLayoutMode(int widthSpec, int heightSpec) {
		mWidthMode = widthSpec;
		mHeightMode = heightSpec;
	}

	/**
	 * <p>
	 * Return this popup's height MeasureSpec
	 * </p>
	 * 
	 * @return the height MeasureSpec of the popup
	 * 
	 * @see #setHeight(int)
	 */
	public int getHeight() {
		return mHeight;
	}

	/**
	 * <p>
	 * Change the popup's height MeasureSpec
	 * </p>
	 * 
	 * <p>
	 * If the popup is showing, calling this method will take effect only the
	 * next time the popup is shown.
	 * </p>
	 * 
	 * @param height
	 *            the height MeasureSpec of the popup
	 * 
	 * @see #getHeight()
	 * @see #isShowing()
	 */
	public void setHeight(int height) {
		mHeight = height;
	}

	/**
	 * <p>
	 * Return this popup's width MeasureSpec
	 * </p>
	 * 
	 * @return the width MeasureSpec of the popup
	 * 
	 * @see #setWidth(int)
	 */
	public int getWidth() {
		return mWidth;
	}

	/**
	 * <p>
	 * Change the popup's width MeasureSpec
	 * </p>
	 * 
	 * <p>
	 * If the popup is showing, calling this method will take effect only the
	 * next time the popup is shown.
	 * </p>
	 * 
	 * @param width
	 *            the width MeasureSpec of the popup
	 * 
	 * @see #getWidth()
	 * @see #isShowing()
	 */
	public void setWidth(int width) {
		mWidth = width;
	}

	/**
	 * <p>
	 * Indicate whether this popup window is showing on screen.
	 * </p>
	 * 
	 * @return true if the popup is showing, false otherwise
	 */
	public boolean isShowing() {
		return mIsShowing;
	}

	/**
	 * <p>
	 * Display the content view in a popup window at the specified location. If
	 * the popup window cannot fit on screen, it will be clipped. See
	 * {@link android.view.WindowManager.LayoutParams} for more information on
	 * how gravity and the x and y parameters are related. Specifying a gravity
	 * of {@link android.view.Gravity#NO_GRAVITY} is similar to specifying
	 * <code>Gravity.LEFT | Gravity.TOP</code>.
	 * </p>
	 * 
	 * @param parent
	 *            a parent view to get the
	 *            {@link android.view.View#getWindowToken()} token from
	 * @param gravity
	 *            the gravity which controls the placement of the popup window
	 * @param x
	 *            the popup's x location offset
	 * @param y
	 *            the popup's y location offset
	 */
	public void showAtLocation(View parent, int gravity, int x, int y) {
		if (isShowing() || mContentView == null) {
			return;
		}

		mIsShowing = true;
		mIsDropdown = false;

		WindowManager.LayoutParams p = createPopupLayout(parent.getWindowToken());
		p.windowAnimations = computeAnimationResource();

		preparePopup(p);
		if (gravity == Gravity.NO_GRAVITY) {
			gravity = Gravity.TOP | Gravity.LEFT;
		}
		p.gravity = gravity;
		p.x = x;
		p.y = y;
		invokePopup(p);
	}

	/**
	 * <p>
	 * Display the content view in a popup window anchored to the bottom-left
	 * corner of the anchor view. If there is not enough room on screen to show
	 * the popup in its entirety, this method tries to find a parent scroll view
	 * to scroll. If no parent scroll view can be scrolled, the bottom-left
	 * corner of the popup is pinned at the top left corner of the anchor view.
	 * </p>
	 * 
	 * @param anchor
	 *            the view on which to pin the popup window
	 * 
	 * @see #dismiss()
	 */
	public void showAsDropDown(View anchor) {
		showAsDropDown(anchor, 0, 0);
	}

	/**
	 * <p>
	 * Display the content view in a popup window anchored to the bottom-left
	 * corner of the anchor view offset by the specified x and y coordinates. If
	 * there is not enough room on screen to show the popup in its entirety,
	 * this method tries to find a parent scroll view to scroll. If no parent
	 * scroll view can be scrolled, the bottom-left corner of the popup is
	 * pinned at the top left corner of the anchor view.
	 * </p>
	 * <p>
	 * If the view later scrolls to move <code>anchor</code> to a different
	 * location, the popup will be moved correspondingly.
	 * </p>
	 * 
	 * @param anchor
	 *            the view on which to pin the popup window
	 * 
	 * @see #dismiss()
	 */
	public void showAsDropDown(View anchor, int xoff, int yoff) {
		if (isShowing() || mContentView == null) {
			return;
		}

		mIsShowing = true;
		mIsDropdown = true;

		WindowManager.LayoutParams p = createPopupLayout(anchor.getWindowToken());
		preparePopup(p);
		mAboveAnchor = findDropDownPosition(anchor, p, xoff, yoff);

		if (mHeightMode < 0)
			p.height = mHeightMode;
		if (mWidthMode < 0)
			p.width = mWidthMode;

		p.windowAnimations = computeAnimationResource();

		invokePopup(p);
	}

	/**
	 * Indicates whether the popup is showing above (the y coordinate of the
	 * popup's bottom is less than the y coordinate of the anchor) or below the
	 * anchor view (the y coordinate of the popup is greater than y coordinate
	 * of the anchor's bottom).
	 * 
	 * The value returned by this method is meaningful only after
	 * {@link #showAsDropDown(android.view.View)} or
	 * {@link #showAsDropDown(android.view.View, int, int)} was invoked.
	 * 
	 * @return True if this popup is showing above the anchor view, false
	 *         otherwise.
	 */
	public boolean isAboveAnchor() {
		return mAboveAnchor;
	}

	/**
	 * <p>
	 * Prepare the popup by embedding in into a new ViewGroup if the background
	 * drawable is not null. If embedding is required, the layout parameters'
	 * height is mnodified to take into account the background's padding.
	 * </p>
	 * 
	 * @param p
	 *            the layout parameters of the popup's content view
	 */
	private void preparePopup(WindowManager.LayoutParams p) {
		if (mContentView == null || mContext == null || mWindowManager == null) {
			throw new IllegalStateException("You must specify a valid content view by "
					+ "calling setContentView() before attempting to show the popup.");
		}
		mPopupView = mContentView;
		mPopupWidth = p.width;
		mPopupHeight = p.height;
	}

	/**
	 * <p>
	 * Invoke the popup window by adding the content view to the window manager.
	 * </p>
	 * 
	 * <p>
	 * The content view must be non-null when this method is invoked.
	 * </p>
	 * 
	 * @param p
	 *            the layout parameters of the popup's content view
	 */
	private void invokePopup(WindowManager.LayoutParams p) {
		p.packageName = mContext.getPackageName();
		mWindowManager.addView(mPopupView, p);
	}

	/**
	 * <p>
	 * Generate the layout parameters for the popup window.
	 * </p>
	 * 
	 * @param token
	 *            the window token used to bind the popup's window
	 * 
	 * @return the layout parameters to pass to the window manager
	 */
	private WindowManager.LayoutParams createPopupLayout(IBinder token) {
		// generates the layout parameters for the drop down
		// we want a fixed size view located at the bottom left of the anchor
		WindowManager.LayoutParams p = new WindowManager.LayoutParams();
		// these gravity settings put the view at the top left corner of the
		// screen. The view is then positioned to the appropriate location
		// by setting the x and y offsets to match the anchor's bottom
		// left corner
		p.gravity = Gravity.LEFT | Gravity.TOP;
		p.width = mWidth;
		p.height = mHeight;
		p.format = PixelFormat.TRANSLUCENT;
		p.flags = computeFlags(p.flags);
		p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
		p.token = token;
		p.softInputMode = mSoftInputMode;
		p.setTitle("PopupView:" + Integer.toHexString(hashCode()));

		return p;
	}

	private int computeFlags(int curFlags) {
		curFlags &= ~(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		if (mIgnoreCheekPress) {
			curFlags |= WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;
		}
		if (!mFocusable) {
			curFlags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			if (mInputMethodMode == INPUT_METHOD_NEEDED) {
				curFlags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
			}
		} else if (mInputMethodMode == INPUT_METHOD_NOT_NEEDED) {
			curFlags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
		}
		if (!mTouchable) {
			curFlags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		}
		if (mOutsideTouchable) {
			curFlags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		}
		if (!mClippingEnabled) {
			curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		}
		return curFlags;
	}

	private int computeAnimationResource() {
		if (mAnimationStyle == -1) {
			if (mIsDropdown) {
				return mAboveAnchor ? R.style.Animation_DropDownUp : R.style.Animation_DropDownDown;
			}
			return 0;
		}
		return mAnimationStyle;
	}

	/**
	 * <p>
	 * Positions the popup window on screen. When the popup window is too tall
	 * to fit under the anchor, a parent scroll view is seeked and scrolled up
	 * to reclaim space. If scrolling is not possible or not enough, the popup
	 * window gets moved on top of the anchor.
	 * </p>
	 * 
	 * <p>
	 * The height must have been set on the layout parameters prior to calling
	 * this method.
	 * </p>
	 * 
	 * @param anchor
	 *            the view on which the popup window must be anchored
	 * @param p
	 *            the layout parameters used to display the drop down
	 * 
	 * @return true if the popup is translated upwards to fit on screen
	 */
	private boolean findDropDownPosition(View anchor, WindowManager.LayoutParams p, int xoff, int yoff) {

		anchor.getLocationInWindow(mDrawingLocation);
		p.x = mDrawingLocation[0] + xoff;
		p.y = mDrawingLocation[1] + anchor.getMeasuredHeight() + yoff;

		boolean onTop = false;

		p.gravity = Gravity.LEFT | Gravity.TOP;

		anchor.getLocationOnScreen(mScreenLocation);
		final Rect displayFrame = new Rect();
		anchor.getWindowVisibleDisplayFrame(displayFrame);

		final View root = anchor.getRootView();
		if (p.y + mPopupHeight > displayFrame.bottom || p.x + mPopupWidth - root.getWidth() > 0) {
			// if the drop down disappears at the bottom of the screen. we try
			// to
			// scroll a parent scrollview or move the drop down back up on top
			// of
			// the edit box
			int scrollX = anchor.getScrollX();
			int scrollY = anchor.getScrollY();
			Rect r = new Rect(scrollX, scrollY, scrollX + mPopupWidth, scrollY + mPopupHeight
					+ anchor.getMeasuredHeight());
			anchor.requestRectangleOnScreen(r, true);

			// now we re-evaluate the space available, and decide from that
			// whether the pop-up will go above or below the anchor.
			anchor.getLocationInWindow(mDrawingLocation);
			p.x = mDrawingLocation[0] + xoff;
			p.y = mDrawingLocation[1] + anchor.getMeasuredHeight() + yoff;

			// determine whether there is more space above or below the anchor
			anchor.getLocationOnScreen(mScreenLocation);

			onTop = (displayFrame.bottom - mScreenLocation[1] - anchor.getMeasuredHeight() - yoff) < (mScreenLocation[1]
					- yoff - displayFrame.top);
			if (onTop) {
				p.gravity = Gravity.LEFT | Gravity.BOTTOM;
				p.y = root.getHeight() - mDrawingLocation[1] + yoff;
			} else {
				p.y = mDrawingLocation[1] + anchor.getMeasuredHeight() + yoff;
			}
		}

		p.gravity |= Gravity.DISPLAY_CLIP_VERTICAL;

		return onTop;
	}

	/**
	 * <p>
	 * Dispose of the popup window. This method can be invoked only after
	 * {@link #showAsDropDown(android.view.View)} has been executed. Failing
	 * that, calling this method will have no effect.
	 * </p>
	 * 
	 * @see #showAsDropDown(android.view.View)
	 */
	public void dismiss() {
		if (isShowing() && mPopupView != null) {
			mWindowManager.removeView(mPopupView);
			if (mPopupView != mContentView && mPopupView instanceof ViewGroup) {
				((ViewGroup) mPopupView).removeView(mContentView);
			}
			mPopupView = null;
			mIsShowing = false;

			if (mOnDismissListener != null) {
				mOnDismissListener.onDismiss();
			}
		}
	}

	/**
	 * Sets the listener to be called when the window is dismissed.
	 * 
	 * @param onDismissListener
	 *            The listener.
	 */
	public void setOnDismissListener(OnDismissListener onDismissListener) {
		mOnDismissListener = onDismissListener;
	}

	/**
	 * Listener that is called when this popup window is dismissed.
	 */
	public interface OnDismissListener {
		/**
		 * Called when this popup window is dismissed.
		 */
		public void onDismiss();
	}

}
