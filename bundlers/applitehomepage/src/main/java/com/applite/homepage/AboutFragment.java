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
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
 * Created by wanghaochen on 15-9-6.
 */
public class AboutFragment extends OSGIBaseFragment {
    private Activity mActivity;
    private View view = null;
    private LayoutInflater mInflater;
    private ActionBar actionBar;
    private WheelView wheelView;
    private PackageInfo info;
    private TextView tv_app_version;
    private float scale = 0.75f;
    private int distance = 25;

    public AboutFragment() {
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
        initActionBar();
        view = inflater.inflate(R.layout.fragment_about, container, false);
        mInflater = LayoutInflater.from(mActivity);

        wheelView = (WheelView) view.findViewById(R.id.aa);
        String ss[] = mActivity.getResources().getStringArray(R.array.team);
        wheelView.setViewAdapter(new ArrayWheelAdapter<>(this.getActivity(), ss));
        wheelView.setCyclic(true);
        wheelView.setScaleX(scale);
        wheelView.setScaleY(scale);
        wheelView.setMinimumHeight(distance);

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