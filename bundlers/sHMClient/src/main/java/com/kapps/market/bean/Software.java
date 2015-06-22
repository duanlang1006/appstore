package com.kapps.market.bean;

/**
 * 2010-7-12<br>
 * Software appitem
 * 
 * @author admin
 * 
 */
public class Software extends BaseApp implements Cloneable {
	//
	private static final long serialVersionUID = 20100712L;

	// �Ƿ��и���
	private boolean update;
	// �Ƿ��Ѿ������������Ϣ��,����apk�����������Ϣ�ֲ����ء�
	private boolean infoFull;

	/**
	 * @return the update
	 */
	public boolean isUpdate() {
		return update;
	}

	/**
	 * @param update
	 *            the update to set
	 */
	public void setUpdate(boolean update) {
		this.update = update;
	}

	/**
	 * @return the infoFull
	 */
	public boolean isInfoFull() {
		return infoFull;
	}

	@Override
	public int getIconType() {
		return MImageType.APK_ICON;
	}

	/**
	 * @param infoFull
	 *            the infoFull to set
	 */
	public void setInfoFull(boolean infoFull) {
		this.infoFull = infoFull;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getPackageName() == null) ? 0 : getPackageName().hashCode());
		result = prime * result + getVersionCode();
		return result;
	}

	/*
	 * (non-Javadoc)
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
		BaseApp other = (BaseApp) obj;
		if (getPackageName() == null) {
			if (other.getPackageName() != null)
				return false;
		} else if (!getPackageName().equals(other.getPackageName()))
			return false;
		if (getVersionCode() != other.getVersionCode())
			return false;
		return true;
	}
}
