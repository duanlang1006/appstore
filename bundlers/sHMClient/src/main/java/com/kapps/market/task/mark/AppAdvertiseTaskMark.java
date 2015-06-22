package com.kapps.market.task.mark;

import com.kapps.market.util.ResourceEnum;

/**
 * 2010-6-24 <br>
 * �������, Ŀǰû�з�ҳ��Ϣ��
 * 
 * @author admin
 * 
 */
public class AppAdvertiseTaskMark extends DependTaskMark {

	// ���治���ĴӾ����������ֻ����
	// ������洢��������õ�ʱ��Ĭ��Ϊ-9999
	private int popType = ResourceEnum.AD_TYPE_EXCEL;
	// ��������(ʵ���Ժ�̨���õ�pop_sort����)
	private int sortType = ResourceEnum.SORT_DALL_NUM_DESC;
	// ����
	private int feeType = ResourceEnum.FEE_NONE_TYPE;

	/**
	 * @param category
	 * @param feeType
	 */
	public AppAdvertiseTaskMark(int popType) {
		this.popType = popType;
	}

	public int getPopType() {
		return popType;
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
		return "AppAdvertiseTaskMark [popType=" + popType + ", feeType=" + feeType + ", sortType=" + sortType
				+ ", taskStatus=" + taskStatus + ", toString()=" + super.toString() + "]";
	}

}
