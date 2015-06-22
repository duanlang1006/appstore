package com.kapps.market.service.impl;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class DownloadHandler extends DefaultHandler {

	private String url;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		if (localName.equals("item")) {
			url = atts.getValue("url");
		}
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

}
