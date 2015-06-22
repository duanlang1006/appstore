package com.kapps.market.task.tracker;

import android.app.Activity;
import android.os.SystemClock;

import com.kapps.market.MApplication;
import com.kapps.market.MarketManager;
import com.kapps.market.cache.AppCahceManager;
import com.kapps.market.cache.AssertCacheManager;
import com.kapps.market.cache.LocaleCacheManager;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.DependTaskMark;

import java.lang.ref.WeakReference;


/**
 * 2010-6-26
 *
 *  for result callback.
 * @author admin
 * 
 */
public abstract class AInvokeTracker {

	protected MApplication marketContext = MApplication.getInstance();
	// Ӧ�û���
	protected AppCahceManager appCahceManager = marketContext.getAppCahceManager();

	// ����Ӧ��
	protected LocaleCacheManager localeCacheManager = marketContext.getLocaleCacheManager();

	// ��Դ����
	protected AssertCacheManager assertCacheManager = marketContext.getAssertCacheManager();
	// �������
	protected MarketManager marketManager = marketContext.getMarketManager();
	// �����٣����ڽ��ͳɹ���ʧ�ܵĽ��
	// ��ȫ���ǣ���Ӧ��
	private WeakReference<IResultReceiver> iReference;

	// ���ٺ��һ��������ڴ���ĳЩ��ʱ�ģ�˲̬����ݣ��Ա㽫���Ǵ��ݸ���ͼ�ɡ�
	protected Object trackerResult;

	/**
	 * @param resultReceiver
	 */
	public AInvokeTracker(IResultReceiver resultReceiver) {
		super();
		this.iReference = new WeakReference<IResultReceiver>(resultReceiver);
	}

	/**
	 * @return the iReference
	 */
	public IResultReceiver getResultReceiver() {
		return iReference.get();
	}

	/**
	 * @param iReference
	 *            the iReference to set
	 */
	public void setResultReceiver(IResultReceiver iReference) {
		this.iReference = new WeakReference<IResultReceiver>(iReference);
	}

	/**
	 * if invoke success, result re-invoke by this callback.
	 */
	public void handleInvoikePrepare(ATaskMark taskMark) {
		if (taskMark instanceof DependTaskMark) {
			DependTaskMark dependTaskMark = (DependTaskMark) taskMark;
			ATaskMark dependTask = dependTaskMark.getDependTask();
			// �Ƿ���Ҫ�ȴ�, �����Ҫ���ȴ�15s��ע��ֻ�ȴ����е�����
			if (dependTask != null && dependTask.getTaskStatus() == ATaskMark.HANDLE_DOING) {
				LogUtil.d(TAG(), "handleInvoikePrepare wait for: " + dependTask);
				int tryCount = 0;
				while (true) {
					if (dependTask.getTaskStatus() != ATaskMark.HANDLE_DOING || tryCount >= 50) {
						break;
					}
					SystemClock.sleep(150);
					tryCount++;
				}
			} else {
				LogUtil.d(TAG(), "handleInvoikePrepare not need wait for: " + dependTask);
			}
		}
	}

	/**
	 * ����������յ��á�ע��:��ui�̵߳��á�<br>
	 * Ĭ��ִ��ȡ������������
	 * 
	 * @param taskMark
	 */
	public void handleInvokeFinalize(ATaskMark taskMark) {
		if (taskMark instanceof DependTaskMark) {
			DependTaskMark dependTaskMark = (DependTaskMark) taskMark;
			dependTaskMark.setDependTask(null);
		}
	}

	/**
	 * after Http, callback to UI.
	 * @param result
	 */
	public void handleInvokeOver(OperateResult result) {
		// ���������
		ATaskMark taskWraper = result.getTaskMark();

		// ����ڲ��׳�δ������쳣����ʾ����
		try {
			if (taskWraper.getTaskStatus() == ATaskMark.HANDLE_OVER) {
				// ʵ�ʵ���ݴ���
				// LogUtil.i(TAG(), "handleInvokeOver handle ok");
				handleResult(result);
			} else {
				// LogUtil.i(TAG(), "handleInvokeOver handle fault");
				handleFault(result);
			}

		} catch (Exception e) {
			taskWraper.setTaskStatus(ATaskMark.HANDLE_ERROR);
			e.printStackTrace();
		}

		// ���������
		IResultReceiver receiver = getResultReceiver();
		if (receiver == null || (receiver instanceof Activity && ((Activity) receiver).isFinishing())) {
			// LogUtil.i(TAG(),
			// "handleInvokeOver receive ingore................. taskMark: "
			// + taskWraper);
		} else {
			// LogUtil.i(TAG(),
			// "handleInvokeOver to receive................. taskMark: "
			// + taskWraper);
			receiver.receiveResult(taskWraper, result.getActionException(), trackerResult);
		}

		// ��֤ʧ��, ���ǵ�¼�����ʱ���Զ���ת����¼ҳ��
		if (result.getActionException() != null) {
			int exCode = result.getActionException().getExCode();
			// ����Ƿǵ�½���������֤�쳣����ô�Զ���ת����½ҳ����ʾ��½��
			if (!(this instanceof LoginTracker) && exCode == ActionException.RESULT_SESSION_TIMEOUT) {
				marketContext.handleSessionTimeOut(false);
			}
		}
	}

	/**
	 * ��־���
	 * 
	 * @return
	 */
	public abstract String TAG();

	/**
	 * ������ִ����ϵ�ʱ��Ļص��ӿ� ע�⣬������������¼��߳��е��á�<br>
	 * ʵ�ʵ���ݴ�����ʱ�ﵽ������ɡ�<br>
	 * �������ֻ����ATaskMark.HANDLE_OVER��ʱ��Ż���ã�������Ը��ʵ�ʵ�<br>
	 * ����ֵ���������ATaskMark��״̬��<br>
	 * ע��:��ui�̵߳��á�
	 * 
	 * @param result
	 */
	public abstract void handleResult(OperateResult result);

	/**
	 * �����ʧ���ˣ���ô������Ҫ����ݽ���һ���Ĵ��?<br>
	 * ע��:��ui�̵߳��á�
	 */
	public void handleFault(OperateResult result) {

	}

	public Object getTrackerResult() {
		return trackerResult;
	}
}
