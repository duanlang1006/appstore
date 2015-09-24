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
import com.applite.sharedpreferences.AppliteSPUtils;
import com.applite.utils.DataCleanManager;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import java.io.File;

/**
 * Created by wanghaochen on 15-9-1.
 */
public class SettingFragment extends OSGIBaseFragment implements View.OnClickListener, DataCleanDialog.CallBackInterface {
    private final String TAG = "SettingsPreference";

    private Activity mActivity;
    private ActionBar actionBar;

    private LinearLayout clean_cache;       //清除缓存
    private LinearLayout download_path;       //清除缓存

    private ImageView smart_show;           //智能无图
    private ImageView smart_download;       //零流量下载
    private ImageView update_notification;  //更新提醒
    private ImageView delete_apk;           //删除安装包

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
        mActivity = activity;
        super.onAttach(activity);
        mDataCleanDialog = new DataCleanDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        initActionBar();

        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        mDataCleanDialog.CallBack(this);

        smart_show = (ImageView) view.findViewById(R.id.smart_show);                    //智能无图
        smart_download = (ImageView) view.findViewById(R.id.smart_download);            //零流量下载
        update_notification = (ImageView) view.findViewById(R.id.update_notification);  //更新提醒
        delete_apk = (ImageView) view.findViewById(R.id.delete_apk);                    //删除安装包
        clean_cache = (LinearLayout) view.findViewById(R.id.clean_cache);               //清除缓存

        download_path = (LinearLayout) view.findViewById(R.id.download_path);
        save_path = (TextView) view.findViewById(R.id.save_path);
        cache_size = (TextView) view.findViewById(R.id.cache_size);

        smart_show.setOnClickListener(this);
        smart_download.setOnClickListener(this);
        update_notification.setOnClickListener(this);
        delete_apk.setOnClickListener(this);
        clean_cache.setOnClickListener(this);

        view.findViewById(R.id.feedback).setOnClickListener(this);//意见反馈
        view.findViewById(R.id.about).setOnClickListener(this);//关于

        setSavePath();
        setCacheSize();

        setAllState();
        return view;
    }

    private void setAllState() {
        update_notification.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.UPDATE_REMIND, DefaultValue.defaultBoolean));
        clean_cache.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.CLEAR_CACHE, DefaultValue.defaultBoolean));
        delete_apk.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.DELETE_PACKAGE, DefaultValue.defaultBoolean));
        smart_show.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.NO_PICTURE, DefaultValue.defaultBoolean));
        smart_download.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.WIFI_UPDATE_SWITCH, DefaultValue.defaultBoolean));
    }

    private void setSavePath() {
        if (null != save_path) {
            path = Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath;
            save_path.setText(path);
        }
    }

    private void setCacheSize() {
        String showsize1;
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
                }else{
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

