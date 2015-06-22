package com.kapps.market.bean;

/**
 * 2010-7-26<br>
 * 
 * @author admin <br>
 *         <restype>��Դ����</restype> <price>��Դ�۸�</price> <payTime>����ʱ��</payTime>
 *         <identifier>�����ʶ��</identifier> <version>����汾��</version>
 *         <posterUrl>�����ַ<posterUrl> <ndaction>��ǰ��Դ����״̬</ndaction>
 *         <downloadUrl>�����Դ</downloadUrl>
 * 
 *         * ע��������ʷԭ��: �ҵķ��������صĹ����б���ʽ���� version �� identifier��<br>
 *         ���б���ͨ��id���в�����
 */
public class PurchasedApp extends BaseApp {

	// ndaction
	private String ndaction;

	public PurchasedApp() {
		super();
	}

	/**
	 * @return the ndaction
	 */
	public String getNdaction() {
		return ndaction;
	}

	/**
	 * @param ndaction
	 *            the ndaction to set
	 */
	public void setNdaction(String ndaction) {
		this.ndaction = ndaction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PurchasedApp [ndaction=" + ndaction + "]";
	}
}
