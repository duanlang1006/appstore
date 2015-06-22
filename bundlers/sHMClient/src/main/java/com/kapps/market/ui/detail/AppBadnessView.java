package com.kapps.market.ui.detail;

import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.kapps.market.R;
import com.kapps.market.bean.AppBadness;
import com.kapps.market.bean.AppItem;
import com.kapps.market.ui.CommonView;
import com.kapps.market.util.Constants;
import com.kapps.market.util.ResourceEnum;
import com.kapps.market.util.Util;

/**
 * Ӧ�õ�Σ������
 * 
 * @author admin
 * 
 */
public class AppBadnessView extends CommonView implements OnCheckedChangeListener, OnClickListener {

	public static final String TAG = "AppBadnessView";
	private AppItem appItem;
	private RadioGroup radioGroup;

	/**
	 * @param context
	 */
	public AppBadnessView(Context context, AppItem appItem) {
		super(context);

		addView(R.layout.app_badness);

		this.appItem = appItem;

		radioGroup = (RadioGroup) findViewById(R.id.badnessRadioGroup);
		radioGroup.setOnCheckedChangeListener(this);

		findViewById(R.id.commitBadnessButton).setOnClickListener(this);
		findViewById(R.id.cancelBadnessButton).setOnClickListener(this);

		initView(appItem);
	}

	public void initView(AppItem appItem) {
		if (appItem.getAppBadness() != null) {
			AppBadness appBadness = appItem.getAppBadness();
			switch (appBadness.getIndex()) {
			case 1:
				radioGroup.check(R.id.eroticism);
				break;
			case 2:
				radioGroup.check(R.id.violence);
				break;
			case 3:
				radioGroup.check(R.id.rebarbative);
				break;
			case 4:
				radioGroup.check(R.id.deleterious);
				break;
			case 5:
				radioGroup.check(R.id.otherReason);
				TextView badnessContentFiled = (TextView) findViewById(R.id.badnessContentField);
				badnessContentFiled.setText(appBadness.getContent());
				break;
			default:
				break;
			}
			Button button = (Button) findViewById(R.id.commitBadnessButton);
			button.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.RadioGroup.OnCheckedChangeListener#onCheckedChanged(android
	 * .widget.RadioGroup, int)
	 */
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		Button button = (Button) findViewById(R.id.commitBadnessButton);
		if (!button.isEnabled()) {
			button.setEnabled(true);
		}
		TextView badnessContentFiled = (TextView) findViewById(R.id.badnessContentField);
		if (checkedId == R.id.otherReason) {
			badnessContentFiled.setVisibility(View.VISIBLE);
		} else {
			badnessContentFiled.setVisibility(View.GONE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.commitBadnessButton:
			handleCommitBadness();
			break;

		case R.id.cancelBadnessButton:
			handleCancelBadness();

			break;
		default:
			break;
		}
	}

	private void handleCommitBadness() {
		TextView contentField = (TextView) findViewById(R.id.badnessContentField);
		int viewId = radioGroup.getCheckedRadioButtonId();
		String content = contentField.getText().toString().trim();
		if (viewId == R.id.otherReason && content.length() == 0) {
			Toast.makeText(getContext(), getResources().getString(R.string.please_fill_objection), 150).show();
			return;
		}

		Util.hideInputMethodWindow(getContext(), getFocusedChild());
		AppBadness appBadness = new AppBadness();
		appBadness.setAppId(appItem.getId());
		switch (viewId) {
		case R.id.eroticism:
			appBadness.setIndex(ResourceEnum.RE_EROTICISM);
			break;
		case R.id.violence:
			appBadness.setIndex(ResourceEnum.RE_VIOLENCE);
			break;
		case R.id.rebarbative:
			appBadness.setIndex(ResourceEnum.RE_REBARBATIVE);
			break;
		case R.id.deleterious:
			appBadness.setIndex(ResourceEnum.RE_DELETERIOUS);
			break;
		case R.id.otherReason:
			appBadness.setIndex(ResourceEnum.RE_OTHER_REASON);
			appBadness.setContent(content);
			break;
		default:
			break;
		}

		AppBadness oldBadness = appItem.getAppBadness();
		if (oldBadness != null && oldBadness.equals(appBadness)) {
			Toast.makeText(getContext(), getResources().getString(R.string.submit_same_report), 200).show();
			return;
		}

		radioGroup.clearCheck();
		Button button = (Button) findViewById(R.id.commitBadnessButton);
		button.setEnabled(false);
		contentField.setText("");

		// ֪ͨ�ύ�ٱ�
		Message message = Message.obtain();
		message.what = Constants.M_BADNESS_Ok;
		message.obj = appBadness;
		notifyMessageToParent(message);
	}

	private void handleCancelBadness() {
		radioGroup.clearCheck();
		Button button = (Button) findViewById(R.id.commitBadnessButton);
		TextView badnessContentFiled = (TextView) findViewById(R.id.badnessContentField);
		button.setEnabled(false);
		badnessContentFiled.setText("");
		// ȡ���ύ�ٱ�
		Message message = Message.obtain();
		message.what = Constants.M_BADNESS_CANCEL;
		notifyMessageToParent(message);
	}

}
