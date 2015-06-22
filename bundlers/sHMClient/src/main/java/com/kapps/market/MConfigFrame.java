package com.kapps.market;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kapps.market.bean.config.MarketConfig;

/**
 * 2010-8-2
 * 
 * @author admin
 * 
 */
public class MConfigFrame extends MarketActivity {

	private SharedPreferences configPreferences;

	// �г�����
	private MarketConfig marketConfig;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m_config_page);

		configPreferences = marketContext.getSharedPrefManager().getMarketConfigPref();
		marketConfig = marketContext.getMarketConfig();
		initConfigFrame();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.logoutMenuItem).setVisible(false);
		menu.findItem(R.id.closeMenuItem).setVisible(false);
		menu.findItem(R.id.configMenuItem).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onPause() {
		super.onPause();
		doSaveConfig();
	}

	private void initConfigFrame() {
		// �Ƿ����ͼ��
		View configView = findViewById(R.id.appIconConfigView);
		TextView textView = (TextView) configView.findViewById(R.id.configTitleLabel);
		textView.setText(getString(R.string.whether_load_icon));
		textView = (TextView) configView.findViewById(R.id.configDesLabel);
		textView.setText(getString(R.string.whether_load_icon_note));
		CheckBox checkBox = (CheckBox) configView.findViewById(R.id.configCheckBox);
		checkBox.setChecked(marketConfig.isLoadAppIcon());

		// �Ƿ��Զ����ؽ�ͼ
		configView = findViewById(R.id.appScreenshotConfigView);
		textView = (TextView) configView.findViewById(R.id.configTitleLabel);
		textView.setText(getString(R.string.whether_auto_load_screenshot));
		textView = (TextView) configView.findViewById(R.id.configDesLabel);
		textView.setText(getString(R.string.whether_auto_load_screenshot_note));
		checkBox = (CheckBox) configView.findViewById(R.id.configCheckBox);
		checkBox.setChecked(marketConfig.isLoadAppScreenshot());

		// �Ƿ��Զ�����������
		configView = findViewById(R.id.checkSoftwareUpdateConfigView);
		textView = (TextView) configView.findViewById(R.id.configTitleLabel);
		textView.setText(getString(R.string.whether_check_software_update));
		textView = (TextView) configView.findViewById(R.id.configDesLabel);
		textView.setText(getString(R.string.whether_check_software_update_note));
		checkBox = (CheckBox) configView.findViewById(R.id.configCheckBox);
		checkBox.setChecked(marketConfig.isCheckSoftwareUpdate());

	}

	// ��������
	private void doSaveConfig() {
		Editor editor = configPreferences.edit();

		View configView = findViewById(R.id.appIconConfigView);
		CheckBox checkBox = (CheckBox) configView.findViewById(R.id.configCheckBox);
		marketConfig.setLoadAppIcon(checkBox.isChecked());
		editor.putBoolean(MarketConfig.LOAD_APP_ICON, marketConfig.isLoadAppIcon());

		configView = findViewById(R.id.appScreenshotConfigView);
		checkBox = (CheckBox) configView.findViewById(R.id.configCheckBox);
		marketConfig.setLoadAppScreenshot(checkBox.isChecked());
		editor.putBoolean(MarketConfig.LOAD_APP_SCREENSHOT, marketConfig.isLoadAppScreenshot());

		configView = findViewById(R.id.checkSoftwareUpdateConfigView);
		checkBox = (CheckBox) configView.findViewById(R.id.configCheckBox);
		marketConfig.setCheckSoftwareUpdate(checkBox.isChecked());
		editor.putBoolean(MarketConfig.AUTO_CHECK_SOFTWARE_UPDATE, marketConfig.isCheckSoftwareUpdate());

		editor.commit();
	}

	@Override
	protected void initCustomActionbar() {
		// TODO Auto-generated method stub
		
	}

}
