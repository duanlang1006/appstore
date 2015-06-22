package com.kapps.market.task.mark;

/**
 * 2010-6-22 <br>
 * ɾ���ղ� ˲̬��Ψһ
 * 
 * @author admin
 * 
 */
public class DeleteFavorTaskMark extends ATaskMark {
	// Ӧ��id
	private int appId;

	/**
	 * @param id
	 */
	public DeleteFavorTaskMark(int appId) {
		super();
		this.appId = appId;
	}

	/**
	 * @return the appId
	 */
	public int getAppId() {
		return appId;
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
		result = prime * result + appId;
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
		DeleteFavorTaskMark other = (DeleteFavorTaskMark) obj;
		if (appId != other.appId)
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
		return "DeleteFavorTaskMark [id=" + appId + ", taskStatus=" + taskStatus + "]";
	}

}
