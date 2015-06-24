package com.applite.homepage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.applite.utils.HomePageUtils;

public class HomePageActivity extends FragmentActivity {
    private FragmentManager fm;
    private FragmentTransaction ftx;
    private HomePageFragment hp;
    private HomePageFragment hpOther;
    private Context mContext;
    private static final String TAG = "HomePageActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getAndroidContext();
        setContentView(R.layout.activity_main);
        HomePageUtils.i(TAG, "onCreate yuzm");
        try {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != context) {
                mContext = context;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (savedInstanceState == null) {
            hp = new HomePageFragment(mContext);
            hp.setArguments(getIntent().getExtras());
            fm = getSupportFragmentManager();
            ftx = fm.beginTransaction();
            ftx.add(R.id.container, hp);
            ftx.addToBackStack(null);
            ftx.setTransition(R.id.container);
            ftx.commit();
        }
    }
    //Add by zhimin.yu
    public Activity getAndroidContext() {
        HomePageUtils.i(TAG, "getAndroidContext yuzm this : " + this);
        return this;
    }
    public void SetFragment(){

    }
    @Override
    public void onStart(){
        super.onStart();
        HomePageUtils.i(TAG, "onStart yuzm");
    }
    @Override
    public void onPause(){
        super.onPause();
        HomePageUtils.i(TAG, "onPause yuzm");
    }
    @Override
    public void onResume(){
        super.onResume();
        HomePageUtils.i(TAG, "onResume yuzm");
    }
    @Override
    public void onResumeFragments(){
        super.onResumeFragments();
        HomePageUtils.i(TAG, "onResume yuzm");
    }
    @Override
    public void onStop(){
        super.onStop();
        HomePageUtils.i(TAG, "onStop yuzm");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        HomePageUtils.i(TAG, "onDestroy yuzm");
    }
    @Override
    public void onRestart(){
        super.onRestart();
        HomePageUtils.i(TAG, "onRestart yuzm");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //@Override
    public void onToOtherFragment() {
        hpOther = new HomePageFragment(getAndroidContext());
        fm = getSupportFragmentManager();
        ftx = fm.beginTransaction();
        ftx.hide(hp);
        ftx.add(R.id.container, hpOther);
        ftx.commit();
    }
}
