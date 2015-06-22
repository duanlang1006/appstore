package com.kapps.market.task.mark;

/**
 * 2010-7-28<br>
 * ����ĸ�Ҫ��Ϣ
 * 
 * @author admin
 * 
 */
public class AppSummaryTaskMark extends ATaskMark {

	private int appId;
	private String pname;
	private int versionCode;

	/**
	 * @param pname
	 * @param version
	 */
	public AppSummaryTaskMark(String pname, int versionCode) {
		super();
		this.pname = pname;
		this.versionCode = versionCode;
	}

	public AppSummaryTaskMark(int appId) {
		this.appId = appId;
	}

	/**
	 * @return the pname
	 */
	public String getPname() {
		return pname;
	}

	/**
	 * @param pname
	 *            the pname to set
	 */
	public void setPname(String pname) {
		this.pname = pname;
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
		result = prime * result + ((pname == null) ? 0 : pname.hashCode());
		result = prime * result + versionCode;
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
		AppSummaryTaskMark other = (AppSummaryTaskMark) obj;
		if (appId != other.appId)
			return false;
		if (pname == null) {
			if (other.pname != null)
				return false;
		} else if (!pname.equals(other.pname))
			return false;
		if (versionCode != other.versionCode)
			return false;
		return true;
	}

}
