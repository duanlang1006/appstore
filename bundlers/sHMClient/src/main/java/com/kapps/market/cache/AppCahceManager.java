package com.kapps.market.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.kapps.market.bean.AppCategory;
import com.kapps.market.bean.AppComment;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.KeyWord;
import com.kapps.market.bean.PurchasedApp;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.CommentsTaskMark;

/**
 * 2010-6-8
 * 
 * @author admin <br>
 *         ��Ϊ��ͼһ�������ʾ10�����ҵ�Ӧ��, <br>
 *         �������� �ж�Ӧ�����ҷ�ϲ�ѯ������Ӧ�õĸ�ʽ>=10��ʱ��ֱ�Ӿʹӻ��� ��ȡ�ã�<br>
 *         ����Ĳ�ѯ��ȥ��������ѯ�������ṩ��ʱ���ı����Ļ��档
 * 
 * 
 */
public final class AppCahceManager {

	public static final String TAG = "AppCahceManager";

	public static final boolean logCache = true;

	// �������Ϣ����
	// ά����ӵ�˳��
	// key: ������������Ļ��� value: ��Ӧ���������id
	private Map<ATaskMark, ArrayList<Integer>> appIdMapCache;
	// �������
	// key: ���id(��ݿ�Ψһid) value: ���
	// appTypeCache size ��һ������ appItemCache ��size
	private Map<Integer, AppItem> appItemMapCache;
	// Ӧ�õ����
	private List<AppCategory> appCategoryList;
	// �ҵĹ���
	private List<PurchasedApp> purchasedList;
	
	private List<KeyWord> keywordList;

	public AppCahceManager() {
		appIdMapCache = new HashMap<ATaskMark, ArrayList<Integer>>();
		appItemMapCache = new HashMap<Integer, AppItem>();
		appCategoryList = new ArrayList<AppCategory>();
		purchasedList = new ArrayList<PurchasedApp>();
		keywordList=new ArrayList<KeyWord>();
	}

	public void addKeyWordToCache(List<KeyWord> list)
	{
		if(list!=null)
		{
			for(KeyWord lt:list)
			{
				keywordList.add(lt);
			}
		}
	}
	public List<KeyWord> getKeyWordList()
	{
		return keywordList;
	}
	/**
	 * �������ӵ����� ��Ʒ����ĩβ��ʼ��ӡ�<br>
	 * ����һ���Կ��ǲ����һ���Ӧ�ã�����Ҫ���ּ����б?
	 * 
	 * @param start
	 *            ��ʼ����
	 * @param appItems
	 * 
	 */
	public void addAppItemToCache(ATaskMark type, List<? extends AppItem> appItems) {
		if (type == null) {
			throw new IllegalArgumentException("AppTaskWraper can not null.");
		}
		ArrayList<Integer> idList = getAppIdList(type);
		int id = 0;
		for (AppItem appItem : appItems) {
			id = appItem.getId();
			// ��������ӵ�ĩβ
			if (!idList.contains(id)) {
				idList.add(id);
			}
			// ����ʱ�����ǡ�
			if (!appItemMapCache.containsKey(id)) {
				appItemMapCache.put(id, appItem);
			}
		}
		if (logCache) {
			Log.d(TAG, "addAppItemToCache type: " + type + " count: " + appItems.size() + " item size: "
					+ idList.size() + " item type: size: " + appItemMapCache.size());
		}
	}

	/**
	 * ������滻�ɵ�����б?�������Ϣ��Ȼ���֡�<br>
	 * ����һ���Կ��ǲ����һ���Ӧ�ã�����Ҫ���ּ����б?<br>
	 * see: ����ĸ���
	 * 
	 * @param start
	 *            ��ʼ����
	 * @param appItems
	 * 
	 */
	public void setAppItemToCache(ATaskMark type, List<? extends AppItem> appItems) {
		if (type == null) {
			throw new IllegalArgumentException("AppTaskWraper can not null.");
		}
		ArrayList<Integer> idList = getAppIdList(type);
		// �Ƴ�ɵ�
		idList.clear();
		int id = 0;
		for (AppItem appItem : appItems) {
			id = appItem.getId();
			idList.add(id);
			// ����ʱ�����ǡ�
			if (!appItemMapCache.containsKey(id)) {
				appItemMapCache.put(id, appItem);
			}
		}
		if (logCache) {
			Log.d(TAG, "addAppItemToCache type: " + type + " count: " + appItems.size() + " item size: "
					+ idList.size() + " item type: size: " + appItemMapCache.size());
		}
	}

