package com.kapps.market.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.kapps.market.bean.AppComment;
import com.kapps.market.util.Util;

public class CommentsHandler extends ACheckableXmlParser {

	private List<AppComment> appCommentList;
	private AppComment appComment;

	public CommentsHandler() {
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
					appCommentList = new ArrayList<AppComment>();

				} else if (xpp.getName().equals("item")) {
					appComment = new AppComment();

				} else if (xpp.getName().equals("id")) {
					appComment.setId(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("aid")) {
					appComment.setAppId(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("nick")) {
					appComment.setAuthor(xpp.nextText());

				} else if (xpp.getName().equals("time")) {
					appComment.setTime(xpp.nextText().toString());

				} else if (xpp.getName().equals("starCount")) {
					appComment.setRating(Util.getDrawRateVaue(Float.parseFloat(xpp.nextText())));

				} else if (xpp.getName().equals("content")) {
					appComment.setContent(xpp.nextText());

				} else if (xpp.getName().equals("butt")) {
					appComment.setButt(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("stamp")) {
					appComment.setStamp(Integer.parseInt(xpp.nextText()));
				}

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("item")) {
					appCommentList.add(appComment);

				} else if (xpp.getName().equals("data")) {
					break;
				}
			}
			eventType = xpp.next();
		}

	}

	public List<AppComment> getAppCommentList() {
		return appCommentList;
	}

}
