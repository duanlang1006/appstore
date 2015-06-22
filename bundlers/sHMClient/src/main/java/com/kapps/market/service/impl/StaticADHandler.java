package com.kapps.market.service.impl;

import org.xmlpull.v1.XmlPullParser;

import com.kapps.market.bean.StaticAD;

/**
 * ��̬��洦��
 * 
 * @author shuizhu
 * 
 */
public class StaticADHandler extends ACheckableXmlParser {
	// ��̬���
	private StaticAD staticAD;

	@Override
	protected void parserContent(XmlPullParser xpp) throws Exception {
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("item")) {
					staticAD = new StaticAD();

				} else if (xpp.getName().equals("id")) {
					staticAD.setId(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("name")) {
					staticAD.setName(xpp.nextText());

				} else if (xpp.getName().equals("des")) {
					staticAD.setDes(xpp.nextText());
					
				} else if (xpp.getName().equals("aid")) {
					staticAD.setAid(Integer.parseInt(xpp.nextText()));
					
				} else if (xpp.getName().equals("iconUrl")) {
					staticAD.setIconUrl(xpp.nextText());
					
				}

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("item")) {
					break;
				}
			}
			eventType = xpp.next();
		}
	}

	public StaticAD getStaticAD() {
		return staticAD;
	}

}
