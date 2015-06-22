package com.kapps.market.bean;

/**
 * 2010-7-9<br>
 * ������ʾ��ʱ��������õ�
 * 
 * @author admin
 * 
 */
public class SeparateAppItem extends AppItem {
	/**
	 * ��������
	 */
	private String sepDescripe;

	/**
	 * @param sepDescripe
	 */
	public SeparateAppItem(String sepDescripe) {
		this.sepDescripe = sepDescripe;
	}

	/**
	 * @return the sepDescripe
	 */
	public String getSepDescripe() {
		return sepDescripe;
	}

	/**
	 * @param sepDescripe
	 *            the sepDescripe to set
	 */
	public void setSepDescripe(String sepDescripe) {
		this.sepDescripe = sepDescripe;
	}

	@Override
	public String toString() {
		return sepDescripe;
	}

}
