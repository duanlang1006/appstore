package com.applite.homepage;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.applite.common.DefaultValue;
import com.applite.common.LogUtils;
import com.applite.sharedpreferences.AppliteSPUtils;

/**
 * Created by android153 on 9/23/15.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener, DataCleanDialog.CallBackInterface {
    private final String TAG = "SettingsPreference";

    private android.app.ActionBar actionBar;

    private SwitchPreference smart_update;
    private SwitchPreference smart_show;
    private SwitchPreference push_notification;
    private SwitchPreference delete_apkfile;
    private Preference apk_savepath;
    private Preference download_thread;
    private Preference clean_cache;
    private Preference feedback;
    private Preference about;

    private DataCleanDialog mDataCleanDialog;

    //省流量设置
    private static final String KEY_SMART_UPDATE = "smart_update";    //零流量更新
    private static final String kEY_SMART_SHOW = "smart_show";        //智能无图

    //应用设置
    private static final String KEY_PUSH_NOTIFICATION = "push_notification";    //允许推送更新提示
    private static final String kEY_DELETE_APKFILE = "delete_apkfile";        //安装后自动删除安装包
    private static final String KEY_APK_SAVEPATH = "apk_savepath";    //零流量更新
    private static final String kEY_DOWNLOAD_THREAD = "download_thread";        //智能无图
    private static final String KEY_CLEAN_CACHE = "clean_cache";    //清除缓存

    //帮助
    private static final String KEY_ONEKEY_FEEDBACK = "feedback";    //一键反馈
    private static final String KEY_ABOUT = "about";    //关于

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate");
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);

        mDataCleanDialog = new DataCleanDialog();
        mDataCleanDialog.CallBack(this);
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.setting_title);

        addPreferencesFromResource(R.xml.settings);
        initSmartPreference();
        initAppPreference();
        initHelpPreference();
//        initActionBar();
    }

    private void initSmartPreference() {
        //零流量更新
        smart_update = (SwitchPreference) findPreference(KEY_SMART_UPDATE);
        if (null != smart_update) {
            smart_update.setOnPreferenceChangeListener(this);
            boolean select = (boolean) AppliteSPUtils.get(this, AppliteSPUtils.WIFI_UPDATE_SWITCH, DefaultValue.defaultValueWIFIUpdateSwitch);
            smart_update.setChecked(select);
        }

        //智能无图
        smart_show = (SwitchPreference) findPreference(kEY_SMART_SHOW);
        if (null != smart_show) {
            smart_show.setOnPreferenceChangeListener(this);
            boolean select = (boolean) AppliteSPUtils.get(this, AppliteSPUtils.NO_PICTURE, DefaultValue.defaultValueNoPic);
            smart_show.setChecked(select);
        }

    }

    private void initAppPreference() {

        //允许推送更新提示
        push_notification = (SwitchPreference) findPreference(KEY_PUSH_NOTIFICATION);
        if (null != push_notification) {
            push_notification.setOnPreferenceChangeListener(this);
            boolean select = (boolean) AppliteSPUtils.get(this, AppliteSPUtils.UPDATE_REMIND, DefaultValue.defauleValueUpdateRemind);
            push_notification.setChecked(select);
        }

        //安装后自动删除安装包
        delete_apkfile = (SwitchPreference) findPreference(kEY_DELETE_APKFILE);
        if (null != delete_apkfile) {
            delete_apkfile.setOnPreferenceChangeListener(this);
            boolean select = (boolean) AppliteSPUtils.get(this, AppliteSPUtils.DELETE_PACKAGE, DefaultValue.defaultValueDeletePackage);
            delete_apkfile.setChecked(select);
        }

        //下载存储路径
        apk_savepath = (Preference) findPreference(KEY_APK_SAVEPATH);
        if (null != apk_savepath) {
            apk_savepath.setOnPreferenceClickListener(this);
        }

        //最多同时下载任务
        download_thread = (Preference) findPreference(kEY_DOWNLOAD_THREAD);
        if (null != download_thread) {
            download_thread.setOnPreferenceClickListener(this);
        }

        //清除缓存
        clean_cache = (Preference) findPreference(KEY_CLEAN_CACHE);
        if (null != clean_cache) {
            clean_cache.setOnPreferenceClickListener(this);
        }
    }

    private void initHelpPreference() {
        //一键反馈
        feedback = (Preference) findPreference(KEY_ONEKEY_FEEDBACK);
        if (null != feedback) {
            feedback.setOnPreferenceClickListener(this);
        }

        //关于
        about = (Preference) findPreference(KEY_ABOUT);
        if (null != about) {
            about.setOnPreferenceClickListener(this);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Called when a Preference has been changed by the user. This is
     * called before the state of the Preference is about to be updated and
     * before the state is persisted.
     *
     * @param preference The changed Preference.
     * @param newValue   The new value of the Preference.
     * @return True to update the state of the Preference with the new value.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean check = (Boolean) newValue;
        if (preference.getKey().equals(KEY_SMART_UPDATE)) {
            LogUtils.d(TAG, "preference.getKey() = KEY_SMART_UPDATE");
            AppliteSPUtils.put(this, AppliteSPUtils.WIFI_UPDATE_SWITCH, check);
            smart_update.setChecked(check);
        } else if (preference.getKey().equals(kEY_SMART_SHOW)) {
            LogUtils.d(TAG, "preference.getKey() = kEY_SMART_SHOW");
            AppliteSPUtils.put(this, AppliteSPUtils.NO_PICTURE, check);
            smart_show.setChecked(check);
        } else if (preference.getKey().equals(KEY_PUSH_NOTIFICATION)) {
            LogUtils.d(TAG, "preference.getKey() = KEY_PUSH_NOTIFICATION");
            AppliteSPUtils.put(this, AppliteSPUtils.UPDATE_REMIND, check);
            push_notification.setChecked(check);
        } else if (preference.getKey().equals(kEY_DELETE_APKFILE)) {
            LogUtils.d(TAG, "preference.getKey() = kEY_DELETE_APKFILE");
            AppliteSPUtils.put(this, AppliteSPUtils.DELETE_PACKAGE, check);
            delete_apkfile.setChecked(check);
        }

        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        //TODO

        return false;
    }

    /**
     * Called when a Preference has been clicked.
     *
     * @param preference The Preference that was clicked.
     * @return True if the click was handled.
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(KEY_APK_SAVEPATH)) {
            LogUtils.d(TAG, "preference.getKey() = KEY_APK_SAVEPATH");
        } else if (preference.getKey().equals(kEY_DOWNLOAD_THREAD)) {
            LogUtils.d(TAG, "preference.getKey() = kEY_DOWNLOAD_THREAD");
        } else if (preference.getKey().equals(KEY_CLEAN_CACHE)) {
            LogUtils.d(TAG, "preference.getKey() = KEY_CLEAN_CACHE");
            mDataCleanDialog.show(this);
        } else if (preference.getKey().equals(KEY_ONEKEY_FEEDBACK)) {
            LogUtils.d(TAG, "preference.getKey() = KEY_ONEKEY_FEEDBACK");
            FeedbackDialog.show(this);
        } else if (preference.getKey().equals(KEY_ABOUT)) {
            LogUtils.d(TAG, "preference.getKey() = KEY_ABOUT");
//            ((OSGIServiceHost) mActivity).jumptoAbout(true);
        }

        return false;
    }

    @Override
    public void refreshCacheSize() {

    }
}
