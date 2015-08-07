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
    LayoutInflater mInflater;
    Resources mPlugResource;

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
        mPlugResource = getActivity().getResources();
        mInflater = inflater;
        try {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != context) {
                mInflater = LayoutInflater.from(context);
                mInflater = mInflater.cloneInContext(context);
                mPlugResource = context.getResources();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        View view = mInflater.inflate(R.layout.fragment_personal, container, false);
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
        try {
            Context plugContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            Field field = inflater.getClass().getDeclaredField("mContext");
            field.setAccessible(true);
            Object orgContext = field.get(inflater);
            field.set(inflater,plugContext);
            inflater.inflate(R.menu.menu_main,menu);
            field.set(inflater,orgContext);

            field = menu.getClass().getDeclaredField("mContext");
            field.setAccessible(true);
            field.set(menu,plugContext);

            field = menu.getClass().getDeclaredField("mResources");
            field.setAccessible(true);
            field.set(menu,plugContext.getResources());

            if (menu instanceof SupportMenu){
                SupportMenuItem item = (SupportMenuItem)menu.findItem(R.id.action_search);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
        switch (item.getItemId()){
            case R.id.action_search:
                HomepageUtils.launchSearchFragment((OSGIServiceHost)getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.action_dm:
                HomepageUtils.launchDownloadManagerFragment(((OSGIServiceHost)getActivity()));
                break;
            case R.id.action_upgrade:
                HomepageUtils.launchUpgradeFragment(((OSGIServiceHost)getActivity()));
                break;
        }
    }


    private void initActionBar(){
        try {
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(mPlugResource.getDrawable(R.drawable.action_bar_back_light));
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mPlugResource.getString(R.string.personal));
            actionBar.setDisplayShowCustomEnabled(false);
//            ViewGroup customView = (ViewGroup)mInflater.inflate(R.layout.actionbar_personal,null);
//            actionBar.setCustomView(customView);
            actionBar.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
