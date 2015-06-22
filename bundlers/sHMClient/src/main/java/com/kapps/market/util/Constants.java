package com.kapps.market.util;

/**
 * 2010-6-8 ͨ��Э���еı�ʾ��Ӧ���й�������<br>
 * 
 * һЩ���ó���˵��<br>
 * ������id <br>
 * 
 * @author admin
 * 
 */
public interface Constants {

	public static String VERSION = "1.0";
	// ��Ч
	public static final int NONE_ID = -88;

	// Ĭ�ϵ��г�Ŀ¼ sdcard/mgy_market
	public static final String DEFAULT_BASE_Dir = "mgy_market";
	// Ĭ�ϵ��Ѱ�װ�������Ŀ¼
	public static final String DEFAULT_SOFTWARE_BACKUP_DIR = DEFAULT_BASE_Dir + "/backup";
	// Ĭ�ϵı���apk����Ŀ¼ sdcard/download
	public static final String DEFAULT_LOCAL_APK_DIR = "download";
	// ���ر���Ŀ¼ sdcard/.0102
	public static final String DOWNLOAD_DIR = ".mgy_temp";
	// Ĭ�ϵİ�װĿ¼
	public static final String DEFAULT_APP_INSTALL_DIR = "/data/app";
	/**
	 * �û���Ϣ��������
	 */
	public static String USER_INFO_PREF = "u_pref";
	/**
	 * �г�����
	 */
	public static String MARKET_CONFIG_PREF = "m_pref";

	/**
	 * Have setup shortcut?
	 */
	public static String EXIST_SHORT_CUT_PREF_KEY = "shortcut_exist";
    /**
     * shui: user ignore a version.
     */
    public static String USER_IGNORED_VERSION_CODE = "ignored_version_code";

	// ����
	public static final String TYPE_SEARCH_ID = "-3";
	// �ҵ�����
	public static final String TYPE_MYDOWNLOAD_ID = "-2";
	// ���ܻ�ӭ
	public static final String Type_POP_ID = "-1";
	// ������
	public static final String TYPE_SORT_RATING = "0";
	// ����
	public static final String TYPE_SORT_TIME = "1";

	public static final String CODE_NO_CONTENT = "204";
	public static final String CODE_NOT_MODIFIED = "304";

	// �û����(���˺�)
	public static final String USER_NAME = "user_1";
	public static final String USER_PASSWORD = "user_2";
	public static final String USER_EMAIL = "user_3";
	// �Ự
	public static final String USER_SID = "sid";

	// ��½״̬
	public static final String LOGIN_INVOKE_STATE = "login_invoke_state";

	// �Ƿ��ǲ������ӵ�½���
	// �ӵ�¼���һ�㷢�����û����ػ���ʱ��ʾ�û���½��
	public static final int CM_LOGIN = 0;
	public static final int SUB_LOGIN = 1;
	public static final int RE_LOGIN = 2;
	public static final int VIEW_LOGIN = 3;

	// ����ͼ��ʾ
	public static final String QUICK_VIEW = "quick_view";
	public static final String QUICK_DOWNLOAD_TASK_VIEW = "download_task_view";
	public static final String QUICK_SOFT_MANAGE_VIEW = "soft_manage_view";

	// ������ϸframe�ķ�ʽ
	public static final String DETAIL_AUTHOR_APP = "authorApp";
	public static final String DETAIL_HISTORY_APP = "historyApp";
	public static final String DETAIL_LOCALSOFTWARE_APP = "softwareApp";
	public static final String DETAIL_DOWNLOADABLE_APP = "purchaseApp";

	// �ö������
	public static final int TOP_AD_COUNT = 4;

	// δ֪��ͼ
	public static final int NONE_VIEW = -9999;
	// Ϊ֪����
	public static final String NONE_AUTHOR = "none_author";
	public static final String APP_PACKAGE_NAME = "package_name";
	public static final String APP_VERSION = "app_version";
	public static final String APP_VERSION_CODE = "app_version_CODE";

	// �����Դ
	public static final String APP_SOURCE = "app_source";
	public static final String APP_PATH = "app_path";

