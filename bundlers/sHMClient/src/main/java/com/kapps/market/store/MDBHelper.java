package com.kapps.market.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.kapps.market.log.LogUtil;

/**
 * 2010-6-29 ��ݿ������ Helper ��
 * 
 * @author admin
 * 
 */
public class MDBHelper extends SQLiteOpenHelper {

	public static final String TAG = "MDBHelper";

	// ��ݿ�����
	public static final String DB_NAME = "mgy_market.db";
	// ��ݿ�汾
	public static final int DB_VERSION = 5;

	// ��ݿ����
	// �������ؼ�¼��
	private static final String CREATE_APP_DOWNLOAD_TABLE = "create table " + TAppDownload.TABLE_NAME + " ("
			+ TAppDownload._ID + " integer primary key autoincrement," + TAppDownload.APP_ID + " int,"
			+ TAppDownload.PNAME + " text," + TAppDownload.NAME + " text," + TAppDownload.VERSION + " text,"
			+ TAppDownload.VERSION_CODE + " int," + TAppDownload.DURL + " text," + TAppDownload.IURL + " text,"
			+ TAppDownload.STATE + " integer, " + TAppDownload.DSIZE + " real," + TAppDownload.SIZE + " real)";

	// �ҵ��ղ�
	// private static final String CREATE_APP_FAVOR_TABLE = "create table "
	// + TFavorApp.TABLE_NAME + " (" + TFavorApp._ID
	// + " integer primary key autoincrement," + TFavorApp.APP_ID
	// + " integer," + TFavorApp.USER + " text," + TFavorApp.PNAME
	// + " text," + TFavorApp.NAME + " text," + TFavorApp.D_URL
	// + " text," + TFavorApp.VERSION + " text," + TFavorApp.TIME
	// + " text)";

	public MDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		LogUtil.d("DBHelp", "oncreate db: " + DB_NAME);
		// Ӧ������
		db.execSQL(CREATE_APP_DOWNLOAD_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		LogUtil.d(TAG, "onUpgrade oldVersion=" + oldVersion + ", newVersion=" + newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + TAppDownload.TABLE_NAME);
		onCreate(db);
	}

	// Ӧ�õ����ر�
	public static class TAppDownload implements BaseColumns {
		public static final String TABLE_NAME = "app_download";
		// �����id
		public static final String APP_ID = "app_id";
		// ��
		public static final String PNAME = "pname";
		// ����
		public static final String NAME = "name";
		// �汾
		public static final String VERSION = "version";
		// �汾��
		public static final String VERSION_CODE = "version_code";
		// ��¼���ص�url
		public static final String DURL = "durl";
		// ͼ���С
		public static final String IURL = "iurl";
		// ״̬
		public static final String STATE = "state";
		// ��������(KB)
		public static final String DSIZE = "dsize";
		// �ܴ�С(KB)
		public static final String SIZE = "size";

	}

	//
	// // �ҵ��ղ�
	// public static class TFavorApp implements BaseColumns {
	// public static String TABLE_NAME = "favor_app";
	// // ��Ӧ�������id
	// public static final String APP_ID = "app_id";
	// // �û�����
	// public static final String USER = "user";
	// // ��ӵ�ϲ�õ�ʱ��
	// public static final String TIME = "time";
	// // ��
	// public static final String PNAME = "pname";
	// // ����
	// public static final String NAME = "name";
	// // �汾
	// public static final String VERSION = "version";
	// // ���ص�url
	// public static final String D_URL = "dUrl";
	// }

}
