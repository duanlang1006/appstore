package com.kapps.market.task.mark;

/**
 * 2010-6-22 <br>
 * Task for Favor
 * 
 * @author admin
 * 
 */
public class AppFavorTaskMark extends APageTaskMark {

	// Ψһ�ͺ�
	private int taskMark = UNIQUE;

	/**
	 * @param id
	 */
	public AppFavorTaskMark() {
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
		AppFavorTaskMark other = (AppFavorTaskMark) obj;
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
		return "AppFavorTaskMark [taskMark=" + taskMark + ", toString()=" + super.toString() + "]";
	}

}
