package com.kapps.market.service.impl;

import java.sql.Date;

import org.xmlpull.v1.XmlPullParser;

import com.kapps.market.bean.AppDetail;
import com.kapps.market.util.Util;

/**
 * �������������
 * 
 * @author Administrator
 * 
 */
public class AppDetailHandler extends ACheckableXmlParser {

	// �����ϸ
	private AppDetail appDetail;

	public AppDetailHandler() {
	}

	/*
	 * (non-Javadoc)
	 */
	@Override
	protected void parserContent(XmlPullParser xpp) throws Exception {
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("item")) {
					appDetail = new AppDetail();

				} else if (xpp.getName().equals("description")) {
					appDetail.setDescribe(xpp.nextText());

				} else if (xpp.getName().equals("downloads")) {
					appDetail.setDownloadCount(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("comments")) {
					appDetail.setCommentCount(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("imagesUrl")) { // ���5��
					String imageUrls = xpp.nextText();
					if (imageUrls != null && imageUrls.trim().length() > 0) {
						appDetail.setScreenshots(imageUrls.split(","));
					}

				} else if (xpp.getName().equals("authorEmail")) {
					String email = xpp.nextText();
					appDetail.setAuthorEmail(email == null ? "" : email);

				} else if (xpp.getName().equals("authorSite")) {
					String site = xpp.nextText();
					appDetail.setAuthorSite(site == null ? "" : site);

				} else if (xpp.getName().equals("auditingTime")) {
					Date date = new Date(Long.parseLong(xpp.nextText()));
					appDetail.setAuditingTime(Util.dateFormatShort(date));
				}

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("item")) {
					break;
				}
			}
			eventType = xpp.next();
		}
	}

	/**
	 * @return the appDetail
	 */
	public AppDetail getAppDetail() {
		return appDetail;
	}

}
