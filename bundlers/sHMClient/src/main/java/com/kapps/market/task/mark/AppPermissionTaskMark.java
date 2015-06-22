package com.kapps.market.task.mark;

/**
 * 2010-7-22<br>
 * Ӧ�õ�ȫ���б�
 * 
 * @author admin
 * 
 */
public class AppPermissionTaskMark extends ATaskMark {
	// Ӧ�õ�id
	private int appId;

	/**
	 * @param appId
	 */
	public AppPermissionTaskMark(int appId) {
		super();
		this.appId = appId;
	}

	public int getAppId() {
		return appId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + appId;
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
		AppPermissionTaskMark other = (AppPermissionTaskMark) obj;
		if (appId != other.appId)
			return false;
		return true;
	}

}
