package com.kapps.market.task;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kapps.market.MApplication;
import com.kapps.market.TaskMarkPool;
import com.kapps.market.bean.AppBadness;
import com.kapps.market.bean.AppComment;
import com.kapps.market.bean.Software;
import com.kapps.market.bean.UserInfo;
import com.kapps.market.bean.config.ContextConfig;
import com.kapps.market.cache.CacheConstants;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.impl.LocalMarketService;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.DependTaskMark;
import com.kapps.market.task.mark.MultipleTaskMark;
import com.kapps.market.task.tracker.ADFavorTracker;
import com.kapps.market.task.tracker.AInvokeTracker;
import com.kapps.market.task.tracker.AppCategoryTracker;
import com.kapps.market.task.tracker.AppCommentTracker;
import com.kapps.market.task.tracker.AppDetailTracker;
import com.kapps.market.task.tracker.AppImageTaskTracker;
import com.kapps.market.task.tracker.AppListTracker;
import com.kapps.market.task.tracker.AppPermissionTracker;
import com.kapps.market.task.tracker.AppQuickDownloadTracker;
import com.kapps.market.task.tracker.AppSummaryTracker;
import com.kapps.market.task.tracker.CheckLocalCacheTracker;
import com.kapps.market.task.tracker.CommitBadnessTracker;
import com.kapps.market.task.tracker.CommitCommentMarkTracker;
import com.kapps.market.task.tracker.CommitCommentTracker;
import com.kapps.market.task.tracker.EmptyTracker;
import com.kapps.market.task.tracker.LoginTracker;
import com.kapps.market.task.tracker.MarketUpdateTracker;
import com.kapps.market.task.tracker.RegistTracker;
import com.kapps.market.task.tracker.SearchKeywordTaskTracker;
import com.kapps.market.task.tracker.SoftwareUpdateTracker;
import com.kapps.market.task.tracker.StaticADTaskTracker;
import com.kapps.market.task.tracker.commitDownloadTracker;
import com.kapps.market.task.tracker.commitMarketFirstTracker;
import com.kapps.market.task.tracker.local.BatchBackupSoftwareTracker;
import com.kapps.market.task.tracker.local.InitBackupApkFileTracker;
import com.kapps.market.task.tracker.local.InitDownloadTaskTracker;
import com.kapps.market.task.tracker.local.InitLocalApkFileTracker;
import com.kapps.market.task.tracker.local.InitSoftwareTracker;
import com.kapps.market.util.Constants;
import com.kapps.market.util.SecurityUtil;

/**
 * Wrapper for invoking URL API....
 * @author Administrator
 * 
 */
public class MarketServiceWraper {

	public static final String TAG = "MarketServiceWraper";

	// �г�����
	private MApplication marketContext;
	private TaskMarkPool taskMarkPool;
	// ʵ�ʵķ���
	private LocalMarketService service;
	// ִ������ķ�������
	private Map<String, Method> methodMap = new HashMap<String, Method>();
	// ���ڿ���ͼƬ��Դ������ķ�ֵ,��С������ϵͳ����
	private ImageTaskScheduler imageTaskScheduler;

	public MarketServiceWraper(MApplication marketContext, LocalMarketService service) {
		this.marketContext = marketContext;
		this.taskMarkPool = marketContext.getTaskMarkPool();
		this.service = service;
	}

	public AsyncOperation login(IResultReceiver resultReceiver, ATaskMark taskMark, UserInfo userInfo, String deviceId,
			String simId, String sign) {
		AsyncOperation operation = null;
		if (AsyncOperation.isTaskExist(taskMark)) {
			operation = takeoverExistTask(resultReceiver, taskMark);
		} else {
			LoginTracker loginTracker = new LoginTracker(resultReceiver);
			operation = wraperOperation(loginTracker, taskMark, "login", userInfo);
			operation.excuteOperate(service, userInfo, deviceId, simId, sign);
		}
		return operation;
	}

