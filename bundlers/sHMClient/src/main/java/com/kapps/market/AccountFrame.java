package com.kapps.market;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kapps.market.bean.UserInfo;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.LoginTaskMark;
import com.kapps.market.task.mark.RegistTaskMark;
import com.kapps.market.util.Constants;
import com.kapps.market.util.SecurityUtil;
import com.kapps.market.util.Util;

/**
 * 
 * @author Administrator
 * 
 */
// TODO ��Ҫ���������񣬱����ؽ��ʱ�����ھ����������ã�����޷����½��
public class AccountFrame extends TabableActivity implements OnClickListener, IResultReceiver {

	public static final String TAG = "LoginRegistFrame";

	private int loginFrameState = Constants.CM_LOGIN;

	// ����¼��ͼ
	public static final int MAIN_LOGIN_VIEW = 1;
	// ע����ͼ
	public static final int REGIST_VIEW = 2;
	// �����
	public static final int PROGRESS_VIEW = 3;
	// ע��
	public static final int LOGOUT_VIEW = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ��½ʱ���һ��״̬
		loginFrameState = getIntent().getIntExtra(Constants.LOGIN_INVOKE_STATE, Constants.CM_LOGIN);

		setContentView(R.layout.m_login_page);
		contentFrame = (FrameLayout)findViewById(R.id.contentFrame);
		
		registerView(MAIN_LOGIN_VIEW);
		registerView(REGIST_VIEW);
		registerView(LOGOUT_VIEW);

