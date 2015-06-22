package com.kapps.market.bean;

import java.io.Serializable;

public class KeyWord implements Serializable {
	private int type;
	private String keyword;
	/**
	 * 
	 * @param type
	 */
	public void setType(int type)
	{
		this.type=type;
	}
	/**
	 * 
	 * @param keyword
	 */
	public void setKeyword(String keyword)
	{
		this.keyword=keyword;
	}
	/**
	 * 
	 * @return type
	 */
	public int getType()
	{
		return type;
	}
	/**
	 * 
	 * @return keyword
	 */
	public String getKeyword()
	{
		return keyword;
	}
}
