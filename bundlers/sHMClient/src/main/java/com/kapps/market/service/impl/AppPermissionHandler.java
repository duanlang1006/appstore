package com.kapps.market.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.kapps.market.bean.AppPermission;

/**
 * ����б�
 * 
 * @author admin
 * 
 */
public class AppPermissionHandler extends ACheckableXmlParser {

	private List<AppPermission> permissionList;

	private AppPermission appPermission;

	public AppPermissionHandler() {
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
					permissionList = new ArrayList<AppPermission>();

				} else if (xpp.getName().equals("item")) {
					appPermission = new AppPermission();

				} else if (xpp.getName().equals("title")) {
					appPermission.setTitle(xpp.nextText());

				} else if (xpp.getName().equals("des")) {
					appPermission.setDes(xpp.nextText());

				} else if (xpp.getName().equals("hide")) {
					appPermission.setHide("0".equals(xpp.nextText()) ? false : true);
				}

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("item")) {
					permissionList.add(appPermission);

				} else if (xpp.getName().equals("data")) {
					break;
				}
			}
			eventType = xpp.next();
		}

	}

	public List<AppPermission> getAppPermissionList() {
		return permissionList;
	}

}
