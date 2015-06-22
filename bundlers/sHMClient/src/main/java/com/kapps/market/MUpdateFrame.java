package com.kapps.market;

import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.kapps.market.bean.MarketUpdateInfo;
import com.kapps.market.cache.CacheConstants;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

/**
 * 2010-8-3 <br>
 * 
 * @author admin
 * 
 */
public class MUpdateFrame extends MarketActivity implements OnClickListener {
	public static final String TAG = "MarketUpdateFrame";

	private MarketUpdateInfo marketUpdateInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m_update_page);

		doInitUpdateFrame();
        ((CheckBox)findViewById(R.id.dontnotifyme)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                findViewById(R.id.acceptButton).setEnabled(!b);
            }
        });
		findViewById(R.id.acceptButton).setOnClickListener(this);
		findViewById(R.id.cancelButton).setOnClickListener(this);
	}

	private void doInitUpdateFrame() {
		marketUpdateInfo = (MarketUpdateInfo) marketContext.getLocaleCacheManager().getDataFormCache(
				CacheConstants.MARKET_UPDATE_INFO);
		if (marketUpdateInfo != null) {
			TextView textView = (TextView) findViewById(R.id.versionLabel);
			textView.setText(getString(R.string.version_colon) + marketUpdateInfo.getVersion());

			textView = (TextView) findViewById(R.id.sizeLabel);
			textView.setText(getString(R.string.size_colon) + Util.apkSizeFormat(marketUpdateInfo.getSize(), "KB"));

			textView = (TextView) findViewById(R.id.describeLabel);
			textView.setText(marketUpdateInfo.getDescribe());
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.logoutMenuItem).setVisible(false);
		menu.findItem(R.id.closeMenuItem).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.acceptButton:
			doDownloadNewMarket();
			break;

		case R.id.cancelButton:
            if (((CheckBox)findViewById(R.id.dontnotifyme)).isChecked()) {
                marketContext.getSharedPrefManager().setIgnoredUpdateVersion(marketUpdateInfo.getVersionCode());
            }
			finish();
			break;
		default:
			break;
		}
	}

	private void doDownloadNewMarket() {
		Message message = Message.obtain();
		message.what = Constants.M_QUICK_DOWNLOAD_APP;
		message.obj = marketUpdateInfo;
		marketContext.handleMarketMessage(message);

		// ɾ���ĸ�����Ϣ
		marketContext.getLocaleCacheManager().deleteCacheData(CacheConstants.MARKET_UPDATE_INFO);
		finish();
	}

	@Override
	protected void initCustomActionbar() {
		// TODO Auto-generated method stub
		
	}

}
