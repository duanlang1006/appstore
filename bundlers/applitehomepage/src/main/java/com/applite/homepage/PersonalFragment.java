package com.applite.homepage;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.applite.sharedpreferences.AppliteSPUtils;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

public class PersonalFragment extends OSGIBaseFragment implements View.OnClickListener {
    private Activity mActivity;
    private ImageView mSetWifiUpdateView;

    public PersonalFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal, container, false);
        view.findViewById(R.id.action_upgrade).setOnClickListener(this);
        view.findViewById(R.id.action_dm).setOnClickListener(this);
        view.findViewById(R.id.action_logo).setOnClickListener(this);
        view.findViewById(R.id.action_setting).setOnClickListener(this);
        view.findViewById(R.id.action_lucky).setOnClickListener(this);
//        mSetWifiUpdateView = (ImageView) view.findViewById(R.id.zero_flow_update);
//        mSetWifiUpdateView.setOnClickListener(this);
//        setWifiUpdateViewState();
        initActionBar();
        return view;
    }

//    private void setWifiUpdateViewState() {
//        if ((boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.WIFI_UPDATE_SWITCH, true)) {
//            mSetWifiUpdateView.setSelected(true);
//        } else {
//            mSetWifiUpdateView.setSelected(false);
//        }
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onResume() {
        super.onResume();
//
//        getView().setFocusableInTouchMode(true);
//        getView().requestFocus();
//        getView().setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
//                    // handle back button
//                    getFragmentManager().popBackStackImmediate();
//                    return true;
//                }
//                return false;
//            }
//        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_main_homepage, menu);
//        MenuItem item = menu.findItem(R.id.action_search);
//        if (null != item) {
//            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            initActionBar();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_search == item.getItemId()) {
            ((OSGIServiceHost) mActivity).jumptoSearch(null, true, null, null, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (R.id.action_dm == v.getId()) {
            ((OSGIServiceHost) mActivity).jumptoDownloadManager(true);
        } else if (R.id.action_upgrade == v.getId()) {
            ((OSGIServiceHost) mActivity).jumptoUpdate(true);
        } else if (R.id.action_logo == v.getId()) {
            ((OSGIServiceHost) mActivity).jumptoMylife(true);
        }
//        else if (R.id.zero_flow_update == v.getId()) {
//            AppliteSPUtils.put(mActivity, AppliteSPUtils.WIFI_UPDATE_SWITCH,
//                    !(boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.WIFI_UPDATE_SWITCH, true));
//            setWifiUpdateViewState();
//        }
        else if (R.id.action_setting == v.getId()) {
            ((OSGIServiceHost) mActivity).jumptoSetting(true);
        } else if (R.id.action_lucky == v.getId()) {
            ((OSGIServiceHost) mActivity).jumptoLucky(true);
        }
    }

    private void initActionBar() {
        try {
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mActivity.getResources().getString(R.string.personal));
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
