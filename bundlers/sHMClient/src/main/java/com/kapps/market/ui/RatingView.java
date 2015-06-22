package com.kapps.market.ui;

import com.kapps.market.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 2010-8-16 <br>
 * Ŀǰ��5�Ǽ� ������������
 * 
 * @author admin
 * 
 */
public class RatingView extends TextView {

	private int rating;

	/**
	 * @param context
	 */
	public RatingView(Context context) {
		super(context);
	}

	public RatingView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// ���ó�ʼ����ֵ
		rating = attrs.getAttributeIntValue(null, "rating", 0);
		setBackgroundResource(getDrawableId(rating));
	}

	/**
	 * �����Ǽ�
	 * 
	 * @param aRate
	 *            ���� 0-10������
	 */
	public void setRating(int aRate) {
		rating = aRate;
		setBackgroundResource(getDrawableId(aRate));
	}

	private int getDrawableId(int rate) {
		switch (rate) {
		case 0:
			return R.drawable.rating_bg;
		case 1:
			return R.drawable.rating_bg_1;
		case 2:
			return R.drawable.rating_bg_2;
		case 3:
			return R.drawable.rating_bg_3;
		case 4:
			return R.drawable.rating_bg_4;
		case 5:
			return R.drawable.rating_bg_5;
		case 6:
			return R.drawable.rating_bg_6;
		case 7:
			return R.drawable.rating_bg_7;
		case 8:
			return R.drawable.rating_bg_8;
		case 9:
			return R.drawable.rating_bg_9;
		case 10:
			return R.drawable.rating_full_bg;
		default:
			return R.drawable.rating_bg;
		}
	}

	/**
	 * @return the rating
	 */
	public int getRating() {
		return rating;
	}
}
