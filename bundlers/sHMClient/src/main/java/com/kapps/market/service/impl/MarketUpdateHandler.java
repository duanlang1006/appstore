package com.kapps.market.service.impl;

import org.xmlpull.v1.XmlPullParser;

import com.kapps.market.bean.MarketUpdateInfo;

/**
 * 2010-8-3 <br>
 * �г�����
 * 
 * @author admin
 * 
 */
public class MarketUpdateHandler extends ACheckableXmlParser {

	private MarketUpdateInfo marketUpdateInfo;

	@Override
	protected void parserContent(XmlPullParser xpp) throws Exception {
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("data")) {
					marketUpdateInfo = new MarketUpdateInfo();

				} else if (xpp.getName().equals("id")) {
					marketUpdateInfo.setId(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("name")) {
					marketUpdateInfo.setName(xpp.nextText());

				} else if (xpp.getName().equals("version")) {
					marketUpdateInfo.setVersion(xpp.nextText());

				} else if (xpp.getName().equals("vcode")) {
					marketUpdateInfo.setVersionCode(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("size")) {
					marketUpdateInfo.setSize(Integer.parseInt(xpp.nextText()) / 1024);

				} else if (xpp.getName().equals("downloadurl")) {
					marketUpdateInfo.setApkPath(xpp.nextText());

				} else if (xpp.getName().equals("description")) {
					marketUpdateInfo.setDescribe(xpp.nextText());

				} else if (xpp.getName().equals("pname")) {
					marketUpdateInfo.setPackageName(xpp.nextText());

				}

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("data")) {
					break;
				}
			}
			eventType = xpp.next();
		}
	}

	/**
	 * @return the marketUpdateInfo
	 */
	public MarketUpdateInfo getMarketUpdateInfo() {
		return marketUpdateInfo;
	}

}
