package com.kapps.market.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.kapps.market.bean.AppCategory;

/**
 * 2010-7-16 ������
 * 
 * @author admin
 * 
 */

public class CategoryHandler extends ACheckableXmlParser {
	private List<AppCategory> categoryList;
	private AppCategory appCategory;
	private AppCategory subCategory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.test.BaseXmlParser#parserContent(org.xmlpull.v1.XmlPullParser)
	 */
	@Override
	protected void parserContent(XmlPullParser xpp) throws Exception {
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("data")) {
					categoryList = new ArrayList<AppCategory>();

				} else if (xpp.getName().equals("item")) {
					appCategory = new AppCategory();

				} else if (xpp.getName().equals("subitem")) {
					subCategory = new AppCategory();

				} else if (xpp.getName().equals("id")) {
					if (subCategory != null) {
						subCategory.setId(Integer.parseInt(xpp.nextText()));
					} else {
						appCategory.setId(Integer.parseInt(xpp.nextText()));
					}

				} else if (xpp.getName().equals("pid")) {
					if (subCategory != null) {
						subCategory.setPid(Integer.parseInt(xpp.nextText()));
					} else {
						appCategory.setPid(Integer.parseInt(xpp.nextText()));
					}

				} else if (xpp.getName().equals("name")) {
					if (subCategory != null) {
						subCategory.setName(xpp.nextText());
					} else {
						appCategory.setName(xpp.nextText());
					}

				} else if (xpp.getName().equals("type")) {
					if (subCategory != null) {
						subCategory.setType(Integer.parseInt(xpp.nextText()));
					} else {
						appCategory.setType(Integer.parseInt(xpp.nextText()));
					}

				} else if (xpp.getName().equals("topnames")) {
					if (subCategory != null) {
						subCategory.setTopAppName(xpp.nextText());
					} else {
						appCategory.setTopAppName(xpp.nextText());
					}

				} else if (xpp.getName().equals("iconUrl")) {
					if (subCategory != null) {
						subCategory.setIconUrl(xpp.nextText());
					} else {
						appCategory.setIconUrl(xpp.nextText());
					}

				}

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("item")) {
					categoryList.add(appCategory);

				} else if (xpp.getName().equals("subitem")) {
					appCategory.addSubCategory(subCategory);
					subCategory = null;

				} else if (xpp.getName().equals("data")) {
					break;
				}
			}
			eventType = xpp.next();
		}
	}

	/**
	 * @return the categoryList
	 */
	public List<AppCategory> getCategoryList() {
		return categoryList;
	}
}
