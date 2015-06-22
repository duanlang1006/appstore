package com.kapps.market.task.mark;

/**
 * 2010-6-19<br>
 * Ӧ�õ�����
 * 
 * @author admin
 */
public class CommentsTaskMark extends APageTaskMark {

	// �ĸ�Ӧ�õ�����
	private int appId;

	/**
	 * @param appId
	 */
	public CommentsTaskMark(int appId) {
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "";
	}
}