package com.kapps.market.ui;

import com.kapps.market.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 2010-8-10 <br>
 * ����⻹�н��������
 * 
 * @author Administrator
 * 
 */
public class ViewProgressNote extends FrameLayout {

	public static final String TAG = "ViewProgressBand";

	private ProgressBar pb;

	/**
	 * @param context
	 * @param attrs
	 */
	public ViewProgressNote(Context context, AttributeSet attrs) {
		super(context, attrs);
		// ����
		TextView textView = new TextView(context);
		textView.setGravity(Gravity.CENTER);
		textView.setTextColor(context.getResources().getColor(R.color.app_info_title_text_color));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				getResources().getDimensionPixelOffset(R.dimen.app_info_title_font_size));
		textView.setId(494949);
		int titleResId = attrs.getAttributeResourceValue(null, "title", R.string.unknow);
		textView.setText(getResources().getString(titleResId));
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER | Gravity.CENTER_VERTICAL;
		addView(textView, layoutParams);

		pb = (ProgressBar) LayoutInflater.from(context).inflate(R.layout.b_progress, null);
		pb.setVisibility(View.INVISIBLE);
		layoutParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.rightMargin = 10;
		layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		addView(pb, layoutParams);
	}

	/**
	 * ��ʾ���
	 */
	public void showProgressBar() {
		pb.setVisibility(View.VISIBLE);
	}

	/**
	 * ���ؽ��
	 */
	public void hideProgressBar() {
		pb.setVisibility(View.INVISIBLE);
	}

}
