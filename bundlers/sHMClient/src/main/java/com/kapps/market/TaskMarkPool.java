package com.kapps.market;

import java.util.ArrayList;
import java.util.List;

import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AddFavorTaskMark;
import com.kapps.market.task.mark.AppAdvertiseTaskMark;
import com.kapps.market.task.mark.AppDetailTaskMark;
import com.kapps.market.task.mark.AppFavorTaskMark;
import com.kapps.market.task.mark.AppImageTaskMark;
import com.kapps.market.task.mark.AppListTaskMark;
import com.kapps.market.task.mark.AppPermissionTaskMark;
import com.kapps.market.task.mark.AppQuickDownloadTaskMark;
import com.kapps.market.task.mark.AppSearchTaskMark;
import com.kapps.market.task.mark.AppSummaryTaskMark;
import com.kapps.market.task.mark.AuthorAppTaskMark;
import com.kapps.market.task.mark.CategoryTaskMark;
import com.kapps.market.task.mark.ChannelReportTaskMark;
import com.kapps.market.task.mark.CheckLocalCacheTaskMark;
import com.kapps.market.task.mark.CommentsTaskMark;
import com.kapps.market.task.mark.CommitBadnessTaskMark;
import com.kapps.market.task.mark.CommitCommentMarkTaskMark;
import com.kapps.market.task.mark.CommitCommentTaskMark;
import com.kapps.market.task.mark.CommitDownloadTaskMark;
import com.kapps.market.task.mark.DeleteFavorTaskMark;
import com.kapps.market.task.mark.HistoryAppTaskMark;
import com.kapps.market.task.mark.LoginTaskMark;
import com.kapps.market.task.mark.MarketUpdateTaskMark;
import com.kapps.market.task.mark.RegistTaskMark;
import com.kapps.market.task.mark.SearchKeywordTaskMark;
import com.kapps.market.task.mark.SoftwareUpdateTaskMark;
import com.kapps.market.task.mark.StaticADTaskMark;
import com.kapps.market.task.mark.local.InitDownloadTaskMark;
import com.kapps.market.task.mark.local.InitLocalApkSummaryTaskMark;
import com.kapps.market.task.mark.local.InitSoftwareSummaryTaskMark;
import com.kapps.market.task.mark.local.LocalApkDetailInfoTaskMark;
import com.kapps.market.task.mark.local.SoftwareDetailInfoTaskMark;
import com.kapps.market.task.mark.local.UpdatableDetailInfoTaskMark;

/**
 * 2010-6-22<br>
 * �����
 * 
 * @author admin
 * 
 */
public final class TaskMarkPool {

	TaskMarkPool() {
		super();
	}

	/**
	 * ע��
	 */
	private RegistTaskMark registTaskMark;

	/**
	 * ϲ���б�
	 */
	private AppFavorTaskMark appFavorTaskMark;

	/**
	 * �������
	 */
	private SoftwareUpdateTaskMark softwareUpdateTaskMark;

	/**
	 * �г�����
	 */
	private MarketUpdateTaskMark marketUpdateTaskMark;

	/**
	 * �Ѱ�װ����ĸ����Ϣ����Ϊ�Ѱ�װ�����ͼ���ܱ��򿪶�� ���Ա��뱣��״̬��
	 */
	private SoftwareDetailInfoTaskMark softwareDetailInfoTaskMark;

	/**
	 * ���Ը��µ��������ϸ��Ϣ
	 */
	private UpdatableDetailInfoTaskMark updatableDetailInfoTaskMark;

	/**
	 * ����apk������Ϣ
	 */
	private LocalApkDetailInfoTaskMark localApkDetailInfoTaskMark;

	/**
	 * ����apk
	 */
	private InitLocalApkSummaryTaskMark initApkSummaryTaskMark;

	/**
	 * ��������
	 */
	private InitDownloadTaskMark initDownloadTaskMark;
	/**
	 * �������
	 */
	private InitSoftwareSummaryTaskMark initSoftwareSummaryTaskMark;

	/**
	 * ����б�
	 */
	private CategoryTaskMark categoryTaskMark;
	
	/**
	 * ��̬���
	 */
	private StaticADTaskMark staticADTaskMark;
	/**
	 * ��̬���
	 */
	private CommitDownloadTaskMark commitdownloadTaskMark;
	/**
	 * searchkeyword
	 */
	private SearchKeywordTaskMark  searchKeywordTaskMark;
	/**
	 * ����б�
	 */

