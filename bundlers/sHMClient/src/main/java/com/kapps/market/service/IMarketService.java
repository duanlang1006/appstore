package com.kapps.market.service;

import java.util.List;


import com.kapps.market.bean.AppBadness;
import com.kapps.market.bean.AppCategory;
import com.kapps.market.bean.AppComment;
import com.kapps.market.bean.AppDetail;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.AppPermission;
import com.kapps.market.bean.CommentMark;
import com.kapps.market.bean.KeyWord;
import com.kapps.market.bean.LoginResult;
import com.kapps.market.bean.MarketUpdateInfo;
import com.kapps.market.bean.PageableResult;
import com.kapps.market.bean.Software;
import com.kapps.market.bean.StaticAD;
import com.kapps.market.bean.UserInfo;
import com.kapps.market.bean.config.ContextConfig;
import com.kapps.market.util.Constants;

/**
 * 2010-6-8 �г��������ӿ�
 * 
 * @author admin
 * 
 */
public interface IMarketService extends Constants {

	/**
	 * ע���µ��û�
	 * 
	 * @param user
	 *            ����ע�����Ϣ
	 * @param sign
	 *            ��ȫ��ʶ
	 * @return true:�ɹ���flase:ʧ��
	 */
	public void register(UserInfo user, String sign) throws ActionException;

	/**
	 * �û���¼�ӿ�
	 * 
	 * @param user
	 * @param deviceId
	 * @param simId
	 * @return
	 * @throws ActionException
	 */
	public LoginResult login(UserInfo user, String deviceId, String simId, String sign) throws ActionException;

	/**
	 * ����г�����ĸ�����Ϣ
	 * 
	 * @param contextConfig
	 *            ��ǰ�г���һ����Ϣ��װ
	 * @return ������Ϣ��һ����װ �����û�и����򷵻�null��
	 */
	public MarketUpdateInfo checkMarketUpdate(ContextConfig contextConfig) throws ActionException;

	/**
	 * 
	 * ������¼��
	 * 
	 * @param softwareList
	 *            ��Ҫ�����µ����
	 * @return
	 * @throws ActionException
	 */
	public List<AppItem> checkSoftwareUpdate(List<Software> softwareList) throws ActionException;

	/**
	 * ���е���������б�
	 * 
	 * @return ���
	 */
	public List<AppCategory> getAppCategoryList() throws ActionException;

	/**
	 * TopDownload �����
	 * 
	 * @return
	 * @throws ActionException
	 */
	public PageableResult getAppListByTopDownload(int sorttype, int pi, int ps) throws ActionException;

	/**
	 * ��ȡĳһ����µ���� Ĭ���Ѷ�Ӧfilter�Ľ�������
	 * 
	 * @param categoryId
	 *            ������������
	 * @param sortType
	 *            ��������
	 * @param feeType
	 *            �Ƿ�֧�� 0: ������ 0:�շѷ�, 1:��ѷ�
	 * @param pageIndex
	 *            ��ʼ����
	 * @param perCount
	 *            ���λ�ȡ��¼��
	 * @return Ӧ�����б�
	 */
	public PageableResult getAppListByCategory(int categoryId, int sortType, int feeType, int pageIndex, int perCount)
			throws ActionException;

	/**
	 * ��ȡĳһ�Ƽ�����µ���� Ĭ���Ѷ�Ӧfilter�Ľ�������
	 * 
	 * @param recommendId
	 *            ������������
	 * @param sortType
	 *            ��������
	 * @param pageIndex
	 *            ��ʼ����
	 * @param perCount
	 *            ���λ�ȡ��¼��
	 * @return Ӧ�����б�
	 */
	public PageableResult getAppListByRecommend(int recommendId, int sortType, int pageIndex, int perCount)
			throws ActionException;

	/**
	 * �����ϼܽӿ�1004
	 * 
	 * @param pageIndex
	 *            ҳ������
	 * @param pageCount
	 *            ҳ������
	 * @return
	 */
	public PageableResult getNewsAppList(int pageIndex, int pageCount) throws ActionException;

	/**
	 * ��ù�����
	 * 
	 * @param categoryId
	 *            ��Ӧ����id�������Constants.NONE_ID��������ʾ��Ҫ�����б�������
	 * 
	 * @return
	 */
	public PageableResult getAdvertiseApps(int categoryId, int pageIndex, int perCount) throws ActionException;

	/**
	 * ��ݿ����ߵõ���������߿���������Ӧ�ó����б�
	 * 
	 * @param developer
	 *            ������(�ں�̨�����ߵ����ֱ�����Ψһ��)
	 * @param start
	 *            ��ʼ����
	 * @param count
	 *            ���λ�ȡ��¼��
	 * @return Ӧ�����б�
	 */
	public PageableResult getAppListByDeveloper(String developer, int start, int count) throws ActionException;

	/**
	 * ����������ʷ�汾
	 * 
	 * @param appId
	 * @return
	 * @throws ActionException
	 */
	public PageableResult getHistoryAppList(int appId, String pname) throws ActionException;

