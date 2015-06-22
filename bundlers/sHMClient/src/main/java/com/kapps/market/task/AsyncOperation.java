package com.kapps.market.task;

import android.util.Log;

import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.RunAsyncTask.Status;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.tracker.AInvokeTracker;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * AsyncOperation is for HttpRequest.
 * @author admin
 * 
 */
@SuppressWarnings("unchecked")
public final class AsyncOperation {

	public static final String TAG = "AsyncOperation";

	// ��¼��ǰ�������б?�Ա�ͳһ����
	// ��ִ��ע���ʱ���б�Ҫֹͣ��ȡ��֮��ǰ�ύ������
	private static Hashtable<ATaskMark, AsyncOperation> taskRecordMap = new Hashtable<ATaskMark, AsyncOperation>();

	// ��������ִ�з���
	// ��ȫ���ǣ���Ӧ��
	private Method method;
	// ���ø��٣����ڴ���ɹ���ʧ�ܵĽ��
	private AInvokeTracker invokeTracker;
	// �Ƿ��Ѿ����������
	private boolean isError;
	// �첽����
	private RunAsyncTask asyncTask;
	// ��ǰ����ı�ʾ
	private ATaskMark taskMark;
	// ���ܷ����쳣
	private ActionException actionException;
	// ����,���ڸ�����ݴ���
	private Object attach;

	public AsyncOperation(ATaskMark taskMark, Method method) {
		this.method = method;
		this.taskMark = taskMark;
		// �����̿�ʼ�������Ϊ��ʼ���ء�
		taskMark.setTaskStatus(ATaskMark.HANDLE_DOING);
	}

	public AInvokeTracker getInvokeTracker() {
		return invokeTracker;
	}

	void setInvokeTracker(AInvokeTracker invokeTracker) {
		this.invokeTracker = invokeTracker;
	}

	/**
	 * @return the attach
	 */
	public Object getAttach() {
		return attach;
	}

	/**
	 * @param attach
	 *            the attach to set
	 */
	public void setAttach(Object attach) {
		this.attach = attach;
	}

