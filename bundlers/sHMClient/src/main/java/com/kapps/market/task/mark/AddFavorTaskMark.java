package com.kapps.market.task.mark;

/**
 * 2010-6-22 <br>
 * ����ղ� ˲̬��Ψһ
 * 
 * @author admin
 * 
 */
public class AddFavorTaskMark extends ATaskMark {
	// �ղ�Ӧ
	private int appId;

	/**
	 * @param pname
	 * @param name
	 */
	public AddFavorTaskMark(int appId) {
		super();
		this.appId = appId;
	}

	/**
	 * @return the appId
	 */
	public int getAppId() {
		return appId;
	}

	/**
	 * @param appId
	 *            the appId to set
	 */
	public void setAppId(int appId) {
		this.appId = appId;
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
		AddFavorTaskMark other = (AddFavorTaskMark) obj;
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
		return "AddFavorTaskMark [appId=" + appId + "]";
	}

}
