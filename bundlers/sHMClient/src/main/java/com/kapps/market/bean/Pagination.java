package com.kapps.market.bean;

/**
 * 2010-6-9
 * 
 * @author admin
 * 
 */
public abstract class Pagination {
	// �ܹ�����
	private int totalCount;
	// ÿҳ������
	private int perCount;

	/**
	 * @return the totalCount
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * @param totalCount
	 *            the totalCount to set
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * ��Ӧ�б�������˵��������о������� �����������
	 * 
	 * @return ��ǰ�ܹ�����
	 */
	public abstract int getCurCount();

	/**
	 * @return the perCount
	 */
	public int getPerCount() {
		return perCount;
	}

	/**
	 * @param perCount
	 *            the perCount to set
	 */
	public void setPerCount(int perCount) {
		this.perCount = perCount;
	}

	/**
	 * �ھ���������һҳ��ʱ��������������ȷ�� �Ƿ��Ѿ�����ĩβ�ˡ�
	 */
	public boolean isTheEnd() {
		return (getCurCount() >= totalCount);
	}
}
