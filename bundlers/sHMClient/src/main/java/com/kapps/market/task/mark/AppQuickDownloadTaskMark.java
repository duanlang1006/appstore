package com.kapps.market.task.mark;

/**
 * 2011-5-11<br>
 * �����������
 * 
 * @author shuizhu
 * 
 */
public class AppQuickDownloadTaskMark extends ATaskMark {
	private int aid;

	/**
	 * @param pname
	 * @param vcode
	 */
	public AppQuickDownloadTaskMark(int aid) {
		super();
		this.aid = aid;
		
	}

	public int getAid() {
		return aid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + aid;
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
		AppQuickDownloadTaskMark other = (AppQuickDownloadTaskMark) obj;
		if (aid != other.aid)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AppQuickDownloadTaskMark [aid=" + aid + "]";
	}


}
