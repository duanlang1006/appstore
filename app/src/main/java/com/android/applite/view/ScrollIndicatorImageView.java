package com.android.applite.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ScrollIndicatorImageView extends ImageView {
	private int mTranslationX = 0;
	
	public ScrollIndicatorImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ScrollIndicatorImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ScrollIndicatorImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean setFrame(int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		return super.setFrame(l+mTranslationX, t, r+mTranslationX, b);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	}

	public void setTranslationX (int translationX){
		mTranslationX = translationX;
		offsetLeftAndRight(mTranslationX-getLeft());
	}
}
