package com.kapps.market.service.impl;

import org.xmlpull.v1.XmlPullParser;

public class CommitDownloadHandler extends ACheckableXmlParser {

	@Override
	protected void parserContent(XmlPullParser xpp) throws Exception {
		// TODO Auto-generated method stub

		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {


			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("item")) {
					break;
				}
			}
			eventType = xpp.next();
		}
	}



}
