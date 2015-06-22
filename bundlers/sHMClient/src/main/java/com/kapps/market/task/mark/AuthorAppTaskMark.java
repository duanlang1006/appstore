package com.kapps.market.task.mark;

/**
 * 2010-6-22 <br>
 * ��ÿ�����Ա�����������
 * 
 * @author admin
 * 
 */
public class AuthorAppTaskMark extends APageTaskMark {

	// �������
	private String appAuthor;

	/**
	 * @param appAuthor
	 */
	public AuthorAppTaskMark(String appAuthor) {
		super();
		this.appAuthor = appAuthor;
	}

	/**
	 * @return the develper
	 */
	public String getAuthor() {
		return appAuthor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "";
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
		result = prime * result + ((appAuthor == null) ? 0 : appAuthor.hashCode());
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
		AuthorAppTaskMark other = (AuthorAppTaskMark) obj;
		if (appAuthor == null) {
			if (other.appAuthor != null)
				return false;
		} else if (!appAuthor.equals(other.appAuthor))
			return false;
		return true;
	}

}
