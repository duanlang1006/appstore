package com.applite.homepage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.applite.common.Constant;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

/**
 * Created by wanghaochen on 15-9-1.
 */
public class SettingFragment extends OSGIBaseFragment implements View.OnClickListener {
    private Activity mActivity;
    private LinearLayout ll1_1;//更新提醒
    private LinearLayout ll2_1;//清除缓存
    private LinearLayout ll2_2;//删除安装包
    private LinearLayout ll2_3;//智能无图
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
        ll1_1.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.UPDATE_REMIND, true));
        ll2_1.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.CLEAR_CACHE, true));
        ll2_2.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.DELETE_PACKAGE, true));
        ll2_3.setSelected((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.NO_PICTURE, true));
    }

    @Override
    public void onClick(View v) {
        if (R.id.ll_item1_1 == v.getId()) {//更新提醒
            AppliteSPUtils.put(mActivity, AppliteSPUtils.UPDATE_REMIND,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.UPDATE_REMIND, true));
            setAllState();
        } else if (R.id.ll_item2_1 == v.getId()) {//清除缓存
            AppliteSPUtils.put(mActivity, AppliteSPUtils.CLEAR_CACHE,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.CLEAR_CACHE, true));
            setAllState();
        } else if (R.id.ll_item2_2 == v.getId()) {//删除安装包
            AppliteSPUtils.put(mActivity, AppliteSPUtils.DELETE_PACKAGE,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.DELETE_PACKAGE, true));
            setAllState();
        } else if (R.id.ll_item2_3 == v.getId()) {//智能无图
            AppliteSPUtils.put(mActivity, AppliteSPUtils.NO_PICTURE,
                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.NO_PICTURE, true));
            setAllState();
        } else if (R.id.ll_item3_1 == v.getId()) {//意见反馈
//            Toast.makeText(mActivity, "意见反馈", Toast.LENGTH_LONG).show();
            ((OSGIServiceHost) getActivity()).jumptoConversation();
//            FeedbackAgent agent = new FeedbackAgent(mActivity);
//            agent.startFeedbackActivity();
//            Intent intent = new Intent(getActivity(), ConversationActivity.class);
//            getActivity().startActivity(intent);
//            LayoutInflater inflater = LayoutInflater.from(mActivity);
//            final View layout = inflater.inflate(R.layout.dialog_feedback, (ViewGroup) v.findViewById(R.id.dialog_feedback));
//            new AlertDialog.Builder(mActivity)
//                    .setTitle("我们很重视您的意见和建议")
//                    .setView(layout).setNegativeButton("取消", null)
//                    .setPositiveButton("提交", null)
//                    .show();
//            final RadioGroup rg1 = (RadioGroup) layout.findViewById(R.id.dialog_feedback_rg1);
//            final RadioGroup rg2 = (RadioGroup) layout.findViewById(R.id.dialog_feedback_rg2);
//            final RadioButton rb1 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb1);
//            final RadioButton rb2 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb2);
//            final RadioButton rb3 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb3);
//            final RadioButton rb4 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb4);
//            final RadioButton rb5 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb5);
//            final RadioButton rb6 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb6);
//            DialogInterface.OnClickListener lis = new DialogInterface.OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//            if ((rb1.isChecked() || rb2.isChecked() || rb3.isChecked()) && rg2.isChecked) {
//                        rg2.clearCheck();
//            } else if ((rb4.isChecked() || rb5.isChecked() || rb6.isChecked()) && rg2.isChecked) {
//                        rg1.clearCheck();
//                    }
//                }
//            };
//            RadioGroup.OnCheckedChangeListener lis = new RadioGroup.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(RadioGroup group, int checkedId) {
//                    if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked()) {
//                        rg2.clearCheck();
//                    } else if (rb4.isChecked() || rb5.isChecked() || rb6.isChecked()) {
//                        rg1.clearCheck();
//                    }
//                    if (R.id.dialog_feedback_rg1 == checkedId) {
//                        rg2.clearCheck();
//                    } else {
//                        rg1.clearCheck();
//                    }
//                }
//            };
//            rg1.setOnCheckedChangeListener(lis);
//            rg2.setOnCheckedChangeListener(lis);
        } else if (R.id.ll_item3_2 == v.getId()) {//关于
            ((OSGIServiceHost) mActivity).jumptoAbort(true);
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

}

