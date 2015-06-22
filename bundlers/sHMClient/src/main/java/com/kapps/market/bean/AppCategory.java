package com.kapps.market.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 2010-6-8
 * Bean of Category.
 * 
 * @author admin
 * 
 */
public class AppCategory implements Iconable {

	// ��������Ӧ����ݿ�id
	private int id;
	// ����id
	private int pid;
	// Ӧ������
	private String name;
	// ͼƬurl
	private String iconUrl;
	// ����
	private int appCount;
	// �������
	private int type;
	// ��������������
	private String topAppName;
	// �����
	private List<AppCategory> subList = new ArrayList<AppCategory>();

	public AppCategory(int id, String name) {
		this.name = name;
		this.id = id;
	}

	public AppCategory(int id, String name, int type) {
		this.name = name;
		this.id = id;
		this.type = type;
	}

	/**
	 * @param name
	 */
	public AppCategory() {
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getIconId() {
		return id;
	}

	/**
	 * @return the pid
	 */
	public int getPid() {
		return pid;
	}

	/**
	 * @param pid
	 *            the pid to set
	 */
	public void setPid(int pid) {
		this.pid = pid;
	}

	/**
	 * @return the iconUrl
	 */
	@Override
	public String getIconUrl() {
		return iconUrl;
	}

	/**
	 * @param iconUrl
	 *            the iconUrl to set
	 */
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	@Override
	public int getIconType() {
		return MImageType.CATEGORY_ICON;
	}

	/**
	 * @return the appCount
	 */
	public int getAppCount() {
		return appCount;
	}

	/**
	 * @param appCount
	 *            the appCount to set
	 */
	public void setAppCount(int appCount) {
		this.appCount = appCount;
	}

	/**
	 * @return the subList
	 */
	public List<AppCategory> getSubList() {
		return subList;
	}

	/**
	 * @param subList
	 *            the subList to set
	 */
	public void addSubCategory(AppCategory subCatory) {
		subList.add(subCatory);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the topName
	 */
	public String getTopAppName() {
		return topAppName;
	}

	/**
	 * @param topAppName
	 *            the topName to set
	 */
	public void setTopAppName(String topAppName) {
		this.topAppName = topAppName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AppCategory [appCount=" + appCount + ", iconUrl=" + iconUrl + ", id=" + id + ", name=" + name
				+ ", pid=" + pid + ", subList=" + subList + "]";
	}

}
