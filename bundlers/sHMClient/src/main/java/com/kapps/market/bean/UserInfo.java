package com.kapps.market.bean;

/**
 * 2010-6-8 ��½�û�����Ϣ
 * 
 * @author admin
 * 
 */
public class UserInfo {

	// �û���(Ψһ)
	// ��߲�ͬ�ĵ�½��ʽ��������ǲ�ͬ���͡�
	private String name;
	// �û�����
	private String password;
	// �����ʼ�
	private String email;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

}
