package com.kapps.market.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.kapps.market.bean.PageInfo;
import com.kapps.market.service.ActionException;

/**
 * 2010-7-16<br>
 * xml pull 锟斤拷锟斤拷锟斤拷锟斤拷<br>
 * 锟斤拷锟斤拷榈阶刺拷锟斤拷锟斤拷斐ｏ拷模锟街憋拷锟斤拷壮锟斤拷锟斤拷璐︼拷锟斤拷锟斤拷锟侥诧拷锟街ｏ拷锟斤拷锟斤拷锟斤拷锟杰ｏ拷<br>
 * 锟缴凤拷锟斤拷涌诓锟斤拷锟斤拷?
 * 
 * @author admin
 * 
 */
public abstract class ACheckableXmlParser {
	private static XmlPullParserFactory factory;

	// 页锟斤拷锟斤拷息
	private PageInfo pageInfo;
	private int ret;
	public ACheckableXmlParser() {
		initXmlPullParserFactory();
	}

	// 只锟斤拷一锟斤拷锟斤拷锟斤拷实锟斤拷
	private synchronized void initXmlPullParserFactory() {
		if (factory == null) {
			try {
				factory = XmlPullParserFactory.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * 
	 * @param is
	 * @throws ActionException
	 */
	public void parserXml(byte[] data) throws ActionException {
		parserXml(new ByteArrayInputStream(data), "utf-8");
	}

	/**
	 * 
	 * 
	 * @param is
	 * @throws ActionException
	 */
	public void parserXml(InputStream is) throws ActionException {
		parserXml(is, "utf-8");
	}

	/**
	 * 
	 * 
	 * @param is
	 * @param charset
	 * @throws ActionException
	 */
	public void parserXml(InputStream is, String charset) throws ActionException {
		try {
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(is, charset);
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if (xpp.getName().equals("state")) {
						parserState(xpp);

					} else if (xpp.getName().equals("data")) {
						parserContent(xpp);

					} else if (xpp.getName().equals("pinfo")) {
						parserPageinfo(xpp);
					}
				}
				eventType = xpp.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ActionException) {
				throw (ActionException) e;

			} else {
				throw new ActionException(ActionException.RESULT_ERROR, "parseXml fial 94 line");
			}
		}
	}

	/**
	 * 锟斤拷锟斤拷状态, 锟斤拷锟斤拷锟揭拷锟街憋拷锟斤拷壮锟斤拷斐ｏ拷锟�
	 * 
	 * @param xpp
	 * @throws Exception
	 */
	protected void parserState(XmlPullParser xpp) throws Exception {
		ActionException se = null;
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("state")) {
					se = new ActionException();

				} else if (xpp.getName().equals("code")) {
					ret=Integer.parseInt(xpp.nextText());
					se.setExCode(ret);

				} else if (xpp.getName().equals("msg")) {
					se.setExMessage(xpp.nextText());
				}

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("state")) {
					break;
				}
			}
			eventType = xpp.next();
		}

		// 锟叫达拷锟斤拷锟斤拷锟斤拷锟阶筹拷
		if (se.getExCode() != 0) {
			throw se;
		}
	}

	/**
	 * 锟斤拷锟揭筹拷锟斤拷锟斤拷锟斤拷锟截碉拷统锟斤拷锟斤拷息
	 * 
	 * @param xpp
	 * @throws Exception
	 */
	protected void parserPageinfo(XmlPullParser xpp) throws Exception {
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("pinfo")) {
					pageInfo = new PageInfo();

				} else if (xpp.getName().equals("rn")) { // 锟斤拷锟斤拷锟斤拷锟斤拷锟�
					pageInfo.setRecordNum(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("ps")) { // 每页锟斤拷锟斤拷
					pageInfo.setPageSize(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("pi")) { // 锟斤拷前锟节硷拷页
					pageInfo.setPageIndex(Integer.parseInt(xpp.nextText()));

				} else if (xpp.getName().equals("pn")) { // 一锟斤拷锟叫硷拷页
					pageInfo.setPageNum(Integer.parseInt(xpp.nextText()));
				}

			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("pinfo")) {
					break;
				}
			}
			eventType = xpp.next();
		}
	}

	/**
	 * @return the pageInfo
	 */
	public PageInfo getPageInfo() {
		return pageInfo;
	}
	/**
	 * 
	 * @return state code
	 */
	public int getState()
	{
		return ret;
	}
	protected abstract void parserContent(XmlPullParser xpp) throws Exception;

}
