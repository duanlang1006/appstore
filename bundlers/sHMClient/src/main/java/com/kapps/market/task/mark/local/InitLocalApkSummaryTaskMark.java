package com.kapps.market.task.mark.local;

import com.kapps.market.task.mark.ATaskMark;

/**
 * 2010-6-28 <br>
 * ��ʼ������marketĬ������Ŀ¼�е�apk�ļ���
 * 
 * @author admin
 * 
 */
public class InitLocalApkSummaryTaskMark extends ATaskMark {

	// �豸id��ʾ�������
	private int taskMark = UNIQUE;

	/**
	 * @param deviceId
	 */
	public InitLocalApkSummaryTaskMark() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + taskMark;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InitLocalApkSummaryTaskMark other = (InitLocalApkSummaryTaskMark) obj;
		if (taskMark != other.taskMark)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "InitLocalApkSummaryTaskMark [taskMark=" + taskMark + ", taskStatus=" + taskStatus + "]";
	}

}
