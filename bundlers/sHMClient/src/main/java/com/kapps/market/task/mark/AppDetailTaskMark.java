package com.kapps.market.task.mark;

/**
 * 2010-6-29<br>
 * ��Ҫ����������ϸ��Ϣ
 * 
 * @author admin
 * 
 */
public class AppDetailTaskMark extends ATaskMark {
	// ��Ҫ����������ϸ��Ϣ��id
	private int id;

	public AppDetailTaskMark(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
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
		result = prime * result + id;
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
		AppDetailTaskMark other = (AppDetailTaskMark) obj;
		if (id != other.id)
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
		return "AppDetailTaskMark [id=" + id + "]";
	}

}
