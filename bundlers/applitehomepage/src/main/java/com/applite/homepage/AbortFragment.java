package com.applite.homepage;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.osgi.extra.OSGIBaseFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wanghaochen on 15-9-6.
 */
public class AbortFragment extends OSGIBaseFragment {
    private Activity mActivity;
    private View view = null;
    private LayoutInflater mInflater;
    private ActionBar actionBar;
    private WheelView wheelView;
    private PackageInfo info;
    private TextView tv_app_version;


    public AbortFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        mActivity = activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            info = mActivity.getPackageManager().getPackageInfo(
                    mActivity.getPackageName(), 0);
        } catch (Exception e) {

        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
        initActionBar();
        view = inflater.inflate(R.layout.fragment_abort, container, false);
        mInflater = LayoutInflater.from(mActivity);

        wheelView = (WheelView) view.findViewById(R.id.aa);
        String ss[] = mActivity.getResources().getStringArray(R.array.team);
        wheelView.setViewAdapter(new ArrayWheelAdapter<>(this.getActivity(), ss));
        wheelView.setCyclic(true);


        tv_app_version = (TextView) view.findViewById(R.id.app_version_code);
        tv_app_version.setText(info.versionName);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        initActionBar();
    }

    private void initActionBar() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mActivity.getResources().getString(R.string.setting));
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.show();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

}