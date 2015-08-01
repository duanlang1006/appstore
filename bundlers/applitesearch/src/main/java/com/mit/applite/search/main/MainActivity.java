package com.mit.applite.search.main;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.mit.applite.search.R;

public class MainActivity extends FragmentActivity {

    private FragmentManager fm;
    private FragmentTransaction transaction;
    private SearchFragment mSearchFragment;

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
//        fm = getSupportFragmentManager();
//        transaction = fm.beginTransaction();
//        mSearchFragment = new SearchFragment();
//        transaction.replace(R.id.search_content, mSearchFragment);
//        transaction.commit();
    }

}