	/**
	 * @param user
	 * @return
	 * @see com.ck.market.service.IMarketService#register(com.ck.market.bean.UserInfo)
	 */
	public AsyncOperation register(IResultReceiver resultReceiver, ATaskMark taskMark, UserInfo userInfo, String sign) {
		AsyncOperation operation = null;
		if (AsyncOperation.isTaskExist(taskMark)) {
			operation = takeoverExistTask(resultReceiver, taskMark);
		} else {
			RegistTracker registTracker = new RegistTracker(resultReceiver);
			operation = wraperOperation(registTracker, taskMark, "register", userInfo);
			operation.excuteOperate(service, userInfo, sign);
		}
		return operation;
	}

	/**
	 * 
	 * @param resultReceiver
	 * @param appListTaskMark
	 * @param categoryId
	 * @param sortType
	 * @param feeType
	 * @param pageIndex
	 * @param perCount
	 * @param loadAdvertise
	 *            �Ƿ���ع��
	 * @return
	 */
	public AsyncOperation getAppListByCategory(IResultReceiver resultReceiver, DependTaskMark appListTaskMark,
			int categoryId, int sortType, int feeType, int pageIndex, int perCount, boolean loadAdvertise) {
		AsyncOperation operation = null;
		if (AsyncOperation.isTaskExist(appListTaskMark)) {
			operation = takeoverExistTask(resultReceiver, appListTaskMark);

		} else {
			AppListTracker appItemsTracker = new AppListTracker(resultReceiver);
			operation = wraperOperation(appItemsTracker, appListTaskMark, "getAppListByCategory", null);
			operation.excuteOperate(service, categoryId, sortType, feeType, pageIndex, perCount);
		}
		return operation;
	}

	public AsyncOperation getAppListByRecommend(IResultReceiver resultReceiver, DependTaskMark appListTaskMark,
			int recommendId, int sortType, int pageIndex, int perCount) {
		AsyncOperation operation = null;
		if (AsyncOperation.isTaskExist(appListTaskMark)) {
			operation = takeoverExistTask(resultReceiver, appListTaskMark);

		} else {
			AppListTracker appItemsTracker = new AppListTracker(resultReceiver);
			operation = wraperOperation(appItemsTracker, appListTaskMark, "getAppListByRecommend", null);
			operation.excuteOperate(service, recommendId, sortType, pageIndex, perCount);
		}
		return operation;
	}

	/**
	 * �������µĹ��
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param popType
	 * @return
	 */
	public AsyncOperation getAppAdvertiseByType(IResultReceiver resultReceiver, ATaskMark taskMark, int popType,
			int pageIndex, int perCount) {
		AsyncOperation operation = null;
		if (AsyncOperation.isTaskExist(taskMark)) {
			operation = takeoverExistTask(resultReceiver, taskMark);

		} else {
			AppListTracker appItemsTracker = new AppListTracker(resultReceiver);
			operation = wraperOperation(appItemsTracker, taskMark, "getAdvertiseApps", null);
			operation.excuteOperate(service, popType, pageIndex, perCount);
		}
		return operation;
	}

	/**
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param categoryId
	 * @param sortType
	 * @param feeType
	 * @param pageIndex
	 * @param perCount
	 * @return
	 */
	public AsyncOperation getAppListByTopDownload(IResultReceiver resultReceiver, ATaskMark taskMark, int sortType,
			int pageIndex, int perCount) {
		AsyncOperation operation = null;
		if (AsyncOperation.isTaskExist(taskMark)) {
			operation = takeoverExistTask(resultReceiver, taskMark);
		} else {
			AppListTracker appItemsTracker = new AppListTracker(resultReceiver);
			operation = wraperOperation(appItemsTracker, taskMark, "getAppListByTopDownload", null);
			operation.excuteOperate(service, sortType, pageIndex, perCount);
		}
		return operation;
	}

