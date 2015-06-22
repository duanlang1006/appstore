package com.kapps.market.service;

import java.util.List;
import java.util.Map;

import com.kapps.market.bean.DownloadItem;
import com.kapps.market.bean.Software;

/**
 * 2010-6-28
 * 
 * @author admin
 * 
 */
public interface ILocalService {

	/**
	 * 
	 * @return
	 */
	public List<Software> initSoftwareSummaryInfoList();

	/**
	 * �������б���ϸ��Ϣ
	 * 
	 * @param softwareList
	 *            ����б�
	 * @return
	 */
	public List<Software> initSoftwareDetailInfoList(List<Software> softwareList);

	/**
	 * ���apk�ļ��ĸ�Ҫ��Ϣ
	 * 
	 * @param oldList
	 *            �����е����ڲ��յľ�apk�б?һ������ˢ��apk�б���ͼ��
	 * @param cacheMark
	 *            ������
	 * @param dir
	 *            ������Ŀ¼ ��������sdcard�����һ��Ŀ¼��
	 * @return
	 */
	public List<Software> initApkSummaryInfoList(List<Software> oldList, String cacheMark, String dir);

	/**
	 * ���apk����Ϣ��Ϣ
	 * 
	 * @param apkList
	 * @param cacheMark
	 *            ������
	 * @return
	 */
	public List<Software> initApkDetailInfoList(List<Software> apkList, String cacheMark);

	/**
	 * ��ʼ�������б����������/�������/����ʧ��
	 * 
	 * @return
	 */
	public Map<Integer, List<DownloadItem>> initDownloadTaskMap();

	/**
	 * ��鱾�ػ���
	 */
	public void checkLocalCache();

}
