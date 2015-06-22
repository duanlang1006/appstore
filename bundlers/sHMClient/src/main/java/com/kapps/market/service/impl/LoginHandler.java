package com.kapps.market.service.impl;

import org.xmlpull.v1.XmlPullParser;

import com.kapps.market.bean.LoginResult;

public class LoginHandler extends ACheckableXmlParser {

	private LoginResult loginResult;

	/**
	 * @return the loginResult
	 */
	public LoginResult getLoginResult() {
		return loginResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ck.market.service.impl.CheckableXmlParser#parserContent(org.xmlpull
	 * .v1.XmlPullParser)
	 */
	@Override
	protected void parserContent(XmlPullParser xpp) throws Exception {
		int eventType = xpp.getEventType();
		loginResult = new LoginResult();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("sid")) {
					loginResult.setSessinId(xpp.nextText());
				}

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("item")) {
					break;
				}
			}
			eventType = xpp.next();
		}

	}
}
