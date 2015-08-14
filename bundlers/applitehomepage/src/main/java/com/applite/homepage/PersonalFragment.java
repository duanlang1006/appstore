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
import com.applite.utils.HomepageUtils;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;


public class PersonalFragment extends OSGIBaseFragment implements View.OnClickListener{
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
        initActionBar();
        return view;
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
            HomepageUtils.launchSearchFragment((OSGIServiceHost)mActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if (R.id.action_dm == v.getId()) {
            HomepageUtils.launchDownloadManagerFragment(((OSGIServiceHost) mActivity));
        }else if (R.id.action_upgrade == v.getId()){
            HomepageUtils.launchUpgradeFragment(((OSGIServiceHost)mActivity));
        }else if (R.id.action_logo == v.getId()){
            HomepageUtils.launchLogoManagerFragment(((OSGIServiceHost)mActivity));
        }
    }


    private void initActionBar(){
        try {
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mActivity.getResources().getString(R.string.personal));
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
