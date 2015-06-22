package com.kapps.market.bean;

/**
 * 2010-6-9
 * 
 * @author admin
 * 
 */
public class AppPermission {

	// ����
	private String title;
	// ����
	private String des;
	// ����
	private boolean hide;

	/**
	 * 
	 */
	public AppPermission() {
		super();
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the des
	 */
	public String getDes() {
		return des;
	}

	/**
	 * @param des
	 *            the des to set
	 */
	public void setDes(String des) {
		this.des = des;
	}

	/**
	 * @return the hide
	 */
	public boolean isHide() {
		return hide;
	}

	/**
	 * @param hide
	 *            the hide to set
	 */
	public void setHide(boolean hide) {
		this.hide = hide;
	}
}
