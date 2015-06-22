package com.kapps.market.bean;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 2010-7-18<br>
 *
 * 
 * @author admin
 * 
 */
public class AppDetail implements Serializable {

	// ���ش���
	private int downloadCount;
	// ���۴���(���ڸ�Ҫ)
	private int commentCount;
	// ����
	private String describe;
	// �ϼ�ʱ��
	private String auditingTime;
	// ���ͼ������
	private String[] screenshots;
	// ��������
	private String authorEmail;
	// ������վ
	private String authorSite;

	/**
	 * @return the downloadCount
	 */
	public int getDownloadCount() {
		return downloadCount;
	}

	/**
	 * @param downloadCount
	 *            the downloadCount to set
	 */
	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}

	/**
	 * @return the commentCount
	 */
	public int getCommentCount() {
		return commentCount;
	}

	/**
	 * @param commentCount
	 *            the commentCount to set
	 */
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	/**
	 * @return the describe
	 */
	public String getDescribe() {
		return describe;
	}
	
	/**
	 * @return the describe
	 */
	public String getDescribeSplit() {
		int i1 = describe.indexOf("��");
		if (i1 == -1) {
			i1= describe.indexOf(",");
			if (i1 == -1) {
				i1 = describe.indexOf("");
				if (i1 == -1) {
					i1 = describe.indexOf("��");
				}
			}
		}
		if (i1 > 0) {
			return describe.substring(0, i1);
		}
		return describe;
	}

	/**
	 * @param describe
	 *            the describe to set
	 */
	public void setDescribe(String describe) {
		this.describe = describe;
	}

	/**
	 * @return the auditingTime
	 */
	public String getAuditingTime() {
		return auditingTime;
	}

	/**
	 * @param auditingTime
	 *            the auditingTime to set
	 */
	public void setAuditingTime(String auditingTime) {
		this.auditingTime = auditingTime;
	}

	/**
	 * @return the screenshots
	 */
	public String[] getScreenshots() {
		return screenshots;
	}

	/**
	 * @param screenshots
	 *            the screenshots to set
	 */
	public void setScreenshots(String[] screenshots) {
		this.screenshots = screenshots;
	}

	/**
	 * @return the autorEmail
	 */
	public String getAuthorEmail() {
		return authorEmail;
	}

	/**
	 * @param autorEmail
	 *            the autorEmail to set
	 */
	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}

	/**
	 * @return the autorSite
	 */
	public String getAuthorSite() {
		return authorSite;
	}

	/**
	 * @param authorSite
	 *            the autorSite to set
	 */
	public void setAuthorSite(String authorSite) {
		this.authorSite = authorSite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AppDetail [commentCount=" + commentCount + ", describe=" + describe + ", downloadCount="
				+ downloadCount + ", screenshots=" + Arrays.toString(screenshots) + "]";
	}

}