	/**
	 * ���һ�����
	 * 
	 * @param type
	 *            ����
	 * @param appItem
	 *            ���
	 */
	public void addAppItemToCache(ATaskMark type, AppItem appItem) {
		if (type == null) {
			throw new IllegalArgumentException("AppTaskWraper can not null.");
		}
		ArrayList<Integer> idList = getAppIdList(type);
		int id = appItem.getId();
		// ��������ӵ�ĩβ
		if (!idList.contains(id)) {
			idList.add(id);
		}
		// ����ʱ�����ǡ�
		if (!appItemMapCache.containsKey(id)) {
			appItemMapCache.put(id, appItem);
		}
		if (logCache) {
			Log.d(TAG, "addAppItemToCache type: " + type + " appItem: " + appItem + " item size: " + idList.size()
					+ " item type: size: " + appItemMapCache.size());
		}
	}

	/**
	 * ���һ�����
	 * 
	 * @param appItem
	 *            ���
	 */
	public void addAppItemToCache(AppItem appItem) {
		int id = appItem.getId();
		// ����ʱ�����ǡ�
		if (!appItemMapCache.containsKey(id)) {
			appItemMapCache.put(id, appItem);
		}
		if (logCache) {
			Log.d(TAG, "addAppItemToCache appItem:" + appItem + " size:" + appItemMapCache.size());
		}
	}

	/**
	 * ɾ��ָ�����͵�Ӧ�ã�ע�������ʵ����ֻɾ��id�б?ʵ�ʵ� ��ݻ��Ǵ��ڵġ�
	 * 
	 * @param type
	 * @param id
	 *            id�����Ƕ�������
	 */
	public void deleteAppItemIndexFromCache(ATaskMark type, AppItem appItem) {
		List<Integer> idList = getAppIdList(type);
		idList.remove(new Integer(appItem.getId()));
		if (logCache) {
			Log.d(TAG, "deleteAppItemFromCache type: " + type + " appItem: " + appItem);
		}
	}

	/**
	 * ��û�����֪����������
	 * 
	 * @param type
	 *            ��������
	 * @param index
	 *            ��ǰ��ʾ�����У��ز����������view��һ��
	 * @return
	 */
	public AppItem getAppItemByMarkIndex(ATaskMark type, int index) {
		if (logCache) {
			Log.d(TAG, "getAppItemByIndex type " + type + " index " + index);
		}
		if (type == null) {
			throw new IllegalArgumentException("AppCahceType can not null.");
		}
		List<Integer> idlist = getAppIdList(type);
		if (index >= idlist.size()) {
			return null;

		} else {
			return appItemMapCache.get(idlist.get(index));
		}
	}

	/**
	 * ͨ�����ͱ�ǲ������
	 * 
	 * @param type
	 *            ���
	 * @param pname
	 *            ����
	 * @return
	 */
	public AppItem getAppItemByMarkPName(ATaskMark type, String pname) {
		if (logCache) {
			Log.d(TAG, "getAppItemByMarkPName type " + type + " pname: " + pname);
		}
		if (type == null) {
			throw new IllegalArgumentException("AppCahceType can not null.");
		}
		List<Integer> idlist = getAppIdList(type);
		AppItem appItem = null;
		for (Integer appId : idlist) {
			appItem = appItemMapCache.get(appId);
			if (pname.equals(appItem.getPackageName())) {
				return appItem;
			}
		}
		return null;
	}

	/**
	 * ĳ���͵�Ӧ���Ƿ����
	 * 
	 * @param type
	 * @param id
	 * @return
	 */
	public boolean isAppItemInCache(ATaskMark type, AppItem appItem) {
		if (logCache) {
			Log.d(TAG, "isAppItemInCache type: " + type + " appItem: " + appItem);
		}

		if (type == null) {
			throw new IllegalArgumentException("AppCahceType can not null.");
		}
		List<Integer> idlist = getAppIdList(type);

		return idlist.contains(appItem.getId());
	}

