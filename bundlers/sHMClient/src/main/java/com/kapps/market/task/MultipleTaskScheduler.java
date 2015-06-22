package com.kapps.market.task;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import com.kapps.market.service.ActionException;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.MultipleTaskMark;
import com.kapps.market.task.tracker.AInvokeTracker;


/**
 * 2010-7-24 <br>
 * ����������ߣ����ڸ���ServiceWraper<br>
 * Ŀǰ�ݶ�����ͬʱִ�����
 * 
 * @author admin
 * 
 */
public abstract class MultipleTaskScheduler implements IResultReceiver {

	// �������װ
	protected MultipleTaskMark multipleTaskMark;
	protected MarketServiceWraper serviceWraper;
	// ÿ��ͬʱִ����
	public static final int TASK_WROKER_COUNT = 2;
	// �����٣����ڽ��ͳɹ���ʧ�ܵĽ��
	// ��ȫ���ǣ���Ӧ��
	private WeakReference<IResultReceiver> weakReceiver;

	// ��ǰִ�е�����
	protected HashMap<ATaskMark, AInvokeTracker> schedulingMap = new HashMap<ATaskMark, AInvokeTracker>();

	/**
	 * @param multipleTaskMark
	 */
	public MultipleTaskScheduler(MarketServiceWraper service, MultipleTaskMark multipleTaskMark) {
		this.serviceWraper = service;
		this.multipleTaskMark = multipleTaskMark;
	}

	/**
	 * �ϲ�����������Ϊͬ���������ֻ�в��ֱ�ִ���ˣ�����������<br>
	 * Ҫ��ִ����ͬ��������Ӧ�úϲ���
	 * 
	 * @param mTaskMark
	 */
	public void mergeTaskSchedul(MultipleTaskMark mTaskMark) {
		List<ATaskMark> taskMarkList = mTaskMark.getTaskMarkList();
		// LogUtil.v(TAG(), "merge task: before -->/nadd task size: "
		// + taskMarkList.size());
		// �Ƴ������Ѿ���ִ�е����񲢶���ɵĺ�������
		taskMarkList.removeAll(schedulingMap.keySet());
		multipleTaskMark = mTaskMark;
		// LogUtil.v(TAG(), "merge task: scheduling size: "
		// + schedulingMap.keySet().size() + " new add size: "
		// + taskMarkList.size());
	}

	/**
	 * ����ִ��������������TASK_WROKER_COUNT��
	 */
	public void triggerSchedulTask() {
		if (schedulingMap.size() < TASK_WROKER_COUNT) {
			int canCount = TASK_WROKER_COUNT - schedulingMap.size();
			for (int index = 0; index < canCount; index++) {
				ATaskMark nextTaskMark = multipleTaskMark.pickNextTaskMark();
				if (nextTaskMark != null) {
					AInvokeTracker tracker = handleExecuteNextTask(nextTaskMark, this);
					schedulingMap.put(nextTaskMark, tracker);
					// LogUtil.v(TAG(), "schedul next task mark: " +
					// nextTaskMark);
				}
			}
		}
	}

	/**
	 * @return the multipleTaskMark
	 */
	public MultipleTaskMark getMultipleTaskMark() {
		return multipleTaskMark;
	}

	/**
	 * @param multipleTaskMark
	 *            the multipleTaskMark to set
	 */
	public void setMultipleTaskMark(MultipleTaskMark multipleTaskMark) {
		this.multipleTaskMark = multipleTaskMark;
	}

	/**
	 * ���ͼƬ������, �Ѿ�������ͬ���
	 * 
	 * @param receiver
	 *            the receiver to add
	 */
	public void setReceiver(IResultReceiver receiver) {
		weakReceiver = new WeakReference<IResultReceiver>(receiver);
	}

	/**
	 * �������ʵ�ʵ�����
	 * 
	 * @param taskMark
	 *            ������
	 * @param receiver
	 *            ���������
	 */
	protected abstract AInvokeTracker handleExecuteNextTask(ATaskMark taskMark, IResultReceiver receiver);

	public abstract String TAG();

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		// LogUtil.v(TAG(), "before --> receiver size" + receiveList.size()
		// + "\nscheduling size: " + schedulingMap.size()
		// + "\ntask schedul over: " + taskMark + "\nrest task count:"
		// + multipleTaskMark.getTaskMarkList().size());

		IResultReceiver receiver = null;
		if (weakReceiver != null) {
			receiver = weakReceiver.get();
			if (receiver != null) {
				try {
					receiver.receiveResult(taskMark, exception, trackerResult);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// �Ƴ��¼
		schedulingMap.remove(taskMark);
		// û�н��������ٵ���ʣ�������
		if (receiver != null) {
			triggerSchedulTask();

		} else if (multipleTaskMark != null) {
			multipleTaskMark.invalidTaskMark();
			schedulingMap.clear();
		}

		// ����Ƿ��Ѿ�û������
		if (schedulingMap.size() == 0 && multipleTaskMark != null && multipleTaskMark.getTaskMarkList().size() == 0) {
			weakReceiver = null;
		}

		// LogUtil.v(TAG(), "after --> scheduling weakReceiver: " + weakReceiver
		// + "size: " + schedulingMap.size() + "\ntask schedul over: "
		// + taskMark + "\nrest task count:"
		// + multipleTaskMark.getTaskMarkList().size());
	}

	/**
	 * ���õ�����,�Ա������µ�һ��
	 */
	public void resetScheduler() {
		if (multipleTaskMark != null) {
			multipleTaskMark.invalidTaskMark();
			multipleTaskMark = null;
		}

		weakReceiver = null;
		schedulingMap.clear();
	}
}
