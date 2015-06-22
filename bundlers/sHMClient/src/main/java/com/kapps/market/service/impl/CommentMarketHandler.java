package com.kapps.market.service.impl;

import org.xmlpull.v1.XmlPullParser;

import com.kapps.market.bean.CommentMark;

public class CommentMarketHandler extends ACheckableXmlParser {

	// �������
	private CommentMark comentMark;

	public CommentMarketHandler() {
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
					comentMark = new CommentMark();

				} else if (xpp.getName().equals("id")) {
					comentMark.setId(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("butt")) {
					comentMark.setButt(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("stamp")) {
					comentMark.setStamp(Integer.parseInt(xpp.nextText()));
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
	 * @return the comentMark
	 */
	public CommentMark getComentMark() {
		return comentMark;
	}

}
