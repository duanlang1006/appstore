package com.kapps.market;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.DownloadItem;
import com.kapps.market.bean.Software;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.mark.SoftwareUpdateTaskMark;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2010-6-29 <br>
 * Maintain some list of downloading, downloaded, local software..
 * @author admin
 * 
 */
public class MarketManager {
	public static final String TAG = "MarketManager";

	// ϵͳ������Ļ���
	private List<Software> softwareList;

	// �����б�
	private Map<Integer, List<DownloadItem>> taskMap;

	// ������ֻ�漰�����û���apk���ص�apk�б�
	private List<Software> localApkList;

	// Ĭ�ϱ���Ŀ¼��apk
	private List<Software> backupApkList;

	// �г�����
	private MApplication marketContext;

	public MarketManager(MApplication marketContext) {
		this.marketContext = marketContext;

		softwareList = new ArrayList<Software>();
		taskMap = new HashMap<Integer, List<DownloadItem>>();
		taskMap.put(BaseApp.APP_DOWNLOAD_STOP, new ArrayList<DownloadItem>());
		taskMap.put(BaseApp.APP_DOWNLOADED, new ArrayList<DownloadItem>());
		taskMap.put(BaseApp.APP_DOWNLOADING, new ArrayList<DownloadItem>());
		localApkList = new ArrayList<Software>();
		backupApkList = new ArrayList<Software>();
	}

	/**
	 * type������BaseApp.APP_DOWNLOAD_FAIL; BaseApp.APP_DOWNLOADED;
	 * BaseApp.APP_DOWNLOADING;
	 * 
	 * @param type
	 * @return
	 */
	public int getDownloadTaskCount(Integer type) {
		List<DownloadItem> downloadItemList = null;
		if (taskMap != null) {
			downloadItemList = taskMap.get(type);
		}
		if (downloadItemList != null) {
			return downloadItemList.size();
		} else {
			return 0;
		}
	}

	/**
	 * @return the sysSoftwareList
	 */
	public List<Software> getSoftwareList() {
		return softwareList;
	}

	/**
	 * @param sysSoftwareList
	 *            the sysSoftwareList to set
	 */
	public void setSoftwareList(List<Software> sysSoftwareList) {
		this.softwareList = sysSoftwareList;
	}

	/**
	 * ���Ψһ��������Ѿ����ڵ���ɾ��ɵġ�
	 * 
	 * @param software
	 */
	public void addSoftware(Software software) {
		if (!softwareList.contains(software)) {
			softwareList.add(software);
		}
	}

