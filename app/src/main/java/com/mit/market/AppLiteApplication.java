package com.mit.market;
/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import java.util.List;

public class AppLiteApplication extends Application {
    private static final String TAG="applite_application";
    private static boolean sIsScreenLarge;
    private static float sScreenDensity;

    @Override
    public void onCreate() {
        super.onCreate();

        // set sIsScreenXLarge and sScreenDensity *before* creating icon cache
        final int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        sIsScreenLarge = screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
            screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
        sScreenDensity = getResources().getDisplayMetrics().density;

        AppliteConfig.initNetwork(this);

        String processName = getProcessName(this, android.os.Process.myPid());
        if (processName != null) {
            boolean defaultProcess = processName.equals(getPackageName());
            if (defaultProcess) {
                Log.d(TAG,"defaultProcess is true");
                initAppForMainProcess();
            } else if (processName.contains(":webbrowser")) {
//                initAppForWebBrowseProcess();
            } else if (processName.contains(":wallet")) {

            }
        }
    }

    /**
     * There's no guarantee that this function is ever called.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void initAppForMainProcess(){

    }

    public static boolean isScreenLarge() {
        return sIsScreenLarge;
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;
    }

    public static float getScreenDensity() {
        return sScreenDensity;
    }


    /**
     * @return null may be returned if the specified process not found
     */
    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }
}
