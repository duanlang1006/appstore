/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kapps.market;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.kapps.market.log.LogUtil;

/**
 */
public final class DownloadService extends Service {
	public static final String TAG = "DownloadService";

	private ArrayList<IStatePart> statePartList = new ArrayList<IStatePart>();

	@Override
	public void onCreate() {
		MApplication mApplication = ((MApplication) getApplication());
		statePartList.add(new DownloadStatePart(mApplication));
		LogUtil.d(TAG, "------------------AppStateService create over");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (intent == null || intent.getAction() == null) {
			return;
		}

		LogUtil.d(TAG, "------------------intent.getAction(): " + intent.getAction());

		// ѭ������
		for (IStatePart statePart : statePartList) {
			try {
				statePart.handleServiceRequest(intent, startId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return;
	}

	@Override
	public void onDestroy() {
		// ѭ������
		for (IStatePart statePart : statePartList) {
			try {
				statePart.handleServiceExit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