	public AsyncOperation getAppListByTopDownloadFirstPage(IResultReceiver resultReceiver, ATaskMark taskMark, int sortType,
			int pageIndex, int perCount) {
		AsyncOperation operation = null;
		if (AsyncOperation.isTaskExist(taskMark)) {
			operation = takeoverExistTask(resultReceiver, taskMark);
		} else {
			AppListTracker appItemsTracker = new AppListTracker(resultReceiver);
			operation = wraperOperation(appItemsTracker, taskMark, "getAppListByTopDownloadFirstPage", null);
			operation.excuteOperate(service, sortType, pageIndex, perCount);
		}
		return operation;
	}
	/**
	 * �����ϼܽӿ�1004
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param categoryId
	 * @param sortType
	 * @param feeType
	 * @param pageIndex
	 * @param perCount
	 * @return
	 */
	public AsyncOperation getNewsAppList(IResultReceiver resultReceiver, ATaskMark taskMark, int pageIndex, int perCount) {
		AsyncOperation operation = null;
		if (AsyncOperation.isTaskExist(taskMark)) {
			operation = takeoverExistTask(resultReceiver, taskMark);

		} else {
			AppListTracker appItemsTracker = new AppListTracker(resultReceiver);
			operation = wraperOperation(appItemsTracker, taskMark, "getNewsAppList", null);
			operation.excuteOperate(service, pageIndex, perCount);
		}
		return operation;
	}

