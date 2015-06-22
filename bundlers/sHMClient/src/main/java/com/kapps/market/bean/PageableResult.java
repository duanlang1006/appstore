package com.kapps.market.bean;

import java.util.List;

/**
 * 2010-7-16<br>
 * @author admin
 * 
 */
public class PageableResult {
	// ʵ�ʵ����
	private Object content;
	// ҳ����Ϣ
	private PageInfo pageInfo;

	/**
	 * @param content
	 * @param pageInfo
	 */
	public PageableResult(Object content, PageInfo pageInfo) {
		super();
		this.content = content;
		this.pageInfo = pageInfo;
	}

	/**
	 * @return the content
	 */
	public Object getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(Object content) {
		this.content = content;
	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PageableResult [content=" + (content instanceof List ? ((List) content).size() : content)
				+ ", pageInfo=" + pageInfo + "]";
	}

}
