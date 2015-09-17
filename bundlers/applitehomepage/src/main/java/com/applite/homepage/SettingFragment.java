package com.applite.homepage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.applite.common.DefaultValue;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.applite.utils.DataCleanManager;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import java.io.File;

/**
 * Created by wanghaochen on 15-9-1.
 */
public class SettingFragment extends OSGIBaseFragment implements View.OnClickListener {
    private Activity mActivity;
    private LinearLayout ll1_1;//更新提醒
    private LinearLayout ll2_1;//清除缓存
    private LinearLayout ll2_2;//删除安装包
    private LinearLayout ll2_3;//智能无图
    private LinearLayout ll2_4;//零流量下载
    private ActionBar actionBar;

    public SettingFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        mActivity = activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
        initActionBar();

        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ll1_1 = (LinearLayout) view.findViewById(R.id.ll_item1_1);//更新提醒
        ll1_1.setOnClickListener(this);
        ll2_1 = (LinearLayout) view.findViewById(R.id.ll_item2_1);//清除缓存
        ll2_1.setOnClickListener(this);
        ll2_2 = (LinearLayout) view.findViewById(R.id.ll_item2_2);//删除安装包
        ll2_2.setOnClickListener(this);
        ll2_3 = (LinearLayout) view.findViewById(R.id.ll_item2_3);//智能无图
        ll2_3.setOnClickListener(this);
        ll2_4 = (LinearLayout) view.findViewById(R.id.ll_item2_4);//零流量下载
        ll2_4.setOnClickListener(this);
        view.findViewById(R.id.ll_item3_1).setOnClickListener(this);//关于
        view.findViewById(R.id.ll_item3_2).setOnClickListener(this);//意见反馈
        setAllState();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setAllState() {
        ll1_1.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.UPDATE_REMIND, DefaultValue.defaultBoolean));
        ll2_1.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.CLEAR_CACHE, DefaultValue.defaultBoolean));
        ll2_2.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.DELETE_PACKAGE, DefaultValue.defaultBoolean));
        ll2_3.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.NO_PICTURE, DefaultValue.defaultBoolean));
        ll2_4.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.WIFI_UPDATE_SWITCH, DefaultValue.defaultBoolean));
    }

    @Override
    public void onClick(View v) {
        if (R.id.ll_item1_1 == v.getId()) {//更新提醒
            AppliteSPUtils.put(mActivity, AppliteSPUtils.UPDATE_REMIND,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.UPDATE_REMIND, DefaultValue.defauleValueUpdateRemind));
            setAllState();
        } else if (R.id.ll_item2_1 == v.getId()) {//清除缓存
            AppliteSPUtils.put(mActivity, AppliteSPUtils.CLEAR_CACHE,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.CLEAR_CACHE, DefaultValue.defaultBoolean));
            setAllState();
            DataCleanDialog.show(mActivity);
        } else if (R.id.ll_item2_2 == v.getId()) {//删除安装包
            AppliteSPUtils.put(mActivity, AppliteSPUtils.DELETE_PACKAGE,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.DELETE_PACKAGE, DefaultValue.defaultValueDeletePackage));
            setAllState();
        } else if (R.id.ll_item2_3 == v.getId()) {//智能无图
            AppliteSPUtils.put(mActivity, AppliteSPUtils.NO_PICTURE,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.NO_PICTURE, DefaultValue.defaultValueNoPic));
            setAllState();
        } else if (R.id.ll_item2_4 == v.getId()) {//零流量下载
            AppliteSPUtils.put(mActivity, AppliteSPUtils.WIFI_UPDATE_SWITCH,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.WIFI_UPDATE_SWITCH, DefaultValue.defaultValueWIFIUpdateSwitch));
            setAllState();
        } else if (R.id.ll_item3_1 == v.getId()) {//意见反馈
            FeedbackDialog.show(mActivity);
//            Toast.makeText(mActivity, "意见反馈", Toast.LENGTH_LONG).show();
//            ((OSGIServiceHost) getActivity()).jumptoConversation();
//            FeedbackAgent agent = new FeedbackAgent(mActivity);
//            agent.startFeedbackActivity();
        } else if (R.id.ll_item3_2 == v.getId()) {//关于
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

//    private static long getEnvironmentSize() {
//        File localFile = Environment.getDataDirectory();
//        long l1;
//        if (null == localFile) {
//            l1 = 0L;
//        }
//        while (true) {
//            String str = localFile.getPath();
//            StatFs localStatFs = new StatFs(str);
//            long l2 = localStatFs.getBlockSize();
//            l1 = localStatFs.getBlockCount() * l2;
//            return l1;
//        }
//    }
//
//    private void getAllMemory() throws Exception{
//        PackageManager pm = getActivity().getPackageManager();
//        Class[] arrayOfClass = new Class[2];
//        Class localClass2 = Long.TYPE;
//        arrayOfClass[0] = localClass2;
//        arrayOfClass[1] = IPackageDataObserver.class;
//    }

    /**
     * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理
     * <p/>
     * * @param directory
     */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
}

