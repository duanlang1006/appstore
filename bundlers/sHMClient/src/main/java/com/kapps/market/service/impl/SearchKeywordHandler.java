package com.kapps.market.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.kapps.market.bean.KeyWord;

public class SearchKeywordHandler extends ACheckableXmlParser {

	private List<KeyWord> keywordlist;
	private KeyWord kitem;


	public SearchKeywordHandler() {
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
					keywordlist = new ArrayList<KeyWord>();

				} else if (xpp.getName().equals("item")) {
					kitem = new KeyWord();

				} else if (xpp.getName().equals("category")) {
					kitem.setType(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("keyword")) {
					kitem.setKeyword(xpp.nextText());
				} 

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("item")) {
					keywordlist.add(kitem);

				} else if (xpp.getName().equals("data")) {
					break;
				}
			}
			eventType = xpp.next();
		}

	}

	public List<KeyWord> getKeyWordList() {
		return keywordlist;
	}

}
