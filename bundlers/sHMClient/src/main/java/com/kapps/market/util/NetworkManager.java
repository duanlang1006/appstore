package com.kapps.market.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class NetworkManager {

	private final static String TAG = "DeamoService";
	private boolean mbDataConnected = false;
	private boolean mbWifiConneted = false;

	private Context mContext = null;
	private OnNetworkStateListener mListener = null;
	private TelephonyManager mTelephonyMgr = null;
	private boolean mbInit = false;

	public interface OnNetworkStateListener {
		public void OnStateChanged(boolean bConneted);
	}

	public NetworkManager(Context context) {
		mContext = context;
	}

	public void init() {
		if (!mbInit) {
			mTelephonyMgr = (TelephonyManager) mContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (null == mTelephonyMgr) {
				return;
			}

			IntentFilter inf = new IntentFilter();
			inf.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			mContext.registerReceiver(mReceiver, inf);

			mTelephonyMgr.listen(new PhoneStateListener() {
				@Override
				public void onDataConnectionStateChanged(int state) {
					super.onDataConnectionStateChanged(state);
					switch (state) {
					case TelephonyManager.DATA_DISCONNECTED:// 网络断开
						mbDataConnected = false;
						if (null != mListener) {
							mListener.OnStateChanged(false);
						}
						break;
					case TelephonyManager.DATA_CONNECTING:// 网络正在连接
						break;
					case TelephonyManager.DATA_CONNECTED:// 网络连接丄1�7
						mbDataConnected = true;
						if (null != mListener) {
							mListener.OnStateChanged(true);
						}
						break;
					}
				}
			}, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
			mbDataConnected = isNetworkActive(mContext);
			mbInit = true;
		}
	}

	public void uninit() {
		if (mbInit) {
			mContext.unregisterReceiver(mReceiver);
		}
	}

	public static String callHttp(String uri) {
		try {
			HttpGet net = new HttpGet(uri);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse httpRsp = httpClient.execute(net);
			if (httpRsp.getStatusLine().getStatusCode() == 200) {
				return EntityUtils.toString(httpRsp.getEntity());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				NetworkInfo info = (NetworkInfo) intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				boolean isDisconnected = info.getState().equals(
						State.DISCONNECTED);
				if (info.isConnected()) {
					if (!mbWifiConneted && null != mListener) {
						mListener.OnStateChanged(true);
					}
					mbWifiConneted = true;
				} else if (isDisconnected) {
					if (mbWifiConneted && null != mListener) {
						mListener.OnStateChanged(false);
					}
					mbWifiConneted = false;
				}

			}
		}
	};

	private static boolean isNetworkActive(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null) {
				return true;
			}
		}
		return false;
	}

	public void setListener(OnNetworkStateListener listener) {
		mListener = listener;
	}

	public boolean isNetworkConneted() {
		return isNetworkActive(mContext);
	}

	public boolean isCallIdle() {
		return mTelephonyMgr.getCallState() == TelephonyManager.CALL_STATE_IDLE;
	}

	public String getDeviceId() {
		if (!mbInit) {
			return null;
		}
		return mTelephonyMgr.getDeviceId();
	}
}
