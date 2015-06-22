package com.kapps.market.task.mark;

/**
 * 2010-7-25
 * 
 * @author admin
 * 
 */
public class AppImageTaskMark extends ATaskMark {

	// �ǽ�ͼ����ͼ��
	private int type;
	private int idd;
	private String url;

	/**
	 * @param id
	 * @param url
	 */
	public AppImageTaskMark(int id, String url, int type) {
		super();
		this.idd = id;
		this.url = url;
		this.type = type;
	}

	/**
	 * @return the appId
	 */
	public int getId() {
		return idd;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
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
		result = prime * result + idd;
		result = prime * result + type;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		AppImageTaskMark other = (AppImageTaskMark) obj;
		if (idd != other.idd)
			return false;
		if (type != other.type)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
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
		return "AppImageTaskMark [appId=" + idd + ", type=" + type + ", url=" + url + " super.toString()"
				+ super.toString() + "]";
	}

}
