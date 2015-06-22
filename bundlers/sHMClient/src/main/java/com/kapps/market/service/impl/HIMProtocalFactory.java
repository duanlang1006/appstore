package com.kapps.market.service.impl;

import java.util.List;

import com.kapps.market.bean.Software;
import com.kapps.market.service.ActionException;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

/**
 * �����Ľ���������Э����ݣ���������ȡ�
 * 
 * @author Administrator �����˷��͵�����ķ�װ
 */
public class HIMProtocalFactory implements Constants {

	/**
	 * ������еķ��࣬������Ӧ�������Ϸ����Ƽ�����������
	 */
	public static final int QUEIRY_ALL_APP_CATEGORY = 1008;
	/**
	 * �������µ�����б�
	 */
	private final int QUERY_CATEGORY_APP_LIST = 1001;
	/**
	 * ����Ƽ�����µ����
	 */
	private final int QUERY_RECOMMEND_APP_LIST = 1002;

	/**
	 * �����Ǽܴ��ӿ�
	 */
	private final int QUERY_NEWS_APP_LIST = 1004;

	/**
	 * ������������б�
	 */
	private final int QUERY_DOWN_HOST_APP_LIST = 1010;

	private final int QUERY_DOWN_HOST__FIRST_PAGE_APP_LIST = 1110;
	/**
	 * ���ϲ���б�
	 */
	private final int QUERY_FAVOR_APP_LIST = 2007;
	/**
	 * ��ѯ�����ϸ
	 */
	private final int QUERY_APP_DETAIL = 1012;

	/**
	 * ��ѯ���������
	 */
	private final int QUERY_APP_COMMENT = 1005;
	/**
	 * ��ѯ����������Ϸ�б�
	 */
	private final int QUERY_ADVERTISE_APP = 1000;
	/**
	 * ��ѯ�����ʷ�汾
	 */
	private final int QUERY_HISTORY_APP = 1006;
	/**
	 * ��ѯĳ�����ߵ����
	 */
	private final int QUERY_DEVELOPER_APP = 1009;
	/**
	 * �ύ���������
	 */
	private final int COMMIT_APP_COMMENT = 2002;
	/**
	 * �ύ��������۵ر��
	 */
	private final int COMMIT_APP_COMMENT_MARK = 1017;
	/**
	 * �ύ�ղ�Ӧ��
	 */
	private final int ADD_FAVOR_APP = 2005;
	/**
	 * ɾ��ϲ��Ӧ��
	 */
	private final int DELETE_FAVOR_APP = 2006;
	/**
	 * ����Ӧ��
	 */
	private final int SEARCH_APP = 1013;
	/**
	 * get search keyword
	 */
	private final int GET_SEARCH_KEYWORD = 4001;
	/**
	 * ��ѯ���Ȩ��
	 */
	private final int QUERY_APP_PERMISSION = 1007;
	/**
	 * �û���½
	 */
	public static final int USER_LOGIN = 2000;
	/**
	 * �û�ע��
	 */
	public static final int USER_REGISTE = 2001;
	/**
	 * �г�����
	 */
	public static final int MARKET_UPDATE = 1015;

	/**
	 * �������
	 */
	public static final int SOFTWARE_UPDATE = 1016;
	/**
	 * �ύ������ľٱ�
	 */
	public final int COMMIT_APP_BADNESS = 2003;
	/**
	 * ��ѯ����ĸ�Ҫ��ͨ�����id
	 */
	public final int QUERY_APP_SUMMARY_BY_ID = 1011;

	/**
	 * ��ѯ�����Ҫ �� ͨ��pname��version
	 */
	public final int QUERY_APP_SUMMARY = 1025;

	/**
	 * ��þ�̬ҳ����
	 */
	public static final int QUERY_STATIC_AD = 1014;

	/**
	 * ��������id
	 */
	public final int REPORT_CHANNEL_ID = 2004;
	/**
	 * 
	 */
	public final int REPROT_DOWNLOAD_RESULT = 4000;
	
	public final int REPROT_MARKET_DOWNLOAD_RESULT = 3901;
	// Э��ͬ
	public static final String XMLHEAD = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n";

	public HIMProtocalFactory() {

	}

