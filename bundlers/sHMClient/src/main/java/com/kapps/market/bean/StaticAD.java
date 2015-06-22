package com.kapps.market.bean;

import java.io.Serializable;

/**
 * ��̬���
 * 
 * @author shuizhu
 * 
 */
public class StaticAD implements Iconable, Serializable {

	// ����ʶ
	private int id;
	// ���push����
	private String name;
	// ���push����
	private String des;
	// ���app id
	private int aid;
	// iconUrlͼƬURL
	private String iconUrl; 
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public int getAid() {
		return aid;
	}
	public void setAid(int aid) {
		this.aid = aid;
	}
	
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	
	@Override
	public int getIconId() {
		return aid;
	}
	
	@Override
	public String getIconUrl() {
		return iconUrl;
	}
	
	@Override
	public int getIconType() {
		return MImageType.APK_ICON;
		
	}
	
}