	/**
	 * ��������Ȩ���б�
	 * 
	 * @param appId
	 *            �����id
	 * @return
	 * @throws ActionException
	 */
	public List<AppPermission> getAppPermissionList(int appId) throws ActionException;

	/**
	 * ����������ϸ
	 * 
	 * @param appId
	 *            ���id
	 * @return
	 * @throws ActionException
	 */
	public AppDetail getAppDetailById(int appId) throws ActionException;

	/**
	 * ���packagename versionCode �õ�AppItem
	 * 
	 * @param pname
	 *            Ӧ�õİ���
	 * @param versionCode
	 *            �汾��
	 * @return
	 */
	public AppItem getAppSummary(String pname, int versionCode) throws ActionException;

	/**
	 * ���ID�õ�APPITEM
	 * 
	 * @param appid
	 *            Ӧ�õ�id
	 * @return
	 */
	public AppItem getAppSummaryById(int appid) throws ActionException;

	/**
	 * ���Ӧ�õ�����
	 * 
	 * @param appId
	 *            Ӧ��id
	 * @param pageIndex
	 *            ��ʼ
	 * @param perCount
	 *            ����
	 * @return
	 */
	public PageableResult getAppCommentList(int appId, String pname, int pageIndex, int perCount)
			throws ActionException;

	/**
	 * �ύ������Ϣ
	 * 
	 * @param appBadness
	 *            ������Ϣ
	 */
	public void commitBadnessContent(AppBadness appBadness, String sign) throws ActionException;

	/**
	 * �ύ�ҵ�����<br>
	 * �û���Ϣ�ɷ���ά��
	 * 
	 * @param appComment
	 *            ����
	 * @return �Ƿ����۳ɹ�
	 */
	public void commitAppComment(AppComment appComment, String pname, String sign) throws ActionException;

	/**
	 * �ύ�ҵ�����<br>
	 * �û���Ϣ�ɷ���ά��
	 * 
	 * @param commentId
	 *            ����
	 * @param mark
	 *            ���۵ı��<br>
	 *            0: ���õ����ۣ�1:���õ�����
	 * @return CommentMark ���۱�ǳɹ���ĲȺͶ�
	 */
	public CommentMark commitAppCommentMark(int commentId, int mark) throws ActionException;

	/**
	 * ��ѯ���
	 * 
	 * @param type
	 *            ��ô��
	 * @param key
	 *            �ؼ���
	 * @return
	 */
	public PageableResult searchAppByCondition(int type, String key, int pageIndex, int perCount)
			throws ActionException;

	/**
	 * ����û�ϲ�õ�Ӧ�����<br>
	 * ����ӿ�ֻһ��ֻ����ˢ�����ݶ�ʧ��ʱ��<br>
	 * ���������������¼���Ա�ָ���
	 * 
	 * @return
	 */
	public PageableResult getAppFavorList(int pageIndex, int perCount) throws ActionException;

	/**
	 * 
	 * �ύ�û�ϲ����Ϣ
	 * 
	 * @param appId
	 *            ���id
	 * 
	 * @return
	 * @throws ActionException
	 */
	public void addFavorAppItem(int appId, String sign) throws ActionException;

	/**
	 * ɾ���û���ϲ����Ϣ
	 * 
	 * @param appId
	 * @return
	 * @throws ActionException
	 */
	public void deleteFavorAppItem(int appId, String sign) throws ActionException;

	/**
	 * ͬurl���ָ������Դ��һ������ͼƬ��ȡ
	 * 
	 * @param appId
	 *            Ӧ�õ�id
	 * @param url
	 *            ͼƬurl
	 * @param type
	 *            ͼƬ���ͣ���ϻ���
	 * @param sign
	 */
	public byte[] getAppImageResource(int appId, String url, int type, String sign) throws ActionException;

	/**
	 * ����������
	 * 
	 * @param did
	 *            �豸id(imei�������ַ)
	 * @param cid
	 *            ����id
	 * @param createTime
	 *            �ύʱ��
	 * @param sign
	 *            ��ȫ��֤
	 * @return
	 */
	public void reportChannel(String did, String cid, long createTime, String sign) throws ActionException;

	/**
	 * ��ȡhtml���
	 * 
	 * @param oldAdId
	 *            �͵Ĺ��id
	 * @return ����װ��Ϣ
	 */
	public StaticAD checkStaticAD(long oldAdId) throws ActionException;
	/**
	 * 
	 * @param id
	 * @param sign
	 * @param ei
	 * @param si
	 * @return
	 * @throws ActionException
	 */
	public Integer commitDownloadRecord(String id, String sign, String ei, String si)
			throws ActionException;
	
	public Integer commitMarketDownloadRecord(Integer id, String sign,String ei)
			throws ActionException;
	/**
	 * 
	 * @param sign
	 * @return
	 * @throws ActionException
	 */
	public List<KeyWord> getSearchKeyword() throws ActionException;

	public Integer commitMarketDownloadRecordFirst(int vcode, String sign)
			throws ActionException;

	PageableResult getAppListByTopDownloadFirstPage(int sorttype, int pi, int ps)
			throws ActionException;

}
