package com.kapps.market.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.kapps.market.util.Constants;

/**
 * �Զ����ͼ���ƣ���ԭ�е�ͼƬ���ٻ�һ��<br>
 * 2010-10-12
 * 
 * @author admin
 * 
 */
public class LayerPieceView extends ImageView {

	public static final String TAG = "LayerPieceView";

	// ��ͼƬ
	private Drawable layerDrawable;
	private Drawable pDrawable;

	public LayerPieceView(Context context, AttributeSet attrs) {
		super(context, attrs);

		int layerRes = attrs.getAttributeResourceValue(null, "layerRes", Constants.NONE_ID);
		if (layerRes != Constants.NONE_ID) {
			layerDrawable = getResources().getDrawable(layerRes);
		}
		layerRes = attrs.getAttributeResourceValue(null, "pLayerRes", Constants.NONE_ID);
		if (layerRes != Constants.NONE_ID) {
			pDrawable = getResources().getDrawable(layerRes);
		}
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (layerDrawable == null || getDrawable() == null) {
			return;
		}

		// ��Ҫ���Ƶ�����
		Drawable needDrawable = null;
		if (isPressed() && pDrawable != null) {
			needDrawable = pDrawable;
		} else {
			needDrawable = layerDrawable;
		}

		if (getDrawable() != null) {
			needDrawable.setBounds(getDrawable().getBounds());

		} else {
			int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
			int vheight = getHeight() - getPaddingTop() - getPaddingBottom();
			needDrawable.setBounds(getPaddingLeft(), getPaddingTop(), vwidth, vheight);
		}

		if (getPaddingTop() == 0 && getPaddingLeft() == 0) {
			needDrawable.draw(canvas);

		} else {
			int saveCount = canvas.getSaveCount();
			canvas.save();
			canvas.translate(getPaddingLeft(), getPaddingTop());
			needDrawable.draw(canvas);
			canvas.restoreToCount(saveCount);
		}

	}

	/**
	 * @return the layer
	 */
	public Drawable getLayerDrawable() {
		return layerDrawable;
	}

	/**
	 * @return the pDrawable
	 */
	public Drawable getpDrawable() {
		return pDrawable;
	}

	/**
	 * �ͷ���Դ
	 */
	public void recycle() {
		if (layerDrawable instanceof BitmapDrawable) {
			((BitmapDrawable) layerDrawable).getBitmap().recycle();
		}
		if (pDrawable instanceof BitmapDrawable) {
			((BitmapDrawable) pDrawable).getBitmap().recycle();
		}
	}
}
