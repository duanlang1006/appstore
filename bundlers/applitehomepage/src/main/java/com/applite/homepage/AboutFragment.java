package com.applite.homepage;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.osgi.extra.OSGIBaseFragment;

import kankan.wheel.widget.WheelView;

/**
 * Created by wanghaochen on 15-9-6.
 */
public class AboutFragment extends OSGIBaseFragment {

    private LayoutInflater mInflater;
    private ViewGroup rootView;

    private ActionBar actionBar;
//    private WheelView wheelView;
    private PackageInfo info;
    private TextView tv_app_version;
    private float scale = 0.75f;
    private int distance = 25;

    public AboutFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mInflater = LayoutInflater.from(mActivity);
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        rootView = (ViewGroup) mInflater.inflate(R.layout.fragment_about, container, false);


//        wheelView = (WheelView) rootView.findViewById(R.id.aa);
//        String ss[] = mActivity.getResources().getStringArray(R.array.team);
//        wheelView.setViewAdapter(new ArrayWheelAdapter<>(this.getActivity(), ss));
//        wheelView.setCyclic(true);
//        wheelView.setScaleX(scale);
//        wheelView.setScaleY(scale);
//        wheelView.setMinimumHeight(distance);
//        wheelView.setVisibility(View.GONE);

        tv_app_version = (TextView) rootView.findViewById(R.id.app_version_code);
        tv_app_version.setText(info.versionName);

        initActionBar();

        return rootView;
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
        if(null == actionBar){
            actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mActivity.getResources().getString(R.string.about));
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