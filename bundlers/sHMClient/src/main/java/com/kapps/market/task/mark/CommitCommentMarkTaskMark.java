package com.kapps.market.task.mark;

/**
 * 2010-6-22 <br>
 * �ύ����������<br>
 * ˲̬��Ψһ
 * 
 * @author admin
 * 
 */
public class CommitCommentMarkTaskMark extends ATaskMark {
	// ������Ϣ��Ӧ��Ӧ����Ϊ��ʶ
	private int commentId;

	/**
	 * @param id
	 */
	public CommitCommentMarkTaskMark(int commentId) {
		super();
		this.commentId = commentId;
	}

	/**
	 * @return the commentId
	 */
	public int getCommentId() {
		return commentId;
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
		result = prime * result + commentId;
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
		CommitCommentMarkTaskMark other = (CommitCommentMarkTaskMark) obj;
		if (commentId != other.commentId)
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
		return "CommitCommentMarkTaskMark [commentId=" + commentId + ", toString()=" + super.toString() + "]";
	}

}
