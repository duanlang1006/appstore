package com.applite.homepage;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.Constant;
import com.applite.common.DefaultValue;
import com.applite.common.LogUtils;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.applite.utils.DataCleanManager;
import com.mit.impl.ImplConfig;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import java.io.File;

import kankan.wheel.widget.WheelView;

/**
 * Created by wanghaochen on 15-9-1.
 */
public class SettingFragment extends OSGIBaseFragment implements View.OnClickListener, DataCleanDialog.CallBackInterface {
    private final String TAG = "SettingsPreference";

    private ActionBar actionBar;
    private LayoutInflater mInflater;
    private ViewGroup rootView;

    private WheelView mWheelView;

    private LinearLayout clean_cache;       //清除缓存
//    private LinearLayout download_path;       //下载存储路径
//    private LinearLayout download_thread;       //最大下载线程数
    private LinearLayout download_size;       //最大下载线程数

    private ImageView smart_show;           //智能无图
    private ImageView smart_download;       //零流量下载
    private ImageView update_notification;  //更新提醒
    private ImageView delete_apk;           //删除安装包

    private ImageView thread_btn1;
    private ImageView thread_btn2;
    private ImageView thread_btn3;

    private TextView save_path;
    private TextView cache_size;
    private String path;
    private String size;

    private DataCleanDialog mDataCleanDialog;

    public SettingFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        LogUtils.d(TAG, "onAttach ");
        super.onAttach(activity);
        mInflater = LayoutInflater.from(mActivity);
        mDataCleanDialog = new DataCleanDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = (ViewGroup) mInflater.inflate(R.layout.fragment_setting, container, false);

        mDataCleanDialog.CallBack(this);

        smart_show = (ImageView) rootView.findViewById(R.id.smart_show);                    //智能无图
        smart_download = (ImageView) rootView.findViewById(R.id.smart_download);            //零流量下载
        update_notification = (ImageView) rootView.findViewById(R.id.update_notification);  //更新提醒
        delete_apk = (ImageView) rootView.findViewById(R.id.delete_apk);                    //删除安装包
        clean_cache = (LinearLayout) rootView.findViewById(R.id.clean_cache);               //清除缓存
        download_size = (LinearLayout) rootView.findViewById(R.id.download_size);               //清除缓存

        save_path = (TextView) rootView.findViewById(R.id.save_path);
        cache_size = (TextView) rootView.findViewById(R.id.cache_size);

        thread_btn1 = (ImageView) rootView.findViewById(R.id.thread_btn1);
        thread_btn2 = (ImageView) rootView.findViewById(R.id.thread_btn2);
        thread_btn3 = (ImageView) rootView.findViewById(R.id.thread_btn3);

        smart_show.setOnClickListener(this);
        smart_download.setOnClickListener(this);
        update_notification.setOnClickListener(this);
        delete_apk.setOnClickListener(this);
        clean_cache.setOnClickListener(this);
        download_size.setOnClickListener(this);

        thread_btn1.setOnClickListener(this);
        thread_btn2.setOnClickListener(this);
        thread_btn3.setOnClickListener(this);

        rootView.findViewById(R.id.feedback).setOnClickListener(this);//意见反馈
        rootView.findViewById(R.id.about).setOnClickListener(this);//关于

        initActionBar();
        getDownloadThreadNum();
        setSavePath();
        setCacheSize();