	private List<AppListTaskMark> appCategoryItemsTaskList = new ArrayList<AppListTaskMark>();

	/**
	 * ����б�
	 */
	private List<AppAdvertiseTaskMark> appAdvertiseTaskMarkList = new ArrayList<AppAdvertiseTaskMark>();

	/**
	 * ��ѯ�б�
	 */
	private List<AppSearchTaskMark> appSearchTaskList = new ArrayList<AppSearchTaskMark>();

	/**
	 * ��������б�
	 */
	private List<AuthorAppTaskMark> authorAppItemsTaskList = new ArrayList<AuthorAppTaskMark>();

	/**
	 * �����ʷ�б�
	 */
	private List<HistoryAppTaskMark> historyAppTaskMarkList = new ArrayList<HistoryAppTaskMark>();

	/**
	 * �����б�
	 */
	private List<CommentsTaskMark> commentsTaskList = new ArrayList<CommentsTaskMark>();
	

	/**
	 * ��������״̬�ع�����Ա���ϻ���
	 * 
	 * @param taskMark
	 */
	void reinitTaskMark(ATaskMark taskMark) {
		if (taskMark instanceof AppFavorTaskMark) {
			appFavorTaskMark = null;

		}
	}

	/**
	 * �����̳�ʱ����ʱ����ݳ�ʼ����<br>
	 * ��������ݲ����?
	 */
	void reinitForLongLive() {
		appCategoryItemsTaskList.clear();
		appAdvertiseTaskMarkList.clear();
		appSearchTaskList.clear();
		authorAppItemsTaskList.clear();
		historyAppTaskMarkList.clear();
		commentsTaskList.clear();
		appFavorTaskMark = null;
		categoryTaskMark = null;
	}

	/**
	 * ����û����ղ��б�
	 */
	public AppFavorTaskMark getAppFavorTaskMark() {
		if (appFavorTaskMark == null) {
			appFavorTaskMark = new AppFavorTaskMark();
		}
		return appFavorTaskMark;
	}

	/**
	 * ��ȡ����������
	 */
	public CategoryTaskMark getCategoryTask() {
		if (categoryTaskMark == null) {
			categoryTaskMark = new CategoryTaskMark();
		}
		return categoryTaskMark;
	}

	/**
	 * 
	 * @param type
	 * @param key
	 * @return
	 */
	public AppSearchTaskMark getAppSearchTaskMark(int type, String key) {
		for (AppSearchTaskMark taskWraper : appSearchTaskList) {
			if (taskWraper.getType() == type && taskWraper.getKey().equals(key)) {
				return taskWraper;
			}
		}
		AppSearchTaskMark taskMark = new AppSearchTaskMark(type, key);
		appSearchTaskList.add(taskMark);
		return taskMark;
	}

	/**
	 * @return the registTaskWraper
	 */
	public RegistTaskMark getRegistTask() {
		if (registTaskMark == null) {
			registTaskMark = new RegistTaskMark();
		}
		return registTaskMark;
	}

	/**
	 * ������������Ϣ
	 * 
	 * @return the loadSoftwareTaskMark
	 */
	public SoftwareDetailInfoTaskMark getSoftwareDetailInfoTaskMark() {
		if (softwareDetailInfoTaskMark == null) {
			softwareDetailInfoTaskMark = new SoftwareDetailInfoTaskMark();
			softwareDetailInfoTaskMark.setTaskStatus(ATaskMark.HANDLE_WAIT);
		}
		return softwareDetailInfoTaskMark;
	}

	public UpdatableDetailInfoTaskMark getUpdatableDetailInfoTaskMark() {
		if (updatableDetailInfoTaskMark == null) {
			updatableDetailInfoTaskMark = new UpdatableDetailInfoTaskMark();
			updatableDetailInfoTaskMark.setTaskStatus(ATaskMark.HANDLE_WAIT);
		}
		return updatableDetailInfoTaskMark;
	}

	/**
	 * 
	 * @return
	 */
	public LocalApkDetailInfoTaskMark getLocalApkDetailInfoTaskMark() {
		if (localApkDetailInfoTaskMark == null) {
			localApkDetailInfoTaskMark = new LocalApkDetailInfoTaskMark();
			localApkDetailInfoTaskMark.setTaskStatus(ATaskMark.HANDLE_WAIT);
		}
		return localApkDetailInfoTaskMark;
	}