	/**
	 * ��ѯ����б�
	 * 
	 * @return
	 */
	public AsyncOperation searchAppByCondition(IResultReceiver resultReceiver, ATaskMark taskMark, int type,
			String key, int pageIndex, int perCount) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				AppListTracker categoryTracker = new AppListTracker(resultReceiver);
				operation = wraperOperation(categoryTracker, taskMark, "searchAppByCondition", null);
				operation.excuteOperate(service, type, key, pageIndex, perCount);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ͨ�����id��������ϸ
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param appId
	 *            ���ip
	 * @return
	 */
	public AsyncOperation getAppDetailById(IResultReceiver resultReceiver, ATaskMark taskMark, int appId) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				AppDetailTracker appDetailTracker = new AppDetailTracker(resultReceiver);
				operation = wraperOperation(appDetailTracker, taskMark, "getAppDetailById", null);
				operation.excuteOperate(service, appId);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * �������ĸ�Ҫ��Ϣ
	 * 
	 * @param pname
	 *            Ӧ�õİ���
	 * @param version
	 *            �汾
	 * @return
	 */
	public AsyncOperation getAppSummary(IResultReceiver resultReceiver, ATaskMark taskMark, String pname,
			int versionCode) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				AppSummaryTracker appSummaryTracker = new AppSummaryTracker(resultReceiver);
				operation = wraperOperation(appSummaryTracker, taskMark, "getAppSummary", null);
				operation.excuteOperate(service, pname, versionCode);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * �������ĸ�Ҫ��Ϣ
	 * 
	 * @param appId
	 *            Ӧ�õ�id
	 * 
	 * @return
	 */
	public AsyncOperation getAppSummary(IResultReceiver resultReceiver, ATaskMark taskMark, int appId) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				AppSummaryTracker appSummaryTracker = new AppSummaryTracker(resultReceiver);
				operation = wraperOperation(appSummaryTracker, taskMark, "getAppSummaryById", null);
				operation.excuteOperate(service, appId);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ���е����͵��б�
	 * 
	 * @return
	 */
	public AsyncOperation getAppCategoryList(IResultReceiver resultReceiver, ATaskMark taskMark) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				AppCategoryTracker categoryTracker = new AppCategoryTracker(resultReceiver);
				operation = wraperOperation(categoryTracker, taskMark, "getAppCategoryList", null);
				operation.excuteOperate(service);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ������ߵ�����б�
	 * 
	 * @return
	 */
	public AsyncOperation getAppListByDeveloper(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach,
			String develper, int pageIndex, int perCount) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				AppListTracker appItemsTracker = new AppListTracker(resultReceiver);
				operation = wraperOperation(appItemsTracker, taskMark, "getAppListByDeveloper", attach);
				operation.excuteOperate(service, develper, pageIndex, perCount);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ����������ʷ�汾
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param appId
	 *            ���id
	 * @return
	 */
	public AsyncOperation getHistoryAppList(IResultReceiver resultReceiver, ATaskMark taskMark, int appId, String pname) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				AppListTracker appListTraker = new AppListTracker(resultReceiver);
				operation = wraperOperation(appListTraker, taskMark, "getHistoryAppList", null);
				operation.excuteOperate(service, appId, pname);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ���е�Ӧ�õ�����
	 * 
	 * @return
	 */
	public AsyncOperation getAppCommentList(IResultReceiver resultReceiver, ATaskMark taskMark, int appId,
			String pname, int pageIndex, int perCount) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				AppCommentTracker commentsTracker = new AppCommentTracker(resultReceiver);
				operation = wraperOperation(commentsTracker, taskMark, "getAppCommentList", null);
				operation.excuteOperate(service, appId, pname, pageIndex, perCount);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * �ύ�ҵ�����
	 * 
	 * @return
	 */
	public AsyncOperation commitAppComment(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach,
			AppComment appComment, String pname) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				CommitCommentTracker commentTaskTracker = new CommitCommentTracker(resultReceiver);
				operation = wraperOperation(commentTaskTracker, taskMark, "commitAppComment", attach);
				String sign = SecurityUtil.md5Encode(marketContext.getTs() + Constants.SEC_KEY_STRING);
				operation.excuteOperate(service, appComment, pname, sign);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * �ύ�ҵ����۱��
	 * 
	 * @return
	 */
	public AsyncOperation commitAppCommentMark(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach,
			int commentId, int mark) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				CommitCommentMarkTracker hiapkTracker = new CommitCommentMarkTracker(resultReceiver);
				operation = wraperOperation(hiapkTracker, taskMark, "commitAppCommentMark", attach);
				operation.excuteOperate(service, commentId, mark);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * �ύ��Ӧ�ľٱ�
	 * 
	 * @return
	 */
	public AsyncOperation commitAppBadness(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach,
			AppBadness appBadness) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				CommitBadnessTracker badnessTaskWraper = new CommitBadnessTracker(resultReceiver);
				operation = wraperOperation(badnessTaskWraper, taskMark, "commitBadnessContent", attach);
				String sign = SecurityUtil.md5Encode(marketContext.getTs() + Constants.SEC_KEY_STRING);
				operation.excuteOperate(service, appBadness, sign);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * �������б�
	 * 
	 * @param invokeTracker
	 * @param taskMark
	 * @param methodName
	 * @param attach
	 * @param checkUpdate
	 *            �Ƿ������
	 * @return
	 */
	public AsyncOperation initSoftwareSummaryList(IResultReceiver resultReceiver, ATaskMark taskMark) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				InitSoftwareTracker initSoftwareTracker = new InitSoftwareTracker(resultReceiver);
				operation = wraperOperation(initSoftwareTracker, taskMark, "initSoftwareSummaryInfoList", null);
				operation.excuteOperate(service);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ��ʼ�����������ϸ��Ϣ
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param apkList
	 * @return
	 */
	public AsyncOperation initSoftwareDetailInfoList(IResultReceiver resultReceiver, ATaskMark taskMark,
			List<Software> softwareList) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				EmptyTracker hiapkTracker = new EmptyTracker(resultReceiver);
				operation = wraperOperation(hiapkTracker, taskMark, "initSoftwareDetailInfoList", null);
				operation.excuteOperate(service, softwareList);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ��鱾�ػ���
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 */
	public AsyncOperation checkLocalCache(IResultReceiver resultReceiver, ATaskMark taskMark) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				CheckLocalCacheTracker checkLocalCacheTracker = new CheckLocalCacheTracker(resultReceiver);
				operation = wraperOperation(checkLocalCacheTracker, taskMark, "checkLocalCache", null);
				operation.excuteOperate(service);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Ĭ�ϵı���apkĿ¼
	 * 
	 * @param invokeTracker
	 * @param taskMark
	 * @param methodName
	 * @param attach
	 * @return
	 */
	public AsyncOperation initLocalApkSummaryInfoList(IResultReceiver resultReceiver, ATaskMark taskMark,
			List<Software> oldList) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				InitLocalApkFileTracker hiapkTracker = new InitLocalApkFileTracker(resultReceiver);
				operation = wraperOperation(hiapkTracker, taskMark, "initApkSummaryInfoList", null);
				operation.excuteOperate(service, oldList, CacheConstants.LOCAK_APK_INFO, marketContext
						.getMarketConfig().getLocalApkDir());
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Ĭ�ϵ��������Ŀ¼
	 * 
	 * @param invokeTracker
	 * @param taskMark
	 * @param methodName
	 * @param attach
	 * @return
	 */
	public AsyncOperation initBackupApkSummaryInfoList(IResultReceiver resultReceiver, ATaskMark taskMark,
			List<Software> oldList) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				InitBackupApkFileTracker hiapkTracker = new InitBackupApkFileTracker(resultReceiver);
				operation = wraperOperation(hiapkTracker, taskMark, "initApkSummaryInfoList", null);
				operation.excuteOperate(service, oldList, CacheConstants.BACKUP_APK_INFO,
						Constants.DEFAULT_SOFTWARE_BACKUP_DIR);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ��ʼ������apk��ϸ��Ϣ
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param apkList
	 * @return
	 */
	public AsyncOperation initApkDetailInfoList(IResultReceiver resultReceiver, ATaskMark taskMark,
			List<Software> apkList, String cacheMark) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				EmptyTracker hiapkTracker = new EmptyTracker(resultReceiver);
				operation = wraperOperation(hiapkTracker, taskMark, "initApkDetailInfoList", null);
				operation.excuteOperate(service, apkList, cacheMark);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public AsyncOperation batchBackupSoftware(IResultReceiver resultReceiver, ATaskMark taskMark,
			List<Software> needBackupList) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				BatchBackupSoftwareTracker hiapkTracker = new BatchBackupSoftwareTracker(resultReceiver);
				operation = wraperOperation(hiapkTracker, taskMark, "batchBackupSoftware", null);
				operation.excuteOperate(service, needBackupList);
			}
			return operation;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * ������ص���������������δ������
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param context
	 * @return
	 */
	public AsyncOperation initDownloadTask(IResultReceiver resultReceiver, ATaskMark taskMark) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				InitDownloadTaskTracker hiapkTracker = new InitDownloadTaskTracker(resultReceiver);
				operation = wraperOperation(hiapkTracker, taskMark, "initDownloadTaskMap", null);
				operation.excuteOperate(service);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ͬ�������Ϣ�б�
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param packageList
	 *            ��Ҫͬ����������б�
	 * @return
	 */
	public AsyncOperation synSoftwareinfo(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach,
			List<String> packageList) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				AppDetailTracker synSoftwareTracker = new AppDetailTracker(resultReceiver);
				operation = wraperOperation(synSoftwareTracker, taskMark, "synSoftwareinfo", attach);
				operation.excuteOperate(service, packageList);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ͬ�������Ϣ�б�
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param packageList
	 *            ��Ҫͬ����������б�
	 * @param sign
	 * @return
	 */
	public AsyncOperation getAppImageResource(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach,
			int appId, String url, int type) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				AppImageTaskTracker downloadUrlTracker = new AppImageTaskTracker(resultReceiver);
				operation = wraperOperation(downloadUrlTracker, taskMark, "getAppImageResource", attach);
				String sign = SecurityUtil.md5Encode(marketContext.getTs() + Constants.SEC_KEY_STRING);
				operation.excuteOperate(service, appId, url, type, sign);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public AsyncOperation addFavorAppItem(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach, int appId) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				String sign = SecurityUtil.md5Encode(marketContext.getTs() + Constants.SEC_KEY_STRING);
				ADFavorTracker adFavorTracker = new ADFavorTracker(resultReceiver);
				operation = wraperOperation(adFavorTracker, taskMark, "addFavorAppItem", attach);
				operation.excuteOperate(service, appId, sign);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public AsyncOperation deleteFavorAppItem(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach,
			int appId) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				String sign = SecurityUtil.md5Encode(marketContext.getTs() + Constants.SEC_KEY_STRING);
				ADFavorTracker adFavorTracker = new ADFavorTracker(resultReceiver);
				operation = wraperOperation(adFavorTracker, taskMark, "deleteFavorAppItem", attach);
				operation.excuteOperate(service, appId, sign);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public AsyncOperation getAppFavorList(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach,
			int pageIndex, int perCount) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				AppListTracker appListTracker = new AppListTracker(resultReceiver);
				operation = wraperOperation(appListTracker, taskMark, "getAppFavorList", attach);
				operation.excuteOperate(service, pageIndex, perCount);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public AsyncOperation getAppPermissionList(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach,
			int appId) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				AppPermissionTracker appPermissionTracker = new AppPermissionTracker(resultReceiver);
				operation = wraperOperation(appPermissionTracker, taskMark, "getAppPermissionList", attach);
				operation.excuteOperate(service, appId);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public AsyncOperation checkMarketUpdate(IResultReceiver resultReceiver, ATaskMark taskMark,
			ContextConfig marketConfig) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				MarketUpdateTracker marketUpdateTracker = new MarketUpdateTracker(resultReceiver);
				operation = wraperOperation(marketUpdateTracker, taskMark, "checkMarketUpdate", null);
				operation.excuteOperate(service, marketConfig);
			}
			LogUtil.v(TAG, "checkMarketUpdate return");
			return operation;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public AsyncOperation checkSoftwareUpdate(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach,
			List<Software> softwareList) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				SoftwareUpdateTracker softwareUpdateTracker = new SoftwareUpdateTracker(resultReceiver);
				operation = wraperOperation(softwareUpdateTracker, taskMark, "checkSoftwareUpdate", attach);
				operation.excuteOperate(service, softwareList);
			}
			return operation;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ��̬���
	 * 
	 * @param resultReceiver
	 * @param taskMarks
	 * @param oldAdId
	 * @return
	 */
	public AsyncOperation checkStaticAD(IResultReceiver resultReceiver, ATaskMark taskMark, long oldAdId) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				StaticADTaskTracker staticADTaskTracker = new StaticADTaskTracker(resultReceiver);
				operation = wraperOperation(staticADTaskTracker, taskMark, "checkStaticAD", null);
				operation.excuteOperate(service, oldAdId);
			}
			return operation;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public AsyncOperation reportChannel(IResultReceiver resultReceiver, ATaskMark taskMark, String did, String cid) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				SoftwareUpdateTracker softwareUpdateTracker = new SoftwareUpdateTracker(resultReceiver);
				operation = wraperOperation(softwareUpdateTracker, taskMark, "reportChannel", null);
				String sign = SecurityUtil.md5Encode(marketContext.getTs() + Constants.SEC_KEY_STRING);
				long createTime = System.currentTimeMillis();
				operation.excuteOperate(service, did, cid, createTime, sign);
			}
			return operation;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ���ͼƬ��Դ������
	 * 
	 * @param receiver
	 * @param mTaskMark
	 * @param attach
	 * @return
	 */
	public MultipleTaskScheduler scheduleAppImageResourceTask(IResultReceiver receiver, MultipleTaskMark mTaskMark,
			Object attach) {
		if (imageTaskScheduler != null) {
			// ֮ǰ������wuxiao
			MultipleTaskMark oldTaskMark = imageTaskScheduler.getMultipleTaskMark();
			if (oldTaskMark != null) {
				// �ϲ������������ý����ߡ�
				imageTaskScheduler.mergeTaskSchedul(mTaskMark);
			} else {
				imageTaskScheduler.setMultipleTaskMark(mTaskMark);
			}
		} else {
			imageTaskScheduler = new ImageTaskScheduler(this, mTaskMark);
		}
		imageTaskScheduler.setReceiver(receiver);
		LogUtil.iop(TAG, "schedulAppImageResourceTask: task size: " + mTaskMark.getTaskMarkList().size() + "\n task: "
				+ mTaskMark);
		imageTaskScheduler.triggerSchedulTask();

		return imageTaskScheduler;
	}

	// -------------------------------------------------------------------------------
	// -------------------------------------------------------------------------------

	/**
	 * ��ʾ��Ҫǿ�ƽӹ�ĳ������ 2010-7-12
	 * 
	 * @param receiver
	 *            �µĽӹ���
	 * @param taskMark
	 *            ������Ҫ�ӹܵ�����
	 * 
	 * @author admin
	 */
	public AsyncOperation forceTakeoverTask(IResultReceiver receiver, ATaskMark taskMark) {
		LogUtil.iop(TAG, "force take over task: " + taskMark);
		AsyncOperation operation = takeoverExistTask(receiver, taskMark);
		return operation;
	}

	/**
	 * ���ͼƬ��������ͼƬ����
	 * 
	 * @param receiver
	 */
	public void forceTakeoverImageScheduleTask(IResultReceiver receiver) {
		if (imageTaskScheduler == null) {
			LogUtil.iop(TAG, "force take receiver: " + receiver);
			imageTaskScheduler.setReceiver(receiver);
		}
	}

	/**
	 * viewǿ�Ʒ������һ�����񣬵�����Ա���ȡ����<br>
	 * 
	 * @param receiver
	 *            ������
	 * @param taskMark
	 *            �����ʾ
	 * @return
	 */
	public AsyncOperation forceDiscardReceiveTask(ATaskMark taskMark) {
		AsyncOperation asyncOperation = AsyncOperation.getTaskByMark(taskMark);
		if (asyncOperation != null) {
			AInvokeTracker aInvokeTracker = asyncOperation.getInvokeTracker();
			if (aInvokeTracker != null) {
				aInvokeTracker.setResultReceiver(null);
			}
			LogUtil.iop(TAG, "++++ discard : " + taskMark);
		} else {
			LogUtil.iop(TAG, "++++ not need discard : " + taskMark);
		}

		return asyncOperation;
	}

	/**
	 * ǿ��ȡ�����й���Ľ���
	 * 
	 * @param receiver
	 *            ϣ������ܵĽ�����
	 */
	public void forceReleaseReceive(IResultReceiver receiver) {
		List<AsyncOperation> ops = new ArrayList<AsyncOperation>(AsyncOperation.asyncOperations());
		AsyncOperation asyncOperation = null;
		AInvokeTracker tracker = null;
		for (int index = 0; index < ops.size(); index++) {
			asyncOperation = ops.get(index);
			tracker = asyncOperation.getInvokeTracker();
			if (tracker != null && tracker.getResultReceiver() == receiver) {
				tracker.setResultReceiver(null);
			}
		}
	}

	/**
	 * ����Ƿ������Ѿ����ڣ�
	 * 
	 * @param taskMark
	 *            �����ʾ
	 * @return
	 */
	public boolean isTaskExist(ATaskMark taskMark) {
		return AsyncOperation.isTaskExist(taskMark);
	}

	// ��װһ������
	private AsyncOperation wraperOperation(AInvokeTracker invokeTracker, ATaskMark taskMark, String methodName,
			Object attach) {
		Method method = getMethod(methodName);
		AsyncOperation operation = new AsyncOperation(taskMark, method);
		operation.setInvokeTracker(invokeTracker);
		operation.setAttach(attach);
		return operation;
	}

	// ��÷�����������û������ҡ�
	private Method getMethod(String name) {
		Method method = methodMap.get(name);
		if (method == null) {
			Method[] methods = service.getClass().getMethods();
			for (Method aMethod : methods) {
				if (aMethod.getName().equals(name)) {
					method = aMethod;
					methodMap.put(name, method);
					break;
				}
			}
		}
		if (method == null) {
			throw new NoSuchMethodError("unknow method : " + name);
		} else {
			return method;
		}
	}

	/**
	 * ֹͣ���е��첽����
	 */
	public void stopAllAsyncOperate() {
		try {
			AsyncOperation.stopAllAsyncOperate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ֹͣĳ������
	 */
	public void stopAsyncOperate(ATaskMark taskMark) {
		AsyncOperation operation = AsyncOperation.getTaskByMark(taskMark);
		if (operation != null) {
			operation.clearAsysnTask(true);
		}

	}

	/**
	 * ���һ������֮ǰ�Ѿ�ִ�У�����û�з��ص�ʱ��������ʱ�� ���µ��������������<br>
	 * ��ô�����߽��ӹܴ�����, �����ܸ�ľ������״̬���Ա㱣֤����������ԡ�
	 */
	private AsyncOperation takeoverExistTask(IResultReceiver resultReceiver, ATaskMark taskMark) {
		AsyncOperation asyncOperation = AsyncOperation.getTaskByMark(taskMark);
		if (asyncOperation != null) {
			AInvokeTracker aInvokeTracker = asyncOperation.getInvokeTracker();
			if (aInvokeTracker != null && aInvokeTracker.getResultReceiver() != resultReceiver) {
				aInvokeTracker.setResultReceiver(resultReceiver);
			}
			LogUtil.iop(TAG, "!!!! taskover : " + taskMark);
		} else {
			LogUtil.iop(TAG, "!!!! not need taskover : " + taskMark);
		}

		return asyncOperation;
	}

	/**
	 * ��������
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param pname
	 * @param versionCode
	 * @return
	 */
	public AsyncOperation quickDownloadApp(IResultReceiver resultReceiver, ATaskMark taskMark, int aid) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				AppQuickDownloadTracker quickDownloadTracker = new AppQuickDownloadTracker(resultReceiver);
				operation = wraperOperation(quickDownloadTracker, taskMark, "getAppSummaryById", null);
				operation.excuteOperate(service, aid);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * �ر���������
	 */
	public void shutdownHttpConnect() {
		// TODO ��Ҫ�ر����е�http���ӡ�
	}
	/**
	 * ��������
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param appid
	 * @param sign
	 * @param ei
	 * @param si
	 * @return
	 */
	public AsyncOperation commitDownloadRecord(IResultReceiver resultReceiver, ATaskMark taskMark,String id,String sign,String ei,String cid) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				commitDownloadTracker commitDownloadTracker = new commitDownloadTracker(resultReceiver);
				operation = wraperOperation(commitDownloadTracker, taskMark, "commitDownloadRecord", null);
				operation.excuteOperate(service,id,sign,ei,cid);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param id
	 * @param sign
	 * @param ei
	 * @return
	 */
	public AsyncOperation commitMarketDownloadRecord(IResultReceiver resultReceiver, ATaskMark taskMark,Integer id,String sign,String ei) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				commitDownloadTracker commitDownloadTracker = new commitDownloadTracker(resultReceiver);
				operation = wraperOperation(commitDownloadTracker, taskMark, "commitMarketDownloadRecord", null);
				operation.excuteOperate(service,id,sign,ei);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param id
	 * @param sign
	 * @param ei
	 * @return
	 */
	public AsyncOperation commitMarketDownloadRecordFirst(IResultReceiver resultReceiver, ATaskMark taskMark,int id,String sign) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);
			} else {
				commitMarketFirstTracker commitDownloadTracker = new commitMarketFirstTracker(resultReceiver);
				operation = wraperOperation(commitDownloadTracker, taskMark, "commitMarketDownloadRecordFirst", null);
				operation.excuteOperate(service,id,sign);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @return
	 */
	public AsyncOperation getSearchKeyword(IResultReceiver resultReceiver, ATaskMark taskMark) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				SearchKeywordTaskTracker searchKeywordTaskTracker = new SearchKeywordTaskTracker(resultReceiver);
				operation = wraperOperation(searchKeywordTaskTracker, taskMark, "getSearchKeyword", null);
				operation.excuteOperate(service);
			}
			return operation;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
