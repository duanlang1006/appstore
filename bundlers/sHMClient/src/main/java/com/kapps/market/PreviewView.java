package com.kapps.market;

import android.content.Context;
import android.graphics.Color;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.kapps.market.bean.UserInfo;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.RunAsyncTask;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.LoginTaskMark;
import com.kapps.market.ui.CommonView;
import com.kapps.market.util.Constants;
import com.kapps.market.util.SecurityUtil;

/**
 * 2011-2-24<br>
 * 
 * Preview init something, including auto login???
 */
public class PreviewView extends CommonView implements IResultReceiver {
	// Ԥ������������
	private InitMarketTask initTask;
	// Ĭ�ϳ����Զ���½
	public boolean overLogin;

	public PreviewView(Context context) {
		super(context);

		addView(R.layout.preview_view);

		TextView textView = (TextView) findViewById(R.id.marketVersionLabel);
		textView.setText(getResources().getString(R.string.version_colon)
				+ marketContext.getContextConfig().getVersion());

		// ��ʼ���г�����
		initTask = new InitMarketTask();
		initTask.execute();

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// ѡ�񶯻�
		View loadView = findViewById(R.id.progressImage);
		loadView.clearAnimation();
		RotateAnimation rAnimation = new RotateAnimation(0.0f, 360.f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rAnimation.setFillEnabled(true);
		rAnimation.setInterpolator(new LinearInterpolator());
		rAnimation.setDuration(3000);
		rAnimation.setRepeatCount(Animation.INFINITE);
		loadView.startAnimation(rAnimation);

	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception,
			Object trackerResult) {
		// ִ�гɹ�
		LogUtil.d(TAG, "task : " + taskMark);
		if (taskMark instanceof LoginTaskMark) {
			overLogin = true;
		}

		// �Ƿ�Ҫ������ҳ
		checkCanEnterMainView();
	}

	// �Ƿ�Ҫ������ҳ
	private void checkCanEnterMainView() {
		LogUtil.d(TAG, "checkCanEnterMainView overLogin: " + overLogin);
		if (overLogin) {
			notifyPreviewOver();
		}
	}

	// ����ҳ����
	private void notifyPreviewOver() {
        Log.d("temp", "notifyPreviewOver");
        Message msg = Message.obtain();
		msg.what = Constants.M_PREVIEW_INIT_OVER;
		msg.arg1 = getMessageMark();
		notifyMessageToParent(msg);
	}

	// ֪ͨ���³�ʼ��
	private void notifyStartError() {
        Log.d("temp", "notifyStartError");
		TextView textView = (TextView) findViewById(R.id.loadView);
		textView.setTextColor(Color.RED);
		textView.setText(R.string.boot_error);
	}

	// ������
	private void notifyNotNetwork() {
		TextView textView = (TextView) findViewById(R.id.loadView);
		textView.setTextColor(Color.RED);
		textView.setText(R.string.check_network);

		// �Ƴ���ť
		Button button = (Button) findViewById(R.id.exitButton);
		button.setVisibility(View.VISIBLE);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Message msg = Message.obtain();
				msg.what = Constants.M_CLOSE_MAIN_FRAME;
				msg.arg1 = getMessageMark();
				notifyMessageToParent(msg);
			}
		});
	}

	// �г��ĳ�ʼ������
	@SuppressWarnings("unchecked")
	private class InitMarketTask extends RunAsyncTask {

		public InitMarketTask() {
			super();
		}

		@Override
		protected Object doInBackground(Object... params) {
			// ��ʼ��ʼ��
			publishProgress(getResources().getString(R.string.market_init_config), 30);
			if (!marketContext.isBaseDataOk()) {
				publishProgress(getResources().getString(
								R.string.load_config_fail_cant_boot_market), -1);
				return null;
			}

			// �������
			marketContext.checkLocalNetwork();
			if (!NetworkinfoParser.isNetConnect(marketContext)) {
				return null;
			}

			// �Ժ�ÿ��36Сʱ����һ�Ρ�
			marketContext.handleMarketEmptyMessage(Constants.M_REPORT_CHANNEL);

			// �Զ���½
			publishProgress(getResources().getString(R.string.login_request), 40);
            autoLogin();

			// ע������ڲ�Ӧ�û���ʱ�䳬����ô��Ҫɾ���Ա�������ƫ�õĻ����ڽ�̳�ʱ��
			// ���������ʼ����ʾ�ɵ���ݡ�
			marketContext.checkForLongLive();

            /*
			// 广告图的获取，在preview 就开始请求. ConverFlowFull也请求一次
//			AppAdvertiseTaskMark taskMark = taskMarkPool.getAppAdvertiseTaskMark(ResourceEnum.AD_TYPE_TOP);
//			if (marketContext.getAppCahceManager().getAppItemCount(taskMark) == 0) {
//				PageInfo pageInfo = taskMark.getPageInfo();
//                if (pageInfo != null) {
//                    serviceWraper.getAppAdvertiseByType(PreviewView.this, taskMark,
//                            taskMark.getPopType(), pageInfo.getNextPageIndex(),
//                            pageInfo.getPageSize());
//                }
//			}

			// ���ع����������ȴ�
            AppAdvertiseTaskMark taskMark = taskMarkPool.getAppAdvertiseTaskMark(ResourceEnum.AD_TYPE_EXCEL);
			if (marketContext.getAppCahceManager().getAppItemCount(taskMark) == 0) {
				PageInfo pageInfo = taskMark.getPageInfo();
                if (pageInfo != null) {
                    serviceWraper.getAppAdvertiseByType(PreviewView.this, taskMark,
                            taskMark.getPopType(), pageInfo.getNextPageIndex(),
                            pageInfo.getPageSize());
                }
			}
            */
			// ����Ƿ���push��Ϣ
			marketContext.handleMarketEmptyMessage(Constants.M_CHECK_SATIC_AD);

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (!marketContext.isBaseDataOk()) {
				notifyStartError();

			} else if (!NetworkinfoParser.isNetConnect(marketContext)) {
				notifyPreviewOver();

			} else {
				// �Ƿ�Ҫ������ҳ
				checkCanEnterMainView();
			}

			LogUtil.d(
					TAG,
					"onPostExecute isBaseDataOk:"
							+ marketContext.isBaseDataOk() + " netConnect: "
							+ NetworkinfoParser.isNetConnect(marketContext));
		}

		// �Զ���½
		private void autoLogin() {
			UserInfo userInfo = marketContext.getSharedPrefManager()
					.getUserInfo();
			if (userInfo.getPassword() == null || userInfo.getName() == null) {
				marketContext.getSharedPrefManager().saveSession(null);
				overLogin = true;

				// �Ự�Ƿ���Ч
			} else if (!marketContext.isSessionLocalValid()) {
				// �����Ǹ�ͬ������
				// �����½
				ATaskMark taskMark = taskMarkPool.createLoginTaskMark(
						userInfo.getName(), userInfo.getPassword());

				// ����½���񲻴�����ô���Է���֮ǰ�ĵ�½����
				if (!serviceWraper.isTaskExist(taskMark)) {
					serviceWraper.forceDiscardReceiveTask(taskMark);
					// ����Ѿ������Ǳ�ǿ�ƽӹ�
					String sign = SecurityUtil.md5Encode(marketContext.getTs()
							+ Constants.SEC_KEY_STRING);
					serviceWraper.login(PreviewView.this, taskMark, userInfo,
							marketContext.getContextConfig().getDeviceId(),
							marketContext.getContextConfig().getSimId(), sign);

				} else {
					marketContext.getServiceWraper().forceTakeoverTask(
							PreviewView.this, taskMark);
				}

			} else {
				overLogin = true;
			}
		}

	}
}
