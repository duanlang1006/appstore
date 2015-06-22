package com.kapps.market.task.mark;

import com.kapps.market.bean.PageInfo;

/**
 * 2010-6-19 <br>
 * this task can be pagable, page1, 2, 3,??
 * 
 * @author shuizhu
 * 
 */
public abstract class APageTaskMark extends ATaskMark {

	// ҳ����Ϣ, ��ʼ��0��ʼ
	private PageInfo pageInfo = new PageInfo();

	/**
	 * @return the pageInfo
	 */
	public PageInfo getPageInfo() {
		return pageInfo;
	}

	/**
	 * @param pageInfo
	 *            the pageInfo to set
	 */
	public void setPageInfo(PageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}

	/**
	 * ����ÿҳ������
	 */
	public void setPageSize(int count) {
		pageInfo.setPageSize(count);
	}

	/**
	 * @return the loadEnd
	 */
	@Override
	public boolean isLoadEnd() {
		return pageInfo == null || (pageInfo.getPageNum() != 0 && pageInfo.getPageNum() <= pageInfo.getPageIndex())
				|| (pageInfo.getPageNum() == 0 && pageInfo.getPageIndex() != 0);
	}

	/**
	 * �Ƿ��ǵ�һ����
	 */
	public boolean isFirstLoaded() {
		if (pageInfo == null) {
			return false;

		} else {
			return (pageInfo.getPageIndex() == 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "APageTaskMark [pageInfo=" + pageInfo + ", isLoadEnd()=" + isLoadEnd() + ", toString()="
				+ super.toString() + "]";
	}

}