	/**
	 * ��ö�Ӧ���������id�б�
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<Integer> getAppIdList(ATaskMark type) {
		ArrayList<Integer> idList = appIdMapCache.get(type);
		if (idList == null && type != null) {
			idList = new ArrayList<Integer>();
			appIdMapCache.put(type, idList);
		}
		if (logCache) {
			Log.d(TAG, "getAppIdList type " + type + " size: " + idList.size());
		}
		return idList;
	}

	/**
	 * ��ö�Ӧ�����͵�����б�
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<AppItem> getAppItemList(ATaskMark type) {
		List<Integer> appIdList = getAppIdList(type);
		ArrayList<AppItem> appItemList = new ArrayList<AppItem>();
		for (Integer appId : appIdList) {
			appItemList.add(appItemMapCache.get(appId));
		}
		if (logCache) {
			Log.d(TAG, "getAppItemList type " + type + " size: " + appIdList.size());
		}
		return appItemList;
	}

	/**
	 * ���֪�����͵�����Ļ����С
	 * 
	 * @param appId
	 *            Ӧ�õ�id
	 * */
	public AppItem getAppItemById(int appId) {
		if (logCache) {
			Log.d(TAG, "getAppItemById id " + appId);
		}
		return appItemMapCache.get(appId);
	}

	/**
	 * ���֪�����͵�����Ļ����С
	 * 
	 * @param pName
	 *            ����
	 * @param versionCode
	 *            �汾��
	 */
	public AppItem getAppItemByPackageVersion(String pName, int versionCode) {
		if (logCache) {
			Log.d(TAG, "getAppItemByPackage pName " + pName);
		}
		for (AppItem appItem : appItemMapCache.values()) {
			if (appItem.getPackageName().equals(pName) && appItem.getVersionCode() == versionCode) {
				return appItem;
			}
		}
		return null;
	}

	/**
	 * ���֪�����͵�����Ļ����С
	 * 
	 * @param type
	 *            ��������
	 * */
	public int getAppItemCount(ATaskMark type) {
		if (type == null) {
			return 0;
		}
		int count = getAppIdList(type).size();
		if (logCache) {
			LogUtil.d(TAG, "getAppItemCacheCount type " + type + " count " + count);
		}
		return count;
	}

	/**
	 * ���Ӧ�õ�����
	 * 
	 * @param taskMark
	 * @param commentList
	 */
	public void addAppCommentToCache(CommentsTaskMark taskMark, List<AppComment> commentList) {
		if (taskMark == null) {
			throw new IllegalArgumentException("AppCommentTaskWraper can not null.");
		}
		AppItem appItem = getAppItemById(taskMark.getAppId());
		if (appItem != null) {
			appItem.getCommentList().addAll(commentList);
		}
		if (logCache) {
			Log.d(TAG, "addAppCommnetToCache add count: " + commentList.size() + " total count: "
					+ appItem.getCommentList().size());
		}
	}

	/**
	 * @return the categoryList
	 */
	public List<AppCategory> getCategoryList() {
		return appCategoryList;
	}

	/**
	 * ���ָ��λ�õ����
	 */
	public AppCategory getAppCategoryByIndex(int index) {
		if (index >= 0 && index < appCategoryList.size()) {
			return appCategoryList.get(index);
		} else {
			return null;
		}
	}

	/**
	 * @param categoryList
	 *            the categoryList to set
	 */
	public void setCategoryList(List<AppCategory> categoryList) {
		this.appCategoryList = categoryList;
	}

	/**
	 * @return the purchasedList
	 */
	public List<PurchasedApp> getPurchasedList() {
		return purchasedList;
	}

	/**
	 * @param purchasedList
	 *            the purchasedList to set
	 */
	public void setPurchasedList(List<PurchasedApp> purchasedList) {
		this.purchasedList = purchasedList;
	}

	/**
	 * ��ӹ���
	 * 
	 * @param appPurchasedApp
	 */
	public void addPurchaseToList(PurchasedApp appPurchasedApp) {
		for (PurchasedApp app : purchasedList) {
			if (app.getId() == appPurchasedApp.getId()) {
				return;
			}
		}
		purchasedList.add(appPurchasedApp);
	}

	/**
	 * ��ջ���<br>
	 * �������ձ����������ĳ�ʼ����
	 */
	public void reinitAppCache() {
		// ����б�
		appIdMapCache.clear();
		// �����Ϣ
		appItemMapCache.clear();
		// ���
		appCategoryList.clear();
		// �ѹ������
		purchasedList.clear();
	}
}