		// ����ͼ(��������¼ҳ�棬�밴��pre��ť�Ĳ�����һ���)
		if (loginFrameState == Constants.VIEW_LOGIN) {
			showChoosedView(LOGOUT_VIEW);
		}
		else {
			showChoosedView(MAIN_LOGIN_VIEW);
		}
		TextView version = (TextView)findViewById(R.id.version);
		version.setText(version.getText()+Constants.VERSION);
	}
	
	@Override
	protected void initCustomActionbar() {
		ActionBar actionBar = getSupportActionBar();
		//���ñ���  
        actionBar.setTitle(R.string.account);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onClick(View trigger) {
		if (trigger.getId() == R.id.loginViewButton) {
			handleLogin();

		} else if (trigger.getId() == R.id.nextButton) {
			if (getCurrentTabMark() == REGIST_VIEW) {
				handleRegist();
			}
		} else if (trigger.getId() == R.id.registDeclareBox) {
			CheckBox registDeclareBox = (CheckBox) trigger;
			if (registDeclareBox.isChecked()) {
				showDialog(R.id.registDeclareBox);
			}
			Button bButton = (Button) findViewById(R.id.nextButton);
			bButton.setEnabled(registDeclareBox.isChecked());

		} else if (trigger.getId() == R.id.logoutButton) {
			marketContext.initMarketContextForLogout();
			finish();
		} else if (trigger.getId() == R.id.backButton) {
			finish();
		} else {
			super.onClick(trigger);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case R.id.registDeclareBox:
			return new AlertDialog.Builder(this).setTitle(R.string.terms_title).setMessage(R.string.terms_content)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(R.id.registDeclareBox);
						}
					}).create();

		case R.id.progressLabel:
			return new AlertDialog.Builder(this).setTitle(getString(R.string.note))
					.setMessage(getString(R.string.wait_for_progress))
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(R.id.progressLabel);
						}
					}).create();
		default:
			return null;
		}
	}

	// �����½
	private void handleLogin() {
		// �������뷨����
		Util.hideInputMethodWindow(this, getCurrentFocus());

		String userId = ((EditText) findViewById(R.id.userIdField)).getText().toString();
		String pw = ((EditText) findViewById(R.id.pwdField)).getText().toString();
		// �����Ч��
		StringBuilder sb = new StringBuilder();
		if (userId.length() == 0) {
			sb.append(getString(R.string.login_userid_cant_none) + "\n");
		}
		if (pw.length() < 7 || pw.length() > 12) {
			sb.append(getString(R.string.pw_must_satisfactory) + "\n");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
			TextView textView = (TextView) findViewById(R.id.loginResultLabel);
			textView.setText(sb.toString());
			return;
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setName(userId);

		try {
			pw = SecurityUtil.md5Encode(pw);
			userInfo.setPassword(pw);

			TextView textView = (TextView) findViewById(R.id.loginResultLabel);
			textView.setText("");

			doLogin(userInfo);

		} catch (Exception e) {
			e.printStackTrace();
			showProgressInfo(getString(R.string.login_unusual));
		}
	}

	// ����ע��
	private void handleRegist() {
		// �������뷨����
		Util.hideInputMethodWindow(this, getCurrentFocus());

		String userId = ((EditText) findViewById(R.id.userIdField)).getText().toString();
		String pw = ((EditText) findViewById(R.id.pwdField)).getText().toString();
		String comfirmPw = ((EditText) findViewById(R.id.affirmPwdField)).getText().toString();
		// �����Ч��
		StringBuilder sb = new StringBuilder();
		LogUtil.d(
				TAG,
				"+++++++++++: " + Util.isEmail(userId) + " userId: " + userId + " \nuserId.length(): "
						+ userId.length());
		if (!Util.isEmail(userId) || userId.length() < 3 || userId.length() > 25) {
			sb.append(getString(R.string.email_must_satisfactory) + "\n");
		}
		if (pw.length() < 7 || pw.length() > 12) {
			sb.append(getString(R.string.pw_must_satisfactory) + "\n");
		}
		if (!pw.equals(comfirmPw)) {
			sb.append(getString(R.string.password_not_confirm) + "\n");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
			TextView textView = (TextView) findViewById(R.id.registResultLabel);
			textView.setText(sb.toString());
			return;
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setName(userId);
		userInfo.setEmail(userId); // ע��ʱ������û���һ��
		try {
			pw = SecurityUtil.md5Encode(pw);
			userInfo.setPassword(pw);

			// sign
			String sign = SecurityUtil.md5Encode(marketContext.getTs() + Constants.SEC_KEY_STRING);

			// ����ע��
			serviceWraper.register(this, taskMarkPool.getRegistTask(), userInfo, sign);

			TextView textView = (TextView) findViewById(R.id.registResultLabel);
			textView.setText("");
			showChoosedView(PROGRESS_VIEW);
			showProgressInfo(getString(R.string.progress_in_registe));

		} catch (Exception e) {
			e.printStackTrace();
			showProgressInfo(getString(R.string.registe_unusual));
		}
	}

	// ʵ�ʵĵ�¼
	private void doLogin(UserInfo userInfo) {
		//�ɻỰ��Ϣ��Ч
		marketContext.getSharedPrefManager().saveSession(null);
		
		// ��¼
		marketContext.getSharedPrefManager().saveUserInfo(userInfo);

		// �����½
		ATaskMark taskMark = taskMarkPool.createLoginTaskMark(userInfo.getName(), userInfo.getPassword());
		// ����½���񲻴�����ô���Է���֮ǰ�ĵ�½����
		if (!serviceWraper.isTaskExist(taskMark)) {
			serviceWraper.forceDiscardReceiveTask(taskMark);
			// ����Ѿ������Ǳ�ǿ�ƽӹ�
			String sign = SecurityUtil.md5Encode(marketContext.getTs() + Constants.SEC_KEY_STRING);
			serviceWraper.login(this, taskMark, userInfo, marketContext.getContextConfig().getDeviceId(), marketContext
					.getContextConfig().getSimId(), sign);

		} else {
			serviceWraper.forceTakeoverTask(this, taskMark);
		}

		showChoosedView(PROGRESS_VIEW);
		showProgressInfo(getString(R.string.progress_in_login));
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		// ִ�гɹ�
		if (taskMark instanceof LoginTaskMark) {
			if (taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
				showProgressInfo(getString(R.string.login_success_entry_home));
				if (loginFrameState == Constants.SUB_LOGIN) {
					finish();
				} else {
					showMainFrame();
				}

			} else {
				showChoosedView(MAIN_LOGIN_VIEW);

				TextView textView = (TextView) findViewById(R.id.loginResultLabel);
				textView.setText(exception.getExMessage());

				UserInfo userInfo = marketContext.getSharedPrefManager().getUserInfo();
				textView = ((EditText) findViewById(R.id.userIdField));
				textView.setText(userInfo.getName());
			}

		} else if (taskMark instanceof RegistTaskMark) {
			if (taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
				// �Զ���½
				showProgressInfo(getString(R.string.registe_success_then_login));
				UserInfo userInfo = marketContext.getSharedPrefManager().getUserInfo();
				doLogin(userInfo);

			} else {
				showChoosedView(REGIST_VIEW);
				TextView textView = (TextView) findViewById(R.id.registResultLabel);
				textView.setText(exception.getExMessage());
			}
		}
	}

	// ��ʾһ�����
	private void showProgressInfo(String info) {
		// �Զ���½
		TextView registLable = (TextView) findViewById(R.id.progressLabel);
		if (registLable != null) {
			registLable.setText(info);
		}
	}

	// ��ʾ��Ӧ��ҳ��
	private void showMainFrame() {
		Intent intent = new Intent(this, MarketMainFrame.class);
		intent.putExtra(Constants.LOGIN_INVOKE_STATE, loginFrameState);
		startActivity(intent);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			int viewMark = getCurrentTabMark();
			switch (viewMark) {
			case REGIST_VIEW:
				showChoosedView(MAIN_LOGIN_VIEW);
				return true;

			case PROGRESS_VIEW:
				showDialog(R.id.progressLabel);
				return true;
			case LOGOUT_VIEW:
				finish();
				return true;
			default:
				return super.onKeyDown(keyCode, event);
			}
		} else if (KeyEvent.KEYCODE_SEARCH == keyCode) {
			return false;

		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected int getShowViewMark(View trigger) {
		// TODO Auto-generated method stub
		switch (trigger.getId()) {
		case R.id.preButton:
			return MAIN_LOGIN_VIEW;

		case R.id.registerViewButton:
			return REGIST_VIEW;
		case R.id.backButton:
			return LOGOUT_VIEW;
		default:
			return MAIN_LOGIN_VIEW;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hiapk.market.TabableActivity#onAfterShowView(android.view.View)
	 */
	@Override
	protected void onAfterShowView(int viewMark) {
		// �ɵ�ǰ����ͼ״̬������δ���
		switch (viewMark) {
		case REGIST_VIEW:
			Button aButton = (Button) findViewById(R.id.nextButton);
			CheckBox registBox = (CheckBox) findViewById(R.id.registDeclareBox);
			aButton.setEnabled(registBox.isChecked());
			break;

		}
	}

	@Override
	protected View createContentView(int viewMark) {
		// TODO Auto-generated method stub
		View view = null;
		switch (viewMark) {
		case MAIN_LOGIN_VIEW:
			view = LayoutInflater.from(this).inflate(R.layout.login_main_view, null);
			Button loginBtn = (Button) view.findViewById(R.id.loginViewButton);
			loginBtn.setOnClickListener(this);
			registerTrigger(view.findViewById(R.id.registerViewButton));
			break;

		case REGIST_VIEW:
			view = LayoutInflater.from(this).inflate(R.layout.regist_view, null);
			view.findViewById(R.id.registDeclareBox).setOnClickListener(this);
			view.findViewById(R.id.nextButton).setOnClickListener(this);
			registerTrigger(view.findViewById(R.id.preButton));
			break;
		case LOGOUT_VIEW:
			view = LayoutInflater.from(this).inflate(R.layout.welcome_view, null);
			String user_name = marketContext.getSharedPrefManager().getUserInfo().getName();
			if (user_name == null || user_name.length() == 0) {
				user_name = marketContext.getSharedPrefManager().getUserInfo().getEmail();
			}
			((TextView)(view.findViewById(R.id.user_nameLabel))).setText(user_name);
//			((TextView)view.findViewById(R.id.user_emailLabel)).setText(email);
			view.findViewById(R.id.logoutButton).setOnClickListener(this);
			registerTrigger(view.findViewById(R.id.backButton));
			break;
		case PROGRESS_VIEW:
			view = LayoutInflater.from(this).inflate(R.layout.progressbar_view_l_r, null);
			break;

		default:
			Log.w(TAG, "unkonw view: " + "viewMark = " + viewMark);
			break;
		}
		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
}