	// http header
	public static final String H_SESSION_ID = "sid";
	public static final String H_SDK_VERSION = "sdk";
	public static final String H_RESOLUTION = "res";
	public static final String H_DENSITY = "den";
	public static final String H_AUTHORIZATIONS = "auth";
	public static final String H_LANGUAGE = "applang";
	public static final String H_SER_TS = "ts";
	public static final String H_RESUME_DOWNLOAD = "dresume";
	// �Ƿ��Ƕϵ���
	public static final String DRESUME_NO = "0";
	public static final String DRESUME_YES = "1";
	// ���У�������
	public static final String SEC_SIGN = "sign";
	public static final String SEC_KEY_STRING = "hcamdc007";
	public static final String APP_NAME = "app_name";
	// ע�������·������Ŀ¼���ļ���
	public static final String APP_SAVE_DIR = "app_save_dir";
	public static final String APP_ID = "app_id";
	public static final String APP_SIZE = "app_size";
	public static final String APP_DSIZE = "app_dsize";
	public static final String DOWNLOAD_ITEM_ID = "download_item_id";

	// ѡ�еĽ�ͼ����
	public static final String CHOOSED_SCREEN_INDEX = "choosed_screen_index";

	// �ɹ���ʧ��
	public static final int SUCCESS = 0;
	public static final int FAILURE = 1;

	// message what �޷������������
	// ж��/��װ���
	public static final int M_UNINSTALL_APK = 10;
	public static final int M_INSTALL_APK = 11;
	public static final int M_DOWNLOAD_FORBID = 12;
	public static final int M_DOWNLOAD_COMPLETED = 13;
	public static final int M_DOWNLOAD_STOP = 14;
	public static final int M_DOWNLOAD_FAIL = 114;
	public static final int M_DOWNLOAD_PROGRESS = 16;
	public static final int M_DOWNLOAD_CANCEL = 17;
	public static final int M_DOWNLOAD_RETRY = 18;
	public static final int M_FAVOR_ADDED = 19;
	public static final int M_FAVOR_DELETE = 20;
	public static final int M_BADNESS_SHOW_VIEW = 21;
	public static final int M_BADNESS_Ok = 22;
	public static final int M_BADNESS_CANCEL = 23;
	public static final int M_PERMISSION_SHOW_VIEW = 24;
	public static final int M_DOWNLOAD_ACCEPT = 25;
	public static final int M_PERMISSION_BACk = 26;
	public static final int M_SHOW_APP_OTHER_VERSION = 27;
	
	public static final int M_OTHER_VERSION_SHOW = 500;
	// ��ͼ
	public static final int M_SCREEN_SHOTS_SHOW = 28;
	// ��������
	public static final int M_BATCH_DOWNLOAD_APP = 29;
	// �������سɹ��ύ
	public static final int M_BATCH_DOWNLOAD_APP_OK = 33;
	// ȷ���������ݳɹ��ύ
	public static final int M_BATCH_BACKUP_SOFTWARE_OK = 38;
	// ������ԭ�ɹ��ύ
	public static final int M_BATCH_RECOVER_SOFTWARE_OK = 39;

	// ��������
	public static final int M_BATCH_UPDATE_APP = 36;
	// ������°�ȫ���ڱ��ص����
	public static final int M_BATCH_UPDATE_APP_LOCAL = 43;
	// �������³ɹ��ύ
	public static final int M_BATCH_UPDATE_APP_OK = 37;

	// ��̬�����
	public static final int M_CHECK_SATIC_AD = 44;
	// ��̬��棬���֪ͨ����Ϊ
	public static final int M_NOTIFY_STATIC_AD = 45;

	// ���µ�¼
	public static final int M_LOGIN_OUT = 30;
	// ���֪ͨ����Ϊ
	public static final int M_NOTIFY_APP_DOWNLOAD_LIST = 1000;
	// ĳ������Ѿ����ؽ���
	public static final int M_NOTIFY_APP_DOWNLOADED = 1001;
	public static final int M_NOTIFY_SOFT_UPDATE = 1002;
	public static final int M_NOTIFY_MARKET_UPDATE = 1003;
	public static final int M_NOTIFY_SOFTWARE_INSTALL = 1004;

	// �������
	public static final int M_QUICK_DOWNLOAD_APP = 40;
	public static final int M_QUICK_PAYMENT_APP = 41;
	public static final int M_PREVIEW_INIT_OVER = 201;
	public static final int M_SOFTWARE_UPDATED = 202;
	public static final int M_CHECK_MS_UPDATE = 203;
	public static final int M_CLOSE_MAIN_FRAME = 204;
	// ����������
	public static final int M_REPORT_CHANNEL = 205;

