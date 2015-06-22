package com.kapps.market.task;

import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppImageTaskMark;
import com.kapps.market.task.mark.MultipleTaskMark;
import com.kapps.market.task.tracker.AInvokeTracker;

/**
 * 2010-7-24<br>
 * ͼƬ��Դ��ȡ�������
 * 
 * @author admin
 * 
 */
public class ImageTaskScheduler extends MultipleTaskScheduler {

	public static final String TAG = "ImageTaskScheduler";

	/**
	 * @param service
	 * @param receiver
	 * @param multipleTaskMark
	 */
	public ImageTaskScheduler(MarketServiceWraper service, MultipleTaskMark multipleTaskMark) {
		super(service, multipleTaskMark);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.task.MultipleTaskScheduler#TAG()
	 */
	@Override
	public String TAG() {
		// TODO Auto-generated method stub
		return TAG;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.task.MultipleTaskScheduler#handleExecuteNextTask(com
	 * .hiapk.market.task.mark.ATaskMark, com.ck.market.task.IResultReceiver)
	 */
	@Override
	protected AInvokeTracker handleExecuteNextTask(ATaskMark taskMark, IResultReceiver receiver) {
		AppImageTaskMark appImageTaskMark = (AppImageTaskMark) taskMark;
		AsyncOperation operation = serviceWraper.getAppImageResource(receiver, taskMark, null,
				appImageTaskMark.getId(), appImageTaskMark.getUrl(), appImageTaskMark.getType());

		return operation.getInvokeTracker();
	}

}
