package com.kapps.market.task.mark;

/**
 * 2010-7-19 �����������������һ��˲̬��Ψһ����
 * 
 * @author admin
 * 
 */
public class AppSearchTaskMark extends APageTaskMark {

	// ��ѯ�ĸ��ֶ�
	private int type;
	// �ؼ���
	private String key;

	/**
	 * @param type
	 * @param key
	 */
	public AppSearchTaskMark(int type, String key) {
		super();
		this.type = type;
		this.key = key;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/*
	 * (non-Javadoc) ����ִ�Сд
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.toLowerCase().hashCode());
		result = prime * result + type;
		return result;
	}

	/*
	 * (non-Javadoc) ����ִ�Сд
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppSearchTaskMark other = (AppSearchTaskMark) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.toLowerCase().equals(other.key.toLowerCase()))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