	/**
	 * ���������±�ʾ
	 */
	public SoftwareUpdateTaskMark getSoftwareUpdateTaskMark(boolean manul) {
		if (softwareUpdateTaskMark == null) {
			softwareUpdateTaskMark = new SoftwareUpdateTaskMark();
		}
		softwareUpdateTaskMark.setManul(manul);
		return softwareUpdateTaskMark;
	}

	/**
	 * ����г�����
	 */
	public MarketUpdateTaskMark getMarketUpdateTaskMark(boolean manul) {
		if (marketUpdateTaskMark == null) {
			marketUpdateTaskMark = new MarketUpdateTaskMark();
		}
		marketUpdateTaskMark.setManul(manul);
		return marketUpdateTaskMark;
	}

	/**
	 * ��ʼ���Ѿ���װ�����
	 * 
	 * @return the loadSoftwareTaskMark
	 */
	public InitSoftwareSummaryTaskMark getInitSoftwareSummaryTaskMark() {
		if (initSoftwareSummaryTaskMark == null) {
			initSoftwareSummaryTaskMark = new InitSoftwareSummaryTaskMark();
		}
		return initSoftwareSummaryTaskMark;
	}

	/**
	 * ��ʼ������apk
	 * 
	 * @return the initHiapkFileTaskMark
	 */
	public InitLocalApkSummaryTaskMark getInitLocalApkSummaryTaskMark() {
		if (initApkSummaryTaskMark == null) {
			initApkSummaryTaskMark = new InitLocalApkSummaryTaskMark();
		}
		return initApkSummaryTaskMark;
	}

	/**
	 * ��ʼ�����������б�
	 * 
	 * @return the InitDownloadTaskMark
	 */
	public InitDownloadTaskMark getInitDownloadTaskMark() {
		if (initDownloadTaskMark == null) {
			initDownloadTaskMark = new InitDownloadTaskMark();
		}
		return initDownloadTaskMark;
	}

	/**
	 * ��鱾�ػ���
	 * 
	 * @return
	 */
	public CheckLocalCacheTaskMark createCheckLocalCacheTaskMark() {
		return new CheckLocalCacheTaskMark();
	}

	/**
	 * ������½����
	 * 
	 * @return the loginTaskWraper
	 */
	// TODO Ҫ �޸�ע������
	public LoginTaskMark createLoginTaskMark(String user, String password) {
		return new LoginTaskMark(user, password);
	}

	/**
	 * �������ͼƬ
	 */
	public AppImageTaskMark createAppImageTaskMark(int appId, String url, int type) {
		return new AppImageTaskMark(appId, url, type);
	}

	/**
	 * ��ȡ�������б�����
	 */
	public AppListTaskMark getAppListTaskMark(int sortType, int feeType) {
		for (AppListTaskMark taskWraper : appCategoryItemsTaskList) {
			if (taskWraper.getCategory() == AppListTaskMark.ALL_CATEGORY_MARK && taskWraper.getSortType() == sortType
					&& taskWraper.getFeeType() == feeType) {
				return taskWraper;
			}
		}
		AppListTaskMark taskMark = new AppListTaskMark(sortType, feeType);
		appCategoryItemsTaskList.add(taskMark);
		return taskMark;
	}

	/**
	 * ��ȡ�������б�����
	 */
	public AppListTaskMark getAppListTaskMark(int categoryId, int sortType, int feeType) {
		for (AppListTaskMark taskWraper : appCategoryItemsTaskList) {
			if (taskWraper.getCategory() == categoryId && taskWraper.getSortType() == sortType
					&& taskWraper.getFeeType() == feeType) {
				return taskWraper;
			}
		}
		AppListTaskMark taskMark = new AppListTaskMark(categoryId, sortType, feeType);
		appCategoryItemsTaskList.add(taskMark);
		return taskMark;
	}

	/**
	 * ���ĳһ����µĹ��
	 */
	public AppAdvertiseTaskMark getAppAdvertiseTaskMark(int popType) {
		for (AppAdvertiseTaskMark appAdvertiseTaskMark : appAdvertiseTaskMarkList) {
			if (appAdvertiseTaskMark.getPopType() == popType) {
				return appAdvertiseTaskMark;
			}
		}
		AppAdvertiseTaskMark taskMark = new AppAdvertiseTaskMark(popType);
		appAdvertiseTaskMarkList.add(taskMark);
		return taskMark;
	}

