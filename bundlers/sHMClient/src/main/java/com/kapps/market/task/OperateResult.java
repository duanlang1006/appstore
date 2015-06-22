package com.kapps.market.task;

import com.kapps.market.service.ActionException;
import com.kapps.market.task.mark.ATaskMark;

/**
 * 2010-6-16 �������ķ�װ
 * 
 * @author admin
 * 
 */
public class OperateResult {

	// ��ʾִ�е�һ�������ݡ�
	private Object resultData;
	// ����������ĸ�
	private ATaskMark taskMark;
	// ���ִ�й�����׳������쳣�Ǳ߽�������
	// ����������Ϊerror
	private ActionException actionException;
	// ����
	private Object attach;

	/**
	 * @param taskMark
	 * @param resultData
	 */
	public OperateResult(ATaskMark taskMark, Object resultData) {
		this.taskMark = taskMark;
		this.resultData = resultData;
	}


	public OperateResult(ATaskMark taskMark) {
		super();
		this.taskMark = taskMark;
	}

	/**
	 * @return the resultData
	 */
	public Object getResultData() {
		return resultData;
	}

	/**
	 * @param resultData
	 *            the resultData to set
	 */
	public void setResultData(Object resultData) {
		this.resultData = resultData;
	}

	/**
	 * @return the taskMark
	 */
	public ATaskMark getTaskMark() {
		return taskMark;
	}

	/**
	 * @param taskMark
	 *            the taskMark to set
	 */
	public void setTaskMark(ATaskMark taskMark) {
		this.taskMark = taskMark;
	}

	/**
	 * @return the serviceException
	 */
	public ActionException getActionException() {
		return actionException;
	}


	public void setActionException(ActionException actionException) {
		this.actionException = actionException;
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
}
