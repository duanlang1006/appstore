package com.android.applite.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import com.android.applite.model.IAppInfo;
import com.android.applite.plugin.IAppLiteOperator;
import com.applite.android.R;

public class CustomizedPagedViewIcon extends FrameLayout implements View.OnClickListener{
	private PagedViewIcon icon;
//	private TextView mPercent ;
//	private RelativeLayout relativeLayout ;
//	private ImageView mWifiView ;
//	private ProgressBar mProgressBar;
//	private ImageView mPause;
	private Button deleteButton;
	private IAppLiteOperator mOperator;
	public CustomizedPagedViewIcon(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public CustomizedPagedViewIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	
	public CustomizedPagedViewIcon(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public void initView() {
		icon=(PagedViewIcon) findViewById(R.id.application_icon);
//		relativeLayout = (RelativeLayout)findViewById(R.id.relative_app_icon);
//		mWifiView = (ImageView)findViewById(R.id.image_wifi_item_id);
//		mProgressBar = (ProgressBar)findViewById(R.id.progressbar_download_id);
//		mPause = (ImageView)findViewById(R.id.image_pause_id);
		deleteButton=(Button) findViewById(R.id.app_delete);
		
		deleteButton.setOnClickListener(this);
	}
	public void applyFromApplicationInfo(IAppInfo info, boolean scaleUp/*,HolographicOutlineHelper holoOutlineHelper*/,boolean removeFlag) {
		icon.applyFromApplicationInfo(info, scaleUp);
		if (removeFlag && info.getItemType() != IAppInfo.AppOnline
		        && info.getItemType() != IAppInfo.AppMore) {
			deleteButton.setVisibility(Button.VISIBLE);
		} else  {
			deleteButton.setVisibility(Button.GONE);
		}
//		if (info.getItemType() == ItemType.AppOffline) {
//			relativeLayout.setVisibility(RelativeLayout.VISIBLE);
//		} else {
//			relativeLayout.setVisibility(RelativeLayout.GONE);
//		}
		deleteButton.setTag(info);
	}
	
	public IAppLiteOperator getOperator() {
		return mOperator;
	}

	public void setOperator(IAppLiteOperator mOperator) {
		this.mOperator = mOperator;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		final IAppInfo appInfo = (IAppInfo) v.getTag();
		if (v.getId()==R.id.app_delete) {
		    mOperator.onRemoveAppClick(appInfo);
		}
	}
}