	// Ԥ�ȳ�ʼ��
	public static final int M_DELAY_GC = 72;

	// �����ĺ����Ϣ
	public static final int M_PARSE_NETWORKINFO = 73;

	// �Ƿ��ǽ�ͼ�������
	public static final int SCREENT_VIEW_CLOSE = 101;

	// ��Ϣ��ֹ
	public static final int M_MESSAGE_END = -494949;

	// ��ʾ����µ����
	public static final int M_SHOW_CATEGORY_APP = 300;

	// ///////////////////////////////////��Ϣ֪ͨ
	// ��װ���
	public static final int M_NOTE_DOWNLOAD_COMPLETE = 407;

	//���Ѻ�ɹ���ʼ������Ϣ
	public static final int M_DOWNLOAD_AFTER_PAY_SUCCESS=408;
	public static final int M_DOWNLOAD_AFTER_PAY_FAIL=409;
	// intent ����
	public static final String INTENT_OP_MARK = "intnet_op_mark";
	public static final String INTENT_DELETE_PACKAGE = "intent_delete_package";
	public static final String INTENT_ADD_PACKAGE = "intent_add_package";

	// activity request code
	public static final int ACTIVITY_RCODE_APPDETAIL = 0;

	// activity result data mark
	// ���������һ���������
	public static final int ACTIVITY_DCODE_PNAME = 1;

	// service
	public static final String ACTION_SERVICE_DOWNLOAD_REQUEST = "com.kapps.market.ACTION_SERVICE_DOWNLOAD_REQUEST";
	public static final String ACTION_SERVICE_DOWNLOAD_CANCEL = "com.kapps.market.ACTION_SERVICE_DOWNLOAD_CANCEL";
	public static final String ACTION_SERVICE_DOWNLOAD_REVERSE = "com.kapps.market.ACTION_SERVICE_DOWNLOAD_REVERSE";
	public static final String ACTION_SERVICE_DOWNLOAD_STOP = "com.kapps.market.ACTION_SERVICE_DOWNLOAD_STOP";

	// recevier action
	public static final String ACTION_CHOOSE_DOWNLOAD_APP_COMPLETED = "com.kapps.market.ACTION_CHOOSE_DOWNLOAD_APP_COMPLETED";
	public static final String ACTION_CHOOSE_APP_DOWNLOAD_LIST_NOTIFY = "com.kapps.market.ACTION_CHOOSE_APP_DOWNLOAD_LIST_NOTIFY";

	public static final String ACTION_CHOOSE_SOFT_UPDATE_NOTIFY = "com.kapps.market.ACTION_CHOOSE_SOFT_UPDATE_NOTIFY";
	public static final String ACTION_CHOOSE_MARKET_UPDATE_NOTIFY = "com.kapps.market.ACTION_CHOOSE_MARKET_UPDATE_NOTIFY";
	public static final String ACTION_CHECK_UPDATE_NOTIFY = "com.kapps.market.ACTION_UPDATE_CHECK_NOTIFY";
	// ��̬���
	public static final String ACTION_CHECK_STATIC_AD_NOTIFY = "com.kapps.market.ACTION_STATIC_AD_CHECK_NOTIFY";
	public static final String ACTION_CHOOSE_STATIC_AD_NOTIFY = "com.kapps.market.ACTION_CHOOSE_STATIC_AD_NOTIFY";

	// ����ղ����
	public static final String ACTION_ADD_FAVOR_APP = "com.kapps.market.ACTION_ADD_FAVOR_APP";
	public static final String ACTION_DELETE_FAVOR_APP = "com.kapps.market.ACTION_DELETE_FAVOR_APP";

	// ���ն���
	public final static String SMS_RECEIVED_FLAG = "android.provider.Telephony.SMS_RECEIVED";

	// ---------------------------------------�����õ��ĳ���
	// ����apk�ļ�"��С"�ٽ�� 5M
	public static final int APK_FILE_CRITICAL_SIZE = 5000000;
	// ����apk�ļ������ٽ��500K��һ��
	public static final int APK_LARGE_PROGRESS_SIZE = 500000;

}
