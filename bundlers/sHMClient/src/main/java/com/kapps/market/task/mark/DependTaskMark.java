package com.kapps.market.task.mark;

/**
 * 2010-8-25<br>
 * ���һ��������Ҫ�������˵���Ϊ<br>
 * �����������������Ǹ�AInvokeTracker��handleInvoikePrepare<br>
 * ��Ҫ����Ӧ�Ĵ������ȴ�
 * 
 * @author admin
 * 
 */
public class DependTaskMark extends APageTaskMark {

	// ������������
	private ATaskMark dependTask;

	/**
	 * @return the dependTask
	 */
	public ATaskMark getDependTask() {
		return dependTask;
	}

	/**
	 * @param dependTask
	 *            the dependTask to set
	 */
	public void setDependTask(ATaskMark dependTask) {
		this.dependTask = dependTask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DependTaskMark [dependTask=" + dependTask + ", toString()=" + super.toString() + "]";
	}

}
