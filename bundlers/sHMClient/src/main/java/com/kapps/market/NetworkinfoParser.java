package com.kapps.market;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

import com.kapps.market.log.LogUtil;

/**
 * 2010-12-3<br>
 * 
 * @author shuizhu
 * 
 */
public class NetworkinfoParser {

	public static final String TAG = "NetworkinfoParser";

	// wap ����ip�Ͷ˿�
	public static final String WAP_PROXY_IP = "10.0.0.172";
	public static final String CT_WAP_PROXY_IP = "10.0.0.200";

	public static final int WAP_PORT = 80;

	// �Ƿ��Ѿ������罨������
	private static boolean netConnect;
	// ʹ�õ���������
	private static int netType = -1;
	// ʹ�õ��������������
	private static String netSubTypeName;
	// ���������Ϣ��һ�������ж�wap����������
	private static String netExtraType;

	/**
	 * �������������û�������<br>
	 * ��ȷ�����ʵ��Ч������������ע��wifi�����
	 * 
	 * @param networkInfo
	 *            ������Ϣ
	 */
	public static void parserNetinfo(NetworkInfo networkInfo, MApplication imContext) {
		// ����ⲿ�����������Ϣ�ǿյģ�����ʹ��ϵͳ��ǰ��Ϣ
		if (networkInfo == null) {
			ConnectivityManager conManager = (ConnectivityManager) imContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (conManager != null) {
				networkInfo = conManager.getActiveNetworkInfo();
			}
		}

		// ��������״̬
		if (networkInfo == null || networkInfo.getState() == State.DISCONNECTED) {
			netConnect = false;
			netType = -1;
			netSubTypeName = null;
			netExtraType = null;

		} else if (networkInfo.getState() == State.CONNECTED) {
			netConnect = true;
			netType = networkInfo.getType();
			netSubTypeName = networkInfo.getSubtypeName();
			netExtraType = networkInfo.getExtraInfo();
		}
	}

	/**
	 * ����HttpClinet���Ӳ���
	 */
	public static DefaultHttpClient getHttpConnector(MApplication imContext) {
		// ���¼������״̬
		parserNetinfo(null, imContext);

		DefaultHttpClient httpClient = new DefaultHttpClient();
		// �Ƿ��ʼ����
		if (imContext.isBaseDataOk()) {
			if (netConnect) {
				if (netExtraType != null) {
					if (netExtraType.toLowerCase().contains("ctwap")) {
						HttpHost proxy = new HttpHost(CT_WAP_PROXY_IP, WAP_PORT);
						httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
						LogUtil.d(TAG, "getHttpConnector -- set ctwap proxy for connector");

					} else if (netExtraType.contains("wap")) {
						HttpHost proxy = new HttpHost(WAP_PROXY_IP, WAP_PORT);
						httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
						LogUtil.d(TAG, "getHttpConnector -- set wap proxy for connector");

					} else {
						LogUtil.d(TAG, "getHttpConnector -- common connector");
					}
				}
			}
		}
		return httpClient;
	}

	/**
	 * ��ǰ�Ƿ���wap����
	 */
	public static boolean isWapConnector(MApplication imContext) {
		// ���¼������״̬
		parserNetinfo(null, imContext);

		// �Ƿ��ʼ����
		if (imContext.isBaseDataOk()) {
			if (netConnect && netExtraType != null) {
				if (netExtraType.toLowerCase().contains("wap")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * �Ƿ���wifi����
	 */
	public static boolean isWifiConnector(MApplication imContext) {
		// ���¼������״̬
		parserNetinfo(null, imContext);

		// �Ƿ��ʼ����
		if (imContext.isBaseDataOk()) {
			if (netConnect && netType == ConnectivityManager.TYPE_WIFI) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the netConnect
	 */
	public static boolean isNetConnect(MApplication imContext) {
		// ���¼������״̬
		parserNetinfo(null, imContext);

		return netConnect;
	}

	/**
	 * @return the netConnect
	 */
	public static boolean isRawNetConnect(MApplication imContext) {
		return netConnect;
	}

	/**
	 * @return the netType
	 */
	public static int getNetType(MApplication imContext) {
		// ���¼������״̬
		parserNetinfo(null, imContext);

		return netType;
	}

	public static String getNetSubTypeName(MApplication imContext) {
		// ���¼������״̬
		parserNetinfo(null, imContext);

		return netSubTypeName;
	}

	/**
	 * @return the netExtraType
	 */
	public static String getNetExtraType(MApplication imContext) {
		// ���¼������״̬
		parserNetinfo(null, imContext);

		return netExtraType;
	}

}
