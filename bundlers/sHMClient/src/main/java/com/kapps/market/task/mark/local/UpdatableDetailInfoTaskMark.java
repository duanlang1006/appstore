package com.kapps.market.task.mark.local;

import com.kapps.market.task.mark.ATaskMark;

/**
 * 2010-6-28 <br>
 * ��ȡ���Ը�������ĸ���ϸ��Ϣ�����ڰ�װ�����һ���Ӽ���
 * 
 * @author admin
 * 
 */
public class UpdatableDetailInfoTaskMark extends ATaskMark {
	// Ψһ�ͺ�
	private int taskMark = UNIQUE;

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
		UpdatableDetailInfoTaskMark other = (UpdatableDetailInfoTaskMark) obj;
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
		return "UpdatableDetailInfoTaskMark [taskMark=" + taskMark + ", taskStatus=" + taskStatus + "]";
	}

}
