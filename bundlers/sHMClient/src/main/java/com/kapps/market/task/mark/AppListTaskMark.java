package com.kapps.market.task.mark;

/**
 * 2010-6-24 <br>
 * app��������ͬʱΨһ�Ĵ��ϵͳ�Ǹ��е�һ��Ӧ�� �ڻ���������Ψһ�ģ� <br>
 * ͬʱһ�����Ӧ�õļ�����ϵͳ�� ͬһʱ��ֻ����һ�����С�
 * 
 * @author admin
 * 
 */
public class AppListTaskMark extends DependTaskMark {

	public static final int ALL_CATEGORY_MARK = -9999;
	// ���治���ĴӾ����������ֻ����
	// ������洢��������õ�ʱ��Ĭ��Ϊ-9999
	private int category = ALL_CATEGORY_MARK;
	// ��������
	private int sortType;
	// ����
	private int feeType;

	/**
	 * @param category
	 * @param subType
	 */
	public AppListTaskMark(int sortType, int feeType) {
		this.sortType = sortType;
		this.feeType = feeType;
	}

	/**
	 * @param category
	 * @param feeType
	 */
	public AppListTaskMark(int category, int sortType, int feeType) {
		this.category = category;
		this.sortType = sortType;
		this.feeType = feeType;
	}

	/**
	 * @return the category
	 */
	public int getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(int category) {
		this.category = category;
	}

	/**
	 * @return the subType
	 */
	public int getFeeType() {
		return feeType;
	}

	/**
	 * @param subType
	 *            the subType to set
	 */
	public void setFeeType(int subType) {
		this.feeType = subType;
	}

	/**
	 * @return the sortType
	 */
	public int getSortType() {
		return sortType;
	}

	/**
	 * @param sortType
	 *            the sortType to set
	 */
	public void setSortType(int sortType) {
		this.sortType = sortType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AppListTaskMark [category=" + category + ", feeType=" + feeType + ", sortType=" + sortType
				+ ", taskStatus=" + taskStatus + ", toString()=" + super.toString() + "]";
	}

}