        setAllState();
        return rootView;
    }

    private void getDownloadThreadNum() {
        int i = ImplConfig.getDownloadThreadNum(mActivity);
        if (i == 1) {
            thread_btn1.setImageResource(R.drawable.setting_button1_selected);
            thread_btn2.setImageResource(R.drawable.setting_button2_unselected);
            thread_btn3.setImageResource(R.drawable.setting_button3_unselected);
        } else if (i == 2) {
            thread_btn1.setImageResource(R.drawable.setting_button1_unselected);
            thread_btn2.setImageResource(R.drawable.setting_button2_selected);
            thread_btn3.setBackgroundResource(R.drawable.setting_button3_unselected);
        } else if (i == 3) {
            thread_btn1.setImageResource(R.drawable.setting_button1_unselected);
            thread_btn2.setImageResource(R.drawable.setting_button2_unselected);
            thread_btn3.setImageResource(R.drawable.setting_button3_selected);
        }
    }

    private void setAllState() {
        update_notification.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.UPDATE_REMIND, DefaultValue.defauleValueUpdateRemind));
        delete_apk.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.DELETE_PACKAGE, DefaultValue.defaultValueDeletePackage));
        smart_show.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.NO_PICTURE, DefaultValue.defaultValueNoPic));
        smart_download.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.WIFI_UPDATE_SWITCH, DefaultValue.defaultValueWIFIUpdateSwitch));
    }

    private void setSavePath() {
        if (null != save_path) {
            path = Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath;
            save_path.setText(path);
        }
    }

    private void setCacheSize() {
        try {
            size = DataCleanManager.getTotalCacheSize(mActivity);
            String num = size.substring(0, size.length() - 2);
            Double numtrans = Double.valueOf(num);
            String unit = size.substring(size.length() - 2, size.length());

            if ((numtrans < 60.00) && (unit.equals("KB"))) {
                size = "0.00KB";
            }
            if (null != cache_size) {
                cache_size.setText(size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refreshCacheSize() {
        String showsize;
        try {
            showsize = DataCleanManager.getTotalCacheSize(mActivity);
            String num = showsize.substring(0, showsize.length() - 2);
            Double numtrans = Double.valueOf(num);
            String unit = showsize.substring(showsize.length() - 2, showsize.length());
            if (null != cache_size) {
                if ((numtrans < 60.00) && (unit.equals("KB"))) {
                    cache_size.setText("0.00KB");
                } else {
                    cache_size.setText(showsize);
                }
            }
            Toast.makeText(mActivity, "释放了" + size + "空间", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        if (R.id.update_notification == v.getId()) {    //更新提醒
            AppliteSPUtils.put(mActivity, AppliteSPUtils.UPDATE_REMIND,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.UPDATE_REMIND, DefaultValue.defauleValueUpdateRemind));
            setAllState();
        } else if (R.id.clean_cache == v.getId()) {     //清除缓存
            setAllState();
            mDataCleanDialog.show(mActivity);
            setCacheSize();
        } else if (R.id.delete_apk == v.getId()) {      //删除安装包
            AppliteSPUtils.put(mActivity, AppliteSPUtils.DELETE_PACKAGE,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.DELETE_PACKAGE, DefaultValue.defaultValueDeletePackage));
            setAllState();
        } else if (R.id.smart_show == v.getId()) {      //智能无图
            AppliteSPUtils.put(mActivity, AppliteSPUtils.NO_PICTURE,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.NO_PICTURE, DefaultValue.defaultValueNoPic));
            setAllState();
        } else if (R.id.smart_download == v.getId()) {  //零流量下载
            AppliteSPUtils.put(mActivity, AppliteSPUtils.WIFI_UPDATE_SWITCH,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.WIFI_UPDATE_SWITCH, DefaultValue.defaultValueWIFIUpdateSwitch));
            setAllState();
        } else if (R.id.feedback == v.getId()) {        //意见反馈
            FeedbackDialog.show(mActivity);
        } else if (R.id.about == v.getId()) {           //关于
            ((OSGIServiceHost) mActivity).jumptoAbout(true);
        } else if (R.id.download_size == v.getId()) {           //数据网络下载最大限制
            ((OSGIServiceHost) mActivity).jumptoDownloadSizeLimit(true);
        }else if (R.id.thread_btn1 == v.getId()) {
            LogUtils.d(TAG, "thread_btn1 ");
            ImplConfig.setDownloadThreadNum(mActivity, 1);
            thread_btn1.setImageResource(R.drawable.setting_button1_selected);
            thread_btn2.setImageResource(R.drawable.setting_button2_unselected);
            thread_btn3.setImageResource(R.drawable.setting_button3_unselected);
        } else if (R.id.thread_btn2 == v.getId()) {
            LogUtils.d(TAG, "thread_btn2 ");
            ImplConfig.setDownloadThreadNum(mActivity, 2);
            thread_btn1.setImageResource(R.drawable.setting_button1_unselected);
            thread_btn2.setImageResource(R.drawable.setting_button2_selected);
            thread_btn3.setImageResource(R.drawable.setting_button3_unselected);
        } else if (R.id.thread_btn3 == v.getId()) {
            LogUtils.d(TAG, "thread_btn3 ");
            ImplConfig.setDownloadThreadNum(mActivity, 3);
            thread_btn1.setImageResource(R.drawable.setting_button1_unselected);
            thread_btn2.setImageResource(R.drawable.setting_button2_unselected);
            thread_btn3.setImageResource(R.drawable.setting_button3_selected);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            initActionBar();
        }
    }

    private void initActionBar() {
        if (null == actionBar) {
            actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mActivity.getResources().getString(R.string.setting));
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.show();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

}

