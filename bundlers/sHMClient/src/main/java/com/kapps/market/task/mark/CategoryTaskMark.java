package com.kapps.market.task.mark;

/**
 * 2010-6-22<br>
 * ���������
 * 
 * @author admin
 * 
 */
public class CategoryTaskMark extends ATaskMark {

	// Ψһ�ͺ�
	private int taskMark = hashCode();

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
		CategoryTaskMark other = (CategoryTaskMark) obj;
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
		return "CategoryTaskMark [taskMark=" + taskMark + ", taskStatus=" + taskStatus + "]";
	}
}