	/**
	 * ִ��һ���첽����
	 * 
	 * @param service
	 *            �������ڶ���
	 * @param args
	 *            ��������
	 */
	void excuteOperate(final Object service, final Object... args) {
		asyncTask = new RunAsyncTask() {

			@Override
			protected Object doInBackground(Object... params) {
				LogUtil.iop(TAG, "task begin execute................. taskMark " + taskMark);
				Object result = null;
				try {
					if (method != null) {
						result = method.invoke(service, args);
					}

				} catch (Exception e) {
					e.printStackTrace();
					// ��Ǵ���
					isError = true;
					// ���ط����쳣���Ա�tracker���쳣���ͽ����ض����?
					if (e.getCause() instanceof ActionException) {
						actionException = (ActionException) e.getCause();
						Log.w(TAG, "actionException " + actionException);

					} else if (e instanceof ActionException) {
						actionException = (ActionException) e;
					}
				}

				// �˳�ǰ�ȵ���Ԥ����
				if (invokeTracker != null && !isCancelled()) {
					LogUtil.iop(TAG, "task Prepare................. taskMark " + taskMark);
					try {
						invokeTracker.handleInvoikePrepare(taskMark);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				return result;
			}

			@Override
			protected void onPostExecute(Object result) {
				LogUtil.iop(TAG, "task do over................. taskMark " + taskMark);
				// ȡ����������invokeTracker==null����Ҫ�ڴ���
				if (invokeTracker != null && !isCancelled()) {
					LogUtil.iop(TAG, "callback task................. taskMark: " + taskMark);
					// �����̽���
					if (isError) {
						taskMark.setTaskStatus(ATaskMark.HANDLE_ERROR);
					} else {
						taskMark.setTaskStatus(ATaskMark.HANDLE_OVER);
					}
					OperateResult operateResult = new OperateResult(taskMark);
					operateResult.setResultData(result);
					operateResult.setActionException(actionException);
					operateResult.setAttach(attach);
					try {
						invokeTracker.handleInvokeOver(operateResult);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					try {
						invokeTracker.handleInvokeFinalize(taskMark);
					} catch (Exception e) {
						e.printStackTrace();
					}

					// ��Ӧ�Ѿ�û�������������ֱ����ֹ
				} else {
					LogUtil.iop(TAG, "ingore task (invokeTracker == null)................. taskMark: " + taskMark);
					if (taskMark != null) {
						taskMark.setTaskStatus(ATaskMark.HANDLE_OVER);
					}
				}

				// �Ƴ�����
				if (taskMark != null) {
					taskRecordMap.remove(taskMark);
				}

				LogUtil.iop(TAG, "remove add now task count: " + taskRecordMap.size() + " isError: " + isError);
				LogUtil.iop(TAG, "excuteOperate method: " + (method == null ? "" : method.getName()) + " isError: "
						+ isError);

				// �������
				taskMark = null;
				invokeTracker = null;
				actionException = null;
				method = null;
			}
		};
		asyncTask.execute();
		// ��¼����
		taskRecordMap.put(taskMark, this);
		// LogUtil.i(TAG, "add now task count: " + taskRecordMap.size());
	}

	/**
	 * @param mayInterruptIfRunning
	 * @return
	 * @see RunAsyncTask#cancel(boolean)
	 */
	public final boolean clearAsysnTask(boolean mayInterruptIfRunning) {
		// ���Ƴ�����
		taskRecordMap.remove(taskMark);
		// ȡ������
		boolean ok = doCancle(mayInterruptIfRunning);
		return ok;
	}

	private final boolean doCancle(boolean mayInterruptIfRunning) {
		// ȡ������
		boolean ok = asyncTask.cancel(mayInterruptIfRunning);

		// ��������ִ�з���
		method = null;
		// ���ø��٣����ڽ��ͳɹ���ʧ�ܵĽ��
		invokeTracker = null;
		// ������
		taskMark.setTaskStatus(ATaskMark.HANDLE_OVER);
		taskMark = null;
		// �쳣
		actionException = null;
		// ����
		attach = null;

		return ok;
	}

	/**
	 * ֹͣ���е��첽����
	 */
	static void stopAllAsyncOperate() {
		for (AsyncOperation asyncOp : taskRecordMap.values()) {
			// �Զ�����ģ��������ã��Ա��ܹ�������������
			if (asyncOp.invokeTracker != null
					&& asyncOp.invokeTracker.getResultReceiver() instanceof MultipleTaskScheduler) {
				MultipleTaskScheduler scheduler = (MultipleTaskScheduler) asyncOp.invokeTracker.getResultReceiver();
				scheduler.resetScheduler();
			}

			asyncOp.doCancle(true);
		}
		taskRecordMap.clear();
	}

	/**
	 * ���?���ĵ��ñ���զִ��execute֮��
	 * 
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @see RunAsyncTask#get()
	 */
	public final Object get() throws InterruptedException, ExecutionException {
		return asyncTask.get();
	}

	/**
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 * @see RunAsyncTask#get(long, java.util.concurrent.TimeUnit)
	 */
	public final Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
			TimeoutException {
		return asyncTask.get(timeout, unit);
	}

	/**
	 * @return
	 * @see RunAsyncTask#getStatus()
	 */
	public final Status getStatus() {
		return asyncTask.getStatus();
	}

	/**
	 * @return
	 * @see RunAsyncTask#isCancelled()
	 */
	public final boolean isCancelled() {
		return asyncTask.isCancelled();
	}

	// �Ƿ��Ѿ�����ͬ���������
	static boolean isTaskExist(ATaskMark taskMark) {
		AsyncOperation asyncOperation = taskRecordMap.get(taskMark);
		if (asyncOperation != null) {
			LogUtil.iop(TAG, "check +++++++++task exist: " + taskMark);
			return true;
		} else {
			LogUtil.iop(TAG, "check +++++++++task not exist: " + taskMark);
			return false;
		}
	}

	/**
	 * ���ָ������
	 * 
	 * @param taskMark
	 *            ������
	 */
	static AsyncOperation getTaskByMark(ATaskMark taskMark) {
		return taskRecordMap.get(taskMark);
	}

	/**
	 * �첽������
	 * 
	 * @return
	 */
	static Collection<AsyncOperation> asyncOperations() {
		return taskRecordMap.values();
	}

}
