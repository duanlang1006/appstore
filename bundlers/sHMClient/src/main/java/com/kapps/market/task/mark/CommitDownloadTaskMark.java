package com.kapps.market.task.mark;

public class CommitDownloadTaskMark extends APageTaskMark {
	private ATaskMark dependTask;
	private int type;
	// �ؼ���

	/**
	 * @return the dependTask
	 */
	public CommitDownloadTaskMark() {
		super();
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + type;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommitDownloadTaskMark other = (CommitDownloadTaskMark) obj;

		if (type != other.type)
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
		return "DependTaskMark [dependTask=" + dependTask + ", toString()=" + super.toString() + "]";
	}

}
