package com.kapps.market.bean;

/**
 * 2010-6-8
 * 
 * @author admin
 * 
 */
public class MarketUpdateInfo extends BaseApp {

	private static final long serialVersionUID = -1967275828078385617L;

	// �Ա��θ��µ�һ����̵�����
	private String describe;

	/**
	 * @return the describe
	 */
	public String getDescribe() {
		return describe;
	}

	/**
	 * @param describe
	 *            the describe to set
	 */
	public void setDescribe(String describe) {
		this.describe = describe;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MarketUpdateInfo [describe=" + describe + ", toString()=" + super.toString() + "]";
	}

}
