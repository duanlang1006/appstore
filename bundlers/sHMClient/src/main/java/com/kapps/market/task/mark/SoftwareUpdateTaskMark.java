package com.kapps.market.task.mark;

/**
 * 2010-6-28 <br>
 * ������£�һ�����������Ϊ<br>
 * InitSoftwareTaskMark��һ��������
 * 
 * @author Administrator
 * 
 */
public class SoftwareUpdateTaskMark extends APageTaskMark {
	// Ψһ�ͺ�
	private int taskMark = UNIQUE;
	// �Ƿ����ֶ�(���ֻ���ڼ��)
	private boolean manul;

	public boolean isManul() {
		return manul;
	}

	public void setManul(boolean manul) {
		this.manul = manul;
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
		SoftwareUpdateTaskMark other = (SoftwareUpdateTaskMark) obj;
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
		return "SoftwareUpdateTaskMark  [taskMark=" + taskMark + ", taskStatus=" + taskStatus + "]";
	}

}
