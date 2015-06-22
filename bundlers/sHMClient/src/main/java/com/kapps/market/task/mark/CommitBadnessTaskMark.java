package com.kapps.market.task.mark;

/**
 * 2010-6-22 <br>
 * �ύ�к���Ϣ������
 * 
 * @author admin
 * 
 */
public class CommitBadnessTaskMark extends ATaskMark {
	// �к���Ϣ��Ӧ��Ӧ����Ϊ��ʶ
	private int appId;

	/**
	 * @param id
	 */
	public CommitBadnessTaskMark(int appId) {
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
		CommitBadnessTaskMark other = (CommitBadnessTaskMark) obj;
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
		return "CommitBadnessTaskMark [id=" + appId + ", taskStatus=" + taskStatus + "]";
	}

}
