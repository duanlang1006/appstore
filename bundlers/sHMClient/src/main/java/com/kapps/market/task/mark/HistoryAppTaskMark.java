package com.kapps.market.task.mark;

/**
 * 2010-7-19 �����������ʷ�汾<br>
 * ˲̬��Ψһ
 * 
 * @author admin
 * 
 */
public class HistoryAppTaskMark extends APageTaskMark {

	/**
	 * ���id
	 */
	private int appId;

	/**
	 * @param appAuthor
	 *            �������
	 */
	public HistoryAppTaskMark(int appId) {
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
		HistoryAppTaskMark other = (HistoryAppTaskMark) obj;
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
		return "HistoryAppTaskMark [appId=" + appId + ", toString()=" + super.toString() + "]";
	}

}
