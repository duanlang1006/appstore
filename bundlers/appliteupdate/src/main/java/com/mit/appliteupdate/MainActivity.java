package com.mit.appliteupdate;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;


public class MainActivity extends FragmentActivity {

    private FragmentManager fm;
    private FragmentTransaction transaction;
    private UpdateFragment mUpdateFragment;

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
        mUpdateFragment = new UpdateFragment();
        transaction.replace(R.id.update_content, mUpdateFragment);
        transaction.commit();
    }

}
