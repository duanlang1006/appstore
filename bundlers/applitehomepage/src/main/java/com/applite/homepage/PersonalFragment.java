package com.applite.homepage;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.utils.HomepageUtils;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import java.lang.reflect.Field;


public class PersonalFragment extends OSGIBaseFragment implements View.OnClickListener{
    public static OSGIBaseFragment newInstance(Fragment fg,Bundle params){
        return new PersonalFragment(fg,params);
    }

    private PersonalFragment(Fragment mFragment, Bundle params) {
        super(mFragment, params);
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
        initActionBar();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_homepage,menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item){
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            initActionBar();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_search == item.getItemId()){
            HomepageUtils.launchSearchFragment((OSGIServiceHost)getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if (R.id.action_dm == v.getId()) {
            HomepageUtils.launchDownloadManagerFragment(((OSGIServiceHost) getActivity()));
        }else if (R.id.action_upgrade == v.getId()){
            HomepageUtils.launchUpgradeFragment(((OSGIServiceHost)getActivity()));
        }
    }


    private void initActionBar(){
        try {
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(getActivity().getResources().getString(R.string.personal));
            actionBar.setDisplayShowCustomEnabled(false);
//            ViewGroup customView = (ViewGroup)mInflater.inflate(R.layout.actionbar_personal,null);
//            actionBar.setCustomView(customView);
            actionBar.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
