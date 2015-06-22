/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.applite.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.TextView;

import com.android.applite.model.IAppInfo;
import com.applite.android.R;

import java.util.Observable;
import java.util.Observer;

/**
 * An icon on a PagedView, specifically for items in the launcher's paged view
 * (with compound drawables on the top).
 */
public class PagedViewIcon extends TextView implements Checkable,Observer {
	private static final String TAG = "PagedViewIcon";

	// holographic outline
	private Bitmap mHolographicOutline;
	private Bitmap mIcon;
	private int mAlpha = 255;
	private int mHolographicAlpha;
	
	private boolean mIsChecked;
	private float mCheckedAlpha = 1.0f;
	private int mCheckedFadeInDuration;
	private int mCheckedFadeOutDuration;
	private IAppInfo info;
	final Resources r;
	HolographicPagedViewIcon mHolographicOutlineView;
//	private HolographicOutlineHelper mHolographicOutlineHelper;
	protected PaintFlagsDrawFilter pfd = null;
    
    private Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            info=(IAppInfo) getTag();
            if (mIcon!=info.getIcon()) {
                mIcon = info.getIcon();
                setCompoundDrawablesWithIntrinsicBounds(null,new FastBitmapDrawable(mIcon), null, null);
            }
            setText(info.getTitle());
            invalidate();
        }
    };
    
    
    public PagedViewIcon(Context context) {
		this(context, null);
	}

	public PagedViewIcon(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagedViewIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// Set up fade in/out constants
		r = context.getResources();
		final int alpha = r
				.getInteger(R.integer.config_dragAppsCustomizeIconFadeAlpha);
		if (alpha > 0) {
			mCheckedAlpha = r
					.getInteger(R.integer.config_dragAppsCustomizeIconFadeAlpha) / 256.0f;
			mCheckedFadeInDuration = r
					.getInteger(R.integer.config_dragAppsCustomizeIconFadeInDuration);
			mCheckedFadeOutDuration = r
					.getInteger(R.integer.config_dragAppsCustomizeIconFadeOutDuration);
		}
		mHolographicOutlineView = new HolographicPagedViewIcon(context, this);
		if (pfd == null){
            pfd = new PaintFlagsDrawFilter(0,  0 | Paint.FILTER_BITMAP_FLAG| Paint.DITHER_FLAG);
        }
	}

	protected HolographicPagedViewIcon getHolographicOutlineView() {
		return mHolographicOutlineView;
	}

	protected Bitmap getHolographicOutline() {
		return mHolographicOutline;
	}
	public void applyFromApplicationInfo(IAppInfo info, boolean scaleUp) {
		this.info=info;
//		mHolographicOutlineHelper = holoOutlineHelper;
		mIcon=info.getIcon();
		setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mIcon), null, null);
		setText(info.getTitle());
		setTag(info);
		

		//注册观察者
		info.unregisterObserver(this);
		info.registerObserver(this);
	}

	public void setHolographicOutline(Bitmap holoOutline) {
		mHolographicOutline = holoOutline;
		getHolographicOutlineView().invalidate();
	}

/*	@Override
	protected void onDraw(Canvas canvas) {
		if (mAlpha > 0) {
			super.onDraw(canvas);
		}
		Bitmap overlay = null;

		// draw any blended overlays
		if (mCheckedOutline != null) {
			mPaint.setAlpha(255);
			overlay = mCheckedOutline;
		}
		if (overlay != null) {
			final int offset = getScrollX();
			final int compoundPaddingLeft = getCompoundPaddingLeft();
			final int compoundPaddingRight = getCompoundPaddingRight();
			int hspace = getWidth() - compoundPaddingRight
					- compoundPaddingLeft;
			canvas.drawBitmap(overlay, offset + compoundPaddingLeft
					+ (hspace - overlay.getWidth()) / 2, mPaddingTop, mPaint);
		}  canvas.drawBitmap(sMaskBitmap,(mWidth-size[0])/2,0, mPaint);
	}*/
	
    @Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
	    super.onDraw(canvas);
	    canvas.setDrawFilter(pfd);
	    int left = (getWidth()-getCompoundPaddingLeft()-getCompoundPaddingRight() - mIcon.getWidth()) / 2;
        int top = getPaddingTop();
        int right = left + mIcon.getWidth();
        int bottom = top + mIcon.getHeight();
        info.drawIcon(canvas, left, top, right, bottom);
	}

	@Override
	public boolean isChecked() {
		return mIsChecked;
	}

	void setChecked(boolean checked, boolean animate) {
		if (mIsChecked != checked) {
			mIsChecked = checked;

			float alpha;
			int duration;
			if (mIsChecked) {
				alpha = mCheckedAlpha;
				duration = mCheckedFadeInDuration;
			} else {
				alpha = 1.0f;
				duration = mCheckedFadeOutDuration;
			}
			invalidate();
		}
	}

	@Override
	public void setChecked(boolean checked) {
		setChecked(checked, true);
	}

	@Override
	public void toggle() {
		setChecked(!mIsChecked);
	}

	public void setInfo(IAppInfo info) {
		this.info = info;
	}
	
	@Override
    public void update(Observable observable, Object data) {
        // TODO Auto-generated method stub
	    this.removeCallbacks(mUpdateRunnable);
        this.postDelayed(mUpdateRunnable, 100);
    }

//    public void update(boolean visible) {
//		info=(IAppInfo) getTag();
//		if (mIcon!=info.getIcon()) {
//			setCompoundDrawablesWithIntrinsicBounds(null,new FastBitmapDrawable(info.getIcon()), null, null);
//		}
////		if (IAppInfo.STATUS_PRIVATE_INSTALLING.equals(info.getInstallStatus())) {
////			setText(R.string.installing);
////			setTextColor(Color.RED);
////		}
////		if (info.getItemType()==IAppInfo.AppInstalled) {
//		setCompoundDrawablesWithIntrinsicBounds(null,new FastBitmapDrawable(info.getIcon()), null, null);
//		setText(info.getTitle());
//		setTextColor(Color.BLACK);
////		}
//		if (visible) invalidate();
//	}
}