	/**
	 * ���Ѿ���װ����л�����
	 * 
	 * @param pname
	 * @return
	 */
	public Software getSoftware(String pname) {
		for (Software item : softwareList) {
			if (item.getPackageName().equals(pname)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * ��һ�������ӵ�apk�б�ͷ����
	 */
	public void addLocalApk(Software software) {
		if (!localApkList.contains(software)) {
			localApkList.add(software);
		}
	}

	/**
	 * ɾ��һ��apk
	 */
	public Software deleteLocalApk(Software software) {
		boolean ok = localApkList.remove(software);
		if (ok) {
			return software;
		} else {
			return null;
		}
	}

	/**
	 * ��apk�б���apk
	 * 
	 * @param pname
	 * @return
	 */
	public Software getLocalApk(String pname, int versionCode) {
		for (Software item : localApkList) {
			if (item.getPackageName().equals(pname) && item.getVersionCode() == versionCode) {
				return item;
			}
		}
		return null;
	}

	/**
	 * @return the backupApkList
	 */
	public List<Software> getBackupApkList() {
		return backupApkList;
	}

	/**
	 * @param backupApkList
	 *            the backupApkList to set
	 */
	public void setBackupApkList(List<Software> backupApkList) {
		this.backupApkList = backupApkList;
	}

	/**
	 * 
	 * @param software
	 */
	public void addBackupApk(Software software) {
		if (!backupApkList.contains(software)) {
			backupApkList.add(software);
		}
	}

	/**
	 * ɾ��һ�����ݵ�apk��Ϣ
	 * 
	 * @param software
	 * @return
	 */
	public Software deleteBackupApk(Software software) {
		boolean ok = backupApkList.remove(software);
		if (ok) {
			return software;
		} else {
			return null;
		}
	}

	/**
	 * ����Ѿ�������ɾ��Ȼ����ӵ��µ�״̬�б�
	 */
	public void distributeDownloadItemToTaskList(DownloadItem downloadItem) {
		deleteDownloadTask(downloadItem.getPackageName(), downloadItem.getVersionCode());

		List<DownloadItem> dList = null;
		if (downloadItem.getState() == BaseApp.APP_DOWNLOADED) {
			dList = taskMap.get(BaseApp.APP_DOWNLOADED);
		} else if (downloadItem.getState() == BaseApp.APP_DOWNLOADING) {
			dList = taskMap.get(BaseApp.APP_DOWNLOADING);
		} else {
			dList = taskMap.get(BaseApp.APP_DOWNLOAD_STOP);
		}
		dList.add(downloadItem);
	}

	/**
	 * ���������
	 * 
	 * @param appId
	 *            ���id
	 * @return
	 */
	public DownloadItem getDownloadItem(int state, int appId) {
		List<DownloadItem> dList = null;
		if (state == BaseApp.APP_DOWNLOADED) {
			dList = taskMap.get(BaseApp.APP_DOWNLOADED);

		} else if (state == BaseApp.APP_DOWNLOADING) {
			dList = taskMap.get(BaseApp.APP_DOWNLOADING);

		} else {
			dList = taskMap.get(BaseApp.APP_DOWNLOAD_STOP);
		}

		for (DownloadItem item : dList) {
			if (item.getAppId() == appId) {
				return item;
			}
		}
		return null;
	}

	/**
	 * ���������
	 * 
	 * @param id
	 *            ��ݿ�id
	 * @return
	 */
	public DownloadItem getDownloadItemById(int id) {
		Collection<List<DownloadItem>> itemLists = taskMap.values();
		for (List<DownloadItem> list : itemLists) {
			for (DownloadItem downloadItem : list) {
				if (downloadItem.getId() == id) {
					return downloadItem;
				}
			}
		}
		return null;
	}
	
	/**
	 * ����������б�
	 * 
	 * @param type BaseApp.APP_DOWNLOAD????
	 * @return
	 */
	public List<DownloadItem> getDownloadListByType(int type) {
		return taskMap.get(type);
		
	}

	/**
	 * ɾ��һ�������Ϣ
	 */
	public Software deleteSoftware(String pname) {
		for (Software item : softwareList) {
			if (item.getPackageName().equals(pname)) {
				softwareList.remove(item);
				return item;
			}
		}

		return null;
	}

	/**
	 * �������Ƿ���ĳ������ĸ������
	 * 
	 * @return
	 */
	public boolean isAppUpdateInfo(AppItem appItem) {
		SoftwareUpdateTaskMark taskMark = marketContext.getTaskMarkPool().getSoftwareUpdateTaskMark(false);
		boolean inUpdate = marketContext.getAppCahceManager().isAppItemInCache(taskMark, appItem);
		return inUpdate;
	}

	/**
	 * �������Ƿ��Ѿ���װ��
	 */
	public boolean isAppInstalled(BaseApp baseApp) {
		for (Software item : softwareList) {
			if (item.getPackageName().equals(baseApp.getPackageName())
					&& item.getVersionCode() == baseApp.getVersionCode()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ɾ��һ�����apk�ļ���ֻ����ʱ��׿�г����ص������
	 * 
	 * @param savePath
	 *            ���ֵ�·��
	 */
	public void deleteHiApkFile(String savePath) {
		try {
			LogUtil.d(TAG, "deleteLocalHiApkFile: " + savePath);
			File file = new File(savePath);
			if (file.exists() && file.isFile() && file.getAbsolutePath().contains(Constants.DOWNLOAD_DIR)) {
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ɾ��һ�������apk�ļ�
	 * 
	 * @param savePath
	 *            ���ֵ�·��
	 */
	public void deleteApkFile(String savePath) {
		try {
			LogUtil.d(TAG, "deleteLocalApkFile: " + savePath);
			File file = new File(savePath);
			if (file.exists() && file.isFile()) {
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ɾ��һ������ �ṩ����Ѱ�װ���ʹ�á�
	 */
	public DownloadItem deleteDownloadTask(String pname, int versionCode) {
		Collection<List<DownloadItem>> dListSet = taskMap.values();
		for (List<DownloadItem> list : dListSet) {
			for (DownloadItem item : list) {
				if (item.getPackageName().equals(pname) && item.getVersionCode() == versionCode) {
					list.remove(item);
					return item;
				}
			}
		}

		return null;
	}

	/**
	 * ɾ��һ������
	 * 
	 * @param appId
	 *            ��Ӧ�����id
	 */
	public DownloadItem deleteDownloadTask(int appId) {
		Collection<List<DownloadItem>> dListSet = taskMap.values();
		for (List<DownloadItem> list : dListSet) {
			for (DownloadItem item : list) {
				if (item.getAppId() == appId) {
					list.remove(item);
					return item;
				}
			}
		}

		return null;
	}

	/**
	 * show for label (TextView)
	 */
	public String getAppViewDescribe(BaseApp baseApp) {
		// ����и���ֱ�ӷ����и���
		int state = getJointSoftwareState(baseApp);
		switch (state) {
		case BaseApp.APP_DOWNLOADED:
			return marketContext.getString(R.string.downloaded);

		case BaseApp.APP_DOWNLOADING:
			return marketContext.getString(R.string.downloading);

		case BaseApp.APP_INSTALLING:
			return marketContext.getString(R.string.installing);

		case BaseApp.APP_INSTALLED: // ������Ѿ���װ���Ҫ�����Ƿ��и���
			Software software = getSoftware(baseApp.getPackageName());
			if (software != null && software.isUpdate()) {
				return marketContext.getString(R.string.updatable);
			} else {
				return marketContext.getString(R.string.installed);
			}

		default:
			if (baseApp.isPurchase()) {
				return marketContext.getString(R.string.purchased);
			} else {
				return null;
			}
		}
	}

    /**
     * show for action (Button)
     */
    public String getAppViewDescribeForNextAction(BaseApp baseApp) {
        //
        int state = getJointSoftwareState(baseApp);
        switch (state) {
            case BaseApp.APP_DOWNLOADED:
                return marketContext.getString(R.string.install);

            case BaseApp.APP_DOWNLOADING:
                return marketContext.getString(R.string.downloading);

            case BaseApp.APP_INSTALLING:
                return marketContext.getString(R.string.installing);

            case BaseApp.APP_INSTALLED: // ������Ѿ���װ���Ҫ�����Ƿ��и���
                Software software = getSoftware(baseApp.getPackageName());
                if (software != null && software.isUpdate()) {
                    return marketContext.getString(R.string.updatable);
                } else {
                    return marketContext.getString(R.string.open);
                }

            default:
                if (baseApp.isPurchase()) {
                    return marketContext.getString(R.string.purchased);
                } else {
                    return null;
                }
        }
    }

	/**
	 * ����Ƿ�һ�������Ҫ����
	 */
	public boolean isNeedDownloadApk(BaseApp baseApp) {
		int state = getJointSoftwareState(baseApp);
		return (state == BaseApp.APP_NEW || state == BaseApp.APP_INSTALLED || state == BaseApp.APP_DOWNLOAD_STOP);
	}

	/*
	 * �������ػ�װ��
	 */
	public boolean isInHandling(BaseApp baseApp) {
		int state = getJointSoftwareState(baseApp);
		return isInHandling(state);
	}

	/**
	 * �ж�һ��״̬
	 * 
	 * @param state
	 * @return
	 */
	public boolean isInHandling(int state) {
		return (state == BaseApp.APP_DOWNLOADING || state == BaseApp.APP_INSTALLING);
	}

	/**
	 * ͨ����ϵͳ��Ϣ��ͬ����ǰ��������״̬<br>
	 * ϵͳ�����漰״̬�ĵط�����Ƚ�Ӧ�ô���ͬ��һ��״̬�� ��Ϊ����ͬһ����Ĳ�ͬ�汾�����Ա����ж��汾��<br>
	 * ע��״̬���ȼ������� -- �Ѱ�װ -- ����apk
	 */
	public int getJointSoftwareState(BaseApp baseApp) {
		String pname = baseApp.getPackageName();
		int versionCode = baseApp.getVersionCode();
		// ������
		DownloadItem downloadItem = getDownloadItem(BaseApp.APP_DOWNLOADED, baseApp.getId());
		if (downloadItem != null) {
			return downloadItem.getState();
		}

		// ������
		downloadItem = getDownloadItem(BaseApp.APP_DOWNLOADING, baseApp.getId());
		if (downloadItem != null) {
			return downloadItem.getState();
		}

		// �Ѱ�װ״̬
		for (Software softWare : softwareList) {
			// ��������ð����ֽ��л���
			if (pname.equals(softWare.getPackageName()) && versionCode == softWare.getVersionCode()) {
				return softWare.getState();
			}
		}

		// ������
		for (Software softWare : localApkList) {
			// ��������ð����ֽ��л���
			if (pname.equals(softWare.getPackageName()) && versionCode == softWare.getVersionCode()) {
				return softWare.getState();
			}
		}

		return BaseApp.APP_NEW;
	}

	/**
	 * ͨ����ϵͳ��Ϣ��ͬ����ǰ��������״̬<br>
	 * ϵͳ�����漰״̬�ĵط�����Ƚ�Ӧ�ô���ͬ��һ��״̬��
	 */
	public String getJointApkSavePath(String pname, int versionCode) {
		// ������
		List<DownloadItem> dList = taskMap.get(BaseApp.APP_DOWNLOADED);
		for (DownloadItem item : dList) {
			// ��������ð����ֽ��л���
			if (pname.equals(item.getPackageName()) && versionCode == item.getVersionCode()) {
				return item.getSavePath();
			}
		}

		// ����apk
		for (Software softWare : localApkList) {
			// ��������ð����ֽ��л���
			if (pname.equals(softWare.getPackageName()) && versionCode == softWare.getVersionCode()) {
				return softWare.getApkPath();
			}
		}

		return null;
	}

	/**
	 * ��������صĹ�ͬ���apk�ı���·��
	 * 
	 * @param appItem
	 * @return
	 */
	public String getLcoalApkPath(AppItem appItem) {
		String apkPath = null;
		DownloadItem downloadItem = getDownloadItem(BaseApp.APP_DOWNLOADED, appItem.getId());
		if (downloadItem != null) {
			apkPath = downloadItem.getSavePath();

		} else {
			Software software = getLocalApk(appItem.getPackageName(), appItem.getVersionCode());
			if (software != null) {
				apkPath = software.getApkPath();
			}
		}
		return apkPath;
	}

	/**
	 * ��������صĹ�ͬ��
	 * 
	 * @param appItem
	 * @return
	 */
	public BaseApp getLcoalAppItem(AppItem appItem) {
		DownloadItem downloadItem = getDownloadItem(BaseApp.APP_DOWNLOADED, appItem.getId());
		if (downloadItem != null) {
			return downloadItem;

		} else {
			Software software = getLocalApk(appItem.getPackageName(), appItem.getVersionCode());
			return software;
		}
	}

    private boolean hasSystemPermission() {
        return ((marketContext.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM);
        //checkCallingOrSelfPermission("android.permission.INSTALL_PACKAGES");
        //return false;
    }

    /**
     * @param path
     */
	public void installSoftware(String path) {
        if (hasSystemPermission()) {
            try {
                Log.d("temp", "install sw has permission: path=" + path);
                Runtime.getRuntime().exec("pm install -r " + new File(path));
            } catch (IOException e) {
                Log.d("temp", "install sw: exception: e="+e.toString());
                e.printStackTrace();
            }
        }
        else {
            Log.d("temp", "install sw: no permisson###");
            boolean ok = checkSDCardStateAndNote();
            if (ok) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                //
                intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                marketContext.startActivity(intent);
            }
        }
	}

	/**
	 * ��װһ�����
	 */
	public void installHiApk(int appId) {
		boolean ok = checkSDCardStateAndNote();
		if (ok) {
			String saveDir = getSoftwareSaveDir();
			String savePath = getSoftwareSavePath(saveDir, appId);
			installSoftware(savePath);
		}
	}

	/**
	 * ����ϵͳ�����ʾ���ϸ��
	 * 
	 * @param pname
	 *            ����
	 */
	public void showInstalledAppDetail(Activity activity, String pname) {
		try {
			Intent intent = null;
			boolean useNew = Util.useNewAppAttrView();
			// android 2.3 �鿴����������޸�
			if (useNew) {
				intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
				intent.setClassName("com.android.settings", "com.android.settings.applications.InstalledAppDetails");
			} else {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");

			}
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			// fw = 2.3
			intent.setData(Uri.fromParts("package", pname, null));
			// fw = 2.2
			intent.putExtra("pkg", pname);
			// fw <= 2.1
			intent.putExtra("com.android.settings.ApplicationPkgName", pname);

			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			if (activity != null) {
				activity.startActivity(intent);

			} else {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				marketContext.startActivity(intent);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * uninstall sw.
	 * @param pname
	 */
	public void uninstallSoftware(String pname) {
		try {
			Uri packageURI = Uri.parse("package:" + pname);
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			marketContext.startActivity(uninstallIntent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * open sw
	 * @param pname
	 */
	public void openSoftware(String pname) {
		PackageManager pm = marketContext.getPackageManager();
		try {
			Intent intent = pm.getLaunchIntentForPackage(pname);
			if (intent != null) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				marketContext.startActivity(intent);
			} else {
				Toast.makeText(marketContext, marketContext.getString(R.string.software_error_cant_boot), 200).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���app���ر���Ŀ¼ <br>
	 * �ض�ʱ���������Ҫ����checkSDCardStateAndNote()��ȷ״̬
	 * 
	 * @param context
	 * @return
	 */
	public String getSoftwareSaveDir() {
		// ȷ�������Ƿ���Խ�������
		File file = Environment.getExternalStorageDirectory();
		if (file.exists()) {
			File downloadFile = new File(file.getAbsolutePath() + File.separator + Constants.DOWNLOAD_DIR);
			if (!downloadFile.exists()) {
				downloadFile.mkdirs();
			}
			return downloadFile.getAbsolutePath();

		} else {
			return null;
		}
	}

	/**
	 * ���app���ر���Ŀ¼
	 * 
	 * @param context
	 * @return
	 */
	public boolean checkSDCardStateAndNote() {
		// ȷ�������Ƿ���Խ�������
		String state = Environment.getExternalStorageState();
		LogUtil.i(TAG, "sdcard state: " + state);
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;

		} else if (Environment.MEDIA_REMOVED.equals(state)) {
			Toast.makeText(marketContext, marketContext.getString(R.string.insert_sdcard_first), 150).show();

		} else if (Environment.MEDIA_SHARED.equals(state)) {
			Toast.makeText(marketContext, marketContext.getString(R.string.sdcard_using_close_first), 150).show();

		} else {
			Toast.makeText(marketContext, marketContext.getString(R.string.sdcard_state_unusual), 150).show();
		}
		return false;
	}

	/**
	 * ����������� ����·��
	 * 
	 * @param pname
	 * @return
	 */
	public String getSoftwareSavePath(String saveDir, int appId) {
		return saveDir + File.separator + appId;
	}

	/**
	 * ��ջ���
	 */
	public void reinitSystemManager() {
		// ��������б�
		softwareList.clear();
		localApkList.clear();
		taskMap.clear();
	}

	/**
	 * @return the taskList
	 */
	public Map<Integer, List<DownloadItem>> getTaskMap() {
		return taskMap;
	}

	/**
	 * @param taskList
	 *            the taskList to set
	 */
	public void setTaskMap(Map<Integer, List<DownloadItem>> taskMap) {
		this.taskMap = taskMap;
	}

	/**
	 * @return the apkList
	 */
	public List<Software> getLocalApkList() {
		return localApkList;
	}

	/**
	 * @param apkList
	 *            the apkList to set
	 */
	public void setLocalApkList(List<Software> apkList) {
		this.localApkList = apkList;
	}

	/**
	 * ����г��Ѿ���ʱ����
	 * 
	 * @param appId
	 *            ���id���������id
	 * @return
	 */
	public DownloadItem getMarketDownloadedItem() {
		List<DownloadItem> dList = taskMap.get(BaseApp.APP_DOWNLOADED);
		String marketPNmae = marketContext.getPackageName();
		for (DownloadItem item : dList) {
			if (marketPNmae.equals(item.getPackageName())) {
				return item;
			}
		}
		return null;
	}

    public static void shareChooser(Context context, String title, String subject) {
        final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, subject);
        final Intent chooserIntent = Intent.createChooser(intent, context.getString(R.string.share));
        try {
            context.startActivity(chooserIntent);
        } catch (ActivityNotFoundException ignore) {
        }
    }

    public void launchMarket(String pn) {
        final Uri uri = Uri.parse("market://details?id=" + pn);
        final Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            myAppLinkToMarket.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            marketContext.startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
//            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

//    public void launchMarcket(String packageName) {
//        if (isAvailable(this, "com.android.vending")) {
//            launchMarket(packageName);
//        }
//        else
//        {
//            Uri uri = Uri.parse("market://search?q="+ packageName);
//            Intent it   = new Intent(Intent.ACTION_VIEW,uri);
//            marketContext.startActivity(it);
//        }
//    }
}
