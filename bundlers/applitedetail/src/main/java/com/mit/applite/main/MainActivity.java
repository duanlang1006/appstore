package com.mit.applite.main;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;


public class MainActivity extends FragmentActivity {

    private FragmentManager fm;
    private FragmentTransaction transaction;
    private DetailFragment mDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDefaultFragment();
    }

    /**
     * 设置默认的Fragment
     */
    private void setDefaultFragment() {
        fm = getSupportFragmentManager();
        transaction = fm.beginTransaction();
        mDetailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", "只有一关");
        mDetailFragment.setArguments(bundle);
        transaction.replace(R.id.detail_main, mDetailFragment);
        transaction.commit();
    }

}