	public ProtocalWrap loginProtocal(String user, String pwd, String imei,
			String imsi, String sign) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + USER_LOGIN + "&user="
				+ Util.encodeContentForUrl(user) + "&pwd=" + pwd + "&chid="
				+ imei + "&imsi=" + imsi + "&sign=" + sign);
		return pw;
	}

	public ProtocalWrap registeProtocal(String user, String pwd, String sign) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + USER_REGISTE + "&user="
				+ Util.encodeContentForUrl(user) + "&pwd=" + pwd + "&sign="
				+ sign);
		return pw;
	}

	public ProtocalWrap queryAppItemsByCategoryProtocal(int categoryId,
			int sortType, int feeType, int pageIndex, int perCount) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_CATEGORY_APP_LIST + "&cid=" + categoryId
				+ "&sorttype=" + sortType + "&feeType=" + feeType + "&pi="
				+ pageIndex + "&ps=" + perCount);
		return pw;
	}

	public ProtocalWrap queryAppItemsByRecommendProtocal(int recommendId,
			int sortType, int pageIndex, int perCount) throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_RECOMMEND_APP_LIST + "&rid=" + recommendId
				+ "&sorttype=" + sortType + "&pi=" + pageIndex + "&ps="
				+ perCount);
		return pw;
	}

	public ProtocalWrap queryAppItemsByDownloadProtocal(int sortType,
			int pageIndex, int perCount) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_DOWN_HOST_APP_LIST + "&sorttype="
				+ sortType + "&pi=" + pageIndex + "&ps=" + perCount);
		return pw;
	}
	public ProtocalWrap queryAppItemsByDownloadFirstPageProtocal(int sortType,
			int pageIndex, int perCount) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_DOWN_HOST__FIRST_PAGE_APP_LIST + "&sorttype="
				+ sortType + "&pi=" + pageIndex + "&ps=" + perCount);
		return pw;
	}
	public ProtocalWrap queryNewsAppItemsProtocal(int pageIndex, int perCount) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_NEWS_APP_LIST + "&pi=" + pageIndex + "&ps="
				+ perCount);
		return pw;
	}

	public ProtocalWrap queryAdvertiseAppsProtocal(int popType, int pageIndex,
			int perCount) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_ADVERTISE_APP + "&poptype=" + popType
				+ "&pi=" + pageIndex + "&ps=" + perCount);
		return pw;
	}

	public ProtocalWrap queryAppItemsByDeveloperProtocal(String developer,
			int pageIndex, int perCount) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_DEVELOPER_APP + "&autor="
				+ Util.encodeContentForUrl(developer) + "&pi=" + pageIndex
				+ "&ps=" + perCount);
		return pw;
	}

	public ProtocalWrap queryHistoryAppItemsProtocal(int appId, String pname)
			throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_HISTORY_APP + "&aid=" + appId + "&pname="
				+ Util.encodeContentForUrl(pname));
		return pw;
	}

	public ProtocalWrap queryAppDetailProtocal(int appId)
			throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_APP_DETAIL + "&aid=" + appId);
		return pw;
	}

	public ProtocalWrap queryAppSummaryProtocal(String pname, int versionCode)
			throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		if (versionCode < 0) {
			pw.setGetData("op=" + QUERY_APP_SUMMARY + "&pname=" + pname);
		} else {
			pw.setGetData("op=" + QUERY_APP_SUMMARY + "&pname=" + pname
					+ "&vcode=" + versionCode);
		}
		return pw;
	}

	public ProtocalWrap queryAppSummaryProtocal(int appId)
			throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_APP_SUMMARY_BY_ID + "&aid=" + appId);
		return pw;
	}

	public ProtocalWrap queryCategoryProtocal() {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUEIRY_ALL_APP_CATEGORY);
		return pw;
	}

	public ProtocalWrap queryAppCommentsProtocal(int appId, String pname,
			int pageIndex, int perCount) throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_APP_COMMENT + "&aid=" + appId + "&pname="
				+ pname + "&pi=" + pageIndex + "&ps=" + perCount);
		return pw;
	}

	public ProtocalWrap commitAppCommentProtocal(int appId, String pname,
			double rating, String content, String sign) throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + COMMIT_APP_COMMENT + "&aid=" + appId + "&pname="
				+ pname + "&rate=" + rating + "&comment="
				+ Util.encodeContentForUrl(content) + "&sign=" + sign);
		return pw;
	}

	public ProtocalWrap commitAppCommentMarkProtocal(int cId, int mark)
			throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + COMMIT_APP_COMMENT_MARK + "&cid=" + cId
				+ "&mark=" + mark);
		return pw;
	}

	public ProtocalWrap commitAppBadnessProtocal(int appId, int badnessIndex,
			String content, String sign) throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + COMMIT_APP_BADNESS + "&aid=" + appId
				+ "&badtype=" + badnessIndex + "&badness="
				+ Util.encodeContentForUrl(content) + "&sign=" + sign);
		return pw;
	}

	public ProtocalWrap queryAppByConditionProtocal(int type, String key,
			int pageIndex, int perCount) throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + SEARCH_APP + "&searchtype=" + type
				+ "&searchkey=" + Util.encodeContentForUrl(key) + "&pi="
				+ pageIndex + "&ps=" + perCount + "&ver=1");
		return pw;
	}

	public ProtocalWrap addFavorAppItemProtocal(int appId, String sign) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + ADD_FAVOR_APP + "&aid=" + appId + "&sign=" + sign);
		return pw;
	}

	public ProtocalWrap deleteFavorAppItemProtocal(int appId, String sign) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + DELETE_FAVOR_APP + "&aid=" + appId + "&sign="
				+ sign);
		return pw;
	}

	public ProtocalWrap queryFavorAppListProtocal(int pageIndex, int perCount) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_FAVOR_APP_LIST + "&pi=" + pageIndex
				+ "&ps=" + perCount);
		return pw;
	}

	public ProtocalWrap queryAppPermissionProtocal(int appId)
			throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_APP_PERMISSION + "&aid=" + appId);
		return pw;
	}

	public ProtocalWrap getAppImageResouceProtocal(int appId, String url,
			String sign) throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setHost(HttpMarketService.BASE_URL + url + "&sign=" + sign);
		pw.setSoTimeout(5000);
		pw.setReTry(false); // ͼƬ������
		return pw;
	}

	public ProtocalWrap checkMarketUpdateProtocal(String vender, int versionCode)
			throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + MARKET_UPDATE + "&vcode=" + versionCode);
		return pw;
	}

	public ProtocalWrap checkSoftwareUpdateProtocal(List<Software> softwareList) {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + SOFTWARE_UPDATE);

		StringBuilder sb = new StringBuilder();
		sb.append(XMLHEAD);
		sb.append("<reason><data>");
		for (Software software : softwareList) {
			sb.append("<item>");
			sb.append("<pname>" + software.getPackageName() + "</pname><vcode>"
					+ software.getVersionCode() + "</vcode>");
			sb.append("</item>");
		}
		sb.append("</data></reason>");
		pw.setPostData(sb.toString());

		return pw;
	}

	public ProtocalWrap getChannelReportProtocal(String did, String cid,
			long createTime, String sign) throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + REPORT_CHANNEL_ID + "&dhid="
				+ Util.encodeContentForUrl(did) + "&chid="
				+ Util.encodeContentForUrl(cid) + "&sign=" + sign);
		return pw;
	}

	public ProtocalWrap checkStaticAD(long oldAdId) throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + QUERY_STATIC_AD + "&oldpushid=" + oldAdId);
		return pw;

	}

	public ProtocalWrap queryCommitDownloadProtocal(String aid, String sign,
			String ei, String cid) throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + REPROT_DOWNLOAD_RESULT + "&aids=" + aid
				+ "&dhid=" + ei + "&chid=" + cid + "&sign=" + sign);
		return pw;

	}

	public ProtocalWrap queryCommitMarketDownloadProtocal(Integer aid, String sign,
			String ei) throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + REPROT_MARKET_DOWNLOAD_RESULT + "&aid=" + aid
				+ "&dhid=" + ei + "&sign=" + sign);
		return pw;

	}
	public ProtocalWrap queryCommitMarketDownloadFirstProtocal(int vcode, String sign
			) throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + REPROT_MARKET_DOWNLOAD_RESULT + "&vcode=" + vcode
				 + "&sign=" + sign);
		return pw;

	}
	public ProtocalWrap querySearchKeywordProtocal(String sign)
			throws ActionException {
		ProtocalWrap pw = new ProtocalWrap();
		pw.setGetData("op=" + GET_SEARCH_KEYWORD + "&sign=" + sign);
		return pw;

	}
}
