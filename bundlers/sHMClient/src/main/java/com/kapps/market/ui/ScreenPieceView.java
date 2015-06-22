package com.kapps.market.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * �Զ����ͼ���ƣ�֧�ֺ���������<br>
 * 2010-10-12
 * 
 * @author admin
 * 
 */
public class ScreenPieceView extends View {

	private Drawable drawable;

	public ScreenPieceView(Context context) {
		super(context);
	}

	public void setImageDrawable(Drawable drawable) {
		this.drawable = drawable;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (drawable == null) {
			return;
		}

		// ע�⻭����״̬����ͻָ�
		canvas.save();
		// ����ֱ�Ϊ����ת�Ƕȣ�ͼƬX���ģ�ͼƬY���ġ�
		int w = getWidth();
		int h = getHeight();
		if (drawable.getIntrinsicWidth() > drawable.getIntrinsicHeight()) {
			int offset = h - w;
			canvas.rotate(90, w / 2, h / 2);
			drawable.setBounds(-offset / 2 + getPaddingTop(), offset / 2 + getPaddingLeft(), h - offset / 2
					- getPaddingBottom(), w + offset / 2 - getPaddingRight());

		} else {
			drawable.setBounds(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
		}

		drawable.draw(canvas);
		canvas.restore();
	}

	/**
	 * @return the drawable
	 */
	public Drawable getDrawable() {
		return drawable;
	}

}
