package com.kapps.market.analysi;

import android.content.Context;

import com.feedback.NotificationType;
import com.feedback.UMFeedbackService;
import com.mobclick.android.MobclickAgent;

/**
 * ��ݷ��չ�����
 * 
 * @author shuizhu
 * 
 */
public class AnalysiManager {

	// ���˷�������
	public static void enableNewReplyNotification(Context context, NotificationType type) {
		try {
			UMFeedbackService.enableNewReplyNotification(context, type);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ���˷���
	public static void openUmengFeedbackSDK(Context context) {
		try {
			UMFeedbackService.openUmengFeedbackSDK(context);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// �ָ�
	public static void onResume(Context context) {
		try {
			MobclickAgent.onResume(context);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ֹͣ
	public static void onPause(Context context) {
		try {
			MobclickAgent.onPause(context);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
