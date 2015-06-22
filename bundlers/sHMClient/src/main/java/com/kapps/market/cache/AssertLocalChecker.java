package com.kapps.market.cache;

import android.os.SystemClock;
import android.util.Log;

/**
 * 2010-9-4 <br>
 * 
 * @author admin
 * 
 */
public class AssertLocalChecker extends Thread {

	public static final String TAG = "AssertChecker";
	public static final boolean logCheck = false;
	// ���ػ���
	private LocaleCacheManager localeCacheManager;

	// ��֪��鱾�ػ����ʱ�����Ļ�����¼�¼ see: LocaleCacheManager.getLastSave()
	// �����ʱ���ֵ��LocaleCacheManager.getLastSave()��ֵһ�±�ʾ����û�б仯����ô���ڼ�顣
	private long lastCheck = -49;
	// ��һ��
	private boolean first = true;
	// ��ʱ����
	private static final int CHECK_DELAY = 1000 * 20;
	// �����һ����
	private static final int CHECK_PERIOD = 1000 * 60;

	public AssertLocalChecker(LocaleCacheManager localeCacheManager) {
		this.localeCacheManager = localeCacheManager;
	}

	// ��ʼ���
	public void beginCheckAssert() {
		if (!isAlive()) {
			start();

		} else {
			throw new IllegalStateException("check already begin!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (true) {
			if (first) {
				SystemClock.sleep(CHECK_DELAY);
				first = false;
				if (logCheck) {
					Log.v(TAG, "check lastCheck: delay over");
				}
			}
			long cacheLastSave = localeCacheManager.getLastSave();
			if (logCheck) {
				Log.v(TAG, "check lastCheck: " + lastCheck + " lastSave: " + cacheLastSave);
			}
			if (lastCheck != cacheLastSave) {
				lastCheck = cacheLastSave;
				// ͼ��
				try {
					if (logCheck) {
						Log.v(TAG, "begin check local icon cache");
					}
					localeCacheManager.checkLocalIconCache();
				} catch (Exception e) {
					e.printStackTrace();
				}

				// ���ͼƬ
				try {
					if (logCheck) {
						Log.v(TAG, "begin check local advertise icon cache");
					}
					localeCacheManager.checkLocalAdvertiseIconCache();
				} catch (Exception e) {
					e.printStackTrace();
				}

				// ��ͼ
				try {
					if (logCheck) {
						Log.v(TAG, "begin check local shot cache");
					}
					localeCacheManager.checkLocalShotCache();
				} catch (Exception e) {
					e.printStackTrace();
				}

				// ���ͼ��
				try {
					if (logCheck) {
						Log.v(TAG, "begin check local category cache");
					}
					localeCacheManager.checkLocalCategoryIconCache();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			// �����ʱ
			SystemClock.sleep(CHECK_PERIOD);
		}
	}
}
