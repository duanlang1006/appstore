package com.kapps.market.bean;

/**
 * 2010-7-16<br>
*
 * @author admin
 * 
 */
public class PageInfo {

	// ÿҳ�Ĵ�С(Ĭ��)
	private int pageSize = 10;
	// ��ǰ�Ѿ����ص��ڼ�ҳ
	private int pageIndex;
	// �ܵļ�¼��
	private int recordNum;
	// �ܹ���ҳ
	private int pageNum;

	/**
	 * @return the recordNum
	 */
	public int getRecordNum() {
		return recordNum;
	}

	/**
	 * @param recordNum
	 *            the recordNum to set
	 */
	public void setRecordNum(int recordNum) {
		this.recordNum = recordNum;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize
	 *            the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the pageIndex
	 */
	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * �����һҳ������<br>
	 * �����б��ʱ���������������
	 * 
	 * @return
	 */
	public int getNextPageIndex() {
		return pageIndex + 1;
	}

	/**
	 * @param pageIndex
	 *            the pageIndex to set
	 */
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	/**
	 * @return the pageNum
	 */
	public int getPageNum() {
		return pageNum;
	}

	/**
	 * @param pageNum
	 *            the pageNum to set
	 */
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PageInfo [pageIndex=" + pageIndex + ", pageNum=" + pageNum + ", pageSize=" + pageSize + ", recordNum="
				+ recordNum + "]";
	}

}
