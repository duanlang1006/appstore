package com.kapps.market.service.impl;

import com.kapps.market.bean.AppItem;
import com.kapps.market.util.Util;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

/**
 * ����б�
 * 
 * @author admin
 * 
 */
public class AppListHandler extends ACheckableXmlParser {

	private List<AppItem> appList;

	private AppItem appItem;

	public AppListHandler() {
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
					appList = new ArrayList<AppItem>();

				} else if (xpp.getName().equals("item")) {
					appItem = new AppItem();

				} else if (xpp.getName().equals("id")) {
					appItem.setId(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("version")) {
					appItem.setVersion(xpp.nextText());

				} else if (xpp.getName().equals("vcode")) {
					String code = xpp.nextText();
					if(code.equals(""))
						appItem.setVersionCode(1);
					else
						appItem.setVersionCode(Integer.parseInt(code));

				} else if (xpp.getName().equals("author")) {
					String author = xpp.nextText();
					if(author.contains("google play")) {
						appItem.setAuthorName("google play");
						appItem.setPackageName(author.substring(author.indexOf("(")+1,author.indexOf(")")));
					}
					else 
						appItem.setAuthorName(author == null ? "" : author);

				} else if (xpp.getName().equals("name")) {
					appItem.setName(xpp.nextText());

				} else if (xpp.getName().equals("pkn")) {
					String temp = xpp.nextText();
					if(!appItem.getAuthorName().equals("google play"))
						appItem.setPackageName(temp);

				} else if (xpp.getName().equals("size")) {
					appItem.setSize(Integer.parseInt(xpp.nextText()) / 1024);

				} else if (xpp.getName().equals("starCount")) {
					// TODO �������ֵ��0-5
					appItem.setRating(Util.getDrawRateVaue(Float.parseFloat(xpp.nextText())));

				} else if (xpp.getName().equals("price")) {
					appItem.setPrice(Double.parseDouble(xpp.nextText()));

				} else if (xpp.getName().equals("feetype")) {
					// feetype��false���;true:����
					appItem.setFree(!Boolean.parseBoolean(xpp.nextText()));

				} else if (xpp.getName().equals("iconUrl")) {
					appItem.setIconUrl(xpp.nextText());

				} else if (xpp.getName().equals("downloadUrl")) {
					appItem.setApkPath(xpp.nextText());

				} else if (xpp.getName().equals("isBought")) {
					appItem.setPurchase(Boolean.valueOf(xpp.nextText()));

				} else if (xpp.getName().equals("adIcon")) {
					appItem.setAdIcon(xpp.nextText());

				} else if (xpp.getName().equals("adDes")) {
					appItem.setAdDes(xpp.nextText());
				} else if (xpp.getName().equals("categoryId")) {
                    appItem.setCategoryId(Integer.parseInt(xpp.nextText()));
                }

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("item")) {
					appList.add(appItem);

				} else if (xpp.getName().equals("data")) {
					break;
				}
			}
			eventType = xpp.next();
		}

	}

	public List<AppItem> getAppItemList() {
		return appList;
	}

}
