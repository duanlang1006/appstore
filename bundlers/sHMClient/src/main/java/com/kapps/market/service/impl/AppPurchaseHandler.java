package com.kapps.market.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.kapps.market.bean.PurchasedApp;

/**
 * �ҵĹ����б�<br>
 * ע��������ʷԭ��: ���������ص��ҵĹ����б���ʽ���� version �� identifier��<br>
 * ���б���ͨ��id���в�����
 * 
 * @author admin
 * 
 */
public class AppPurchaseHandler extends ACheckableXmlParser {

	private List<PurchasedApp> purchaseList;

	private PurchasedApp purchasedApp;

	public AppPurchaseHandler() {
	}

	/*
	 * (non-Javadoc)
	 */
	@Override
	protected void parserContent(XmlPullParser xpp) throws Exception {
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("data")) {
					purchaseList = new ArrayList<PurchasedApp>();

				} else if (xpp.getName().equals("item")) {
					purchasedApp = new PurchasedApp();

				} else if (xpp.getName().equals("id")) {
					purchasedApp.setId(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("version")) {
					purchasedApp.setVersion(xpp.nextText());

				} else if (xpp.getName().equals("versionCode")) {
					purchasedApp.setVersionCode(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("name")) {
					purchasedApp.setName(xpp.nextText());

				} else if (xpp.getName().equals("identifier")) {
					purchasedApp.setPackageName(xpp.nextText());

				} else if (xpp.getName().equals("ndaction")) {
					purchasedApp.setNdaction(xpp.nextText());

				} else if (xpp.getName().equals("payTime")) {
					// ȥ��ĩβ����
					String timeStr = xpp.nextText();
					if (timeStr != null && timeStr.trim().length() > 0) {
						int index = timeStr.lastIndexOf("");
						if (index > 0) {
							timeStr = timeStr.substring(0, index);
						}
					}
					purchasedApp.setTime(timeStr);

				} else if (xpp.getName().equals("price")) {
					purchasedApp.setPrice(Double.parseDouble(xpp.nextText()));

				} else if (xpp.getName().equals("posterUrl")) {
					purchasedApp.setIconUrl(xpp.nextText());

				} else if (xpp.getName().equals("downloadUrl")) {
					purchasedApp.setApkPath(xpp.nextText());
				}

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("item")) {
					purchaseList.add(purchasedApp);

				} else if (xpp.getName().equals("data")) {
					break;
				}
			}
			eventType = xpp.next();
		}

	}

	/**
	 * @return the purchaseList
	 */
	public List<PurchasedApp> getPurchaseList() {
		return purchaseList;
	}

}