	/**
	 * ��ȡ��ؿ������������
	 */
	public AuthorAppTaskMark getAuthorAppMark(String appAuthor) {
		for (AuthorAppTaskMark taskWraper : authorAppItemsTaskList) {
			if (taskWraper.getAuthor().equals(appAuthor)) {
				return taskWraper;
			}
		}
		AuthorAppTaskMark taskMark = new AuthorAppTaskMark(appAuthor);
		authorAppItemsTaskList.add(taskMark);
		return taskMark;
	}

	/**
	 * ��ʷ�������
	 * 
	 * @param appAuthor
	 * @return
	 */
	public HistoryAppTaskMark getHistoryAppTaskMark(int appId) {
		for (HistoryAppTaskMark taskWraper : historyAppTaskMarkList) {
			if (taskWraper.getAppId() == appId) {
				return taskWraper;
			}
		}
		HistoryAppTaskMark taskMark = new HistoryAppTaskMark(appId);
		historyAppTaskMarkList.add(taskMark);
		return taskMark;
	}

	/**
	 * ��ȡ�������������
	 */
	public CommentsTaskMark getCommentsMark(int appId) {
		for (CommentsTaskMark taskWraper : commentsTaskList) {
			if (taskWraper.getAppId() == appId) {
				return taskWraper;
			}
		}
		CommentsTaskMark taskMark = new CommentsTaskMark(appId);
		commentsTaskList.add(taskMark);
		return taskMark;
	}

	/**
	 * ����һ��˲̬�������ʾ
	 * 
	 * @return the synSoftwareTaskMark
	 */
	public AppDetailTaskMark createAppDetailTaskMark(int appId) {
		return new AppDetailTaskMark(appId);
	}

	/**
	 * ���Ӧ�õ�Ȩ���б�
	 * 
	 * @param appId
	 * @return
	 */
	public AppPermissionTaskMark createAppPermissionTaskMark(int appId) {
		return new AppPermissionTaskMark(appId);
	}

	/**
	 * ��ȡ�ύ����к���Ϣ�����־
	 */
	public CommitBadnessTaskMark createCommitBadnessTaskWraper(int appId) {
		return new CommitBadnessTaskMark(appId);
	}

	/**
	 * ��ȡ�ύ�����������<br>
	 * ����һ��˲̬����
	 */
	public CommitCommentTaskMark createCommitCommentTaskWraper(int appId) {
		return new CommitCommentTaskMark(appId);
	}

	/**
	 * ��ȡ�ύ����������۵ı��<br>
	 * ����һ��˲̬����
	 */
	public CommitCommentMarkTaskMark createCommitCommentMarkTaskWraper(int commentId) {
		return new CommitCommentMarkTaskMark(commentId);
	}

	/**
	 * ����ղص�������
	 */
	public AddFavorTaskMark createAddFavorTaskMark(int appId) {
		return new AddFavorTaskMark(appId);
	}

	/**
	 * ɾ���ղص�������
	 */
	public DeleteFavorTaskMark createDeleteFavorTaskMark(int appId) {
		return new DeleteFavorTaskMark(appId);
	}

	/**
	 * �����Ҫ
	 */
	public AppSummaryTaskMark createAppSummaryTaskMark(String pname, int versionCode) {
		return new AppSummaryTaskMark(pname, versionCode);
	}

	public AppSummaryTaskMark createAppSummaryTaskMark(int appId) {
		return new AppSummaryTaskMark(appId);
	}
	
	/**
	 * ��þ�̬���
	 * 
	 * @return
	 */
	public StaticADTaskMark getStaticADTaskMark() {
		if (staticADTaskMark == null) {
			staticADTaskMark = new StaticADTaskMark();
		}
		return staticADTaskMark;
	}

	/**
	 * simple����
	 */
	public ChannelReportTaskMark createChannelReportTaskMark() {
		return new ChannelReportTaskMark();
	}
	
	/**
	 * �����������
	 */
	public AppQuickDownloadTaskMark createAppQuickDownloadTaskMark(int aid) {
		return new AppQuickDownloadTaskMark(aid);
	}
	/**
	 * �����������
	 */
	public CommitDownloadTaskMark getCommitDownloadTaskMark() {
		if(commitdownloadTaskMark==null)
		{
			commitdownloadTaskMark=new CommitDownloadTaskMark();
			return commitdownloadTaskMark;
		}
		else
			return commitdownloadTaskMark;
	}
	public SearchKeywordTaskMark getSearchKeywordTaskMark() {
		if (searchKeywordTaskMark == null) {
			searchKeywordTaskMark = new SearchKeywordTaskMark();
		}
		return searchKeywordTaskMark;
	}
}
