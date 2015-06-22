package com.kapps.market.ui.manage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.TabListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kapps.market.R;
import com.kapps.market.TabableActivity;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.ui.CommonView;
import com.kapps.market.util.Constants;

/**
 * 2010-6-11 market 锟斤拷锟斤拷锟斤拷
 * 
 * @author admin
 * 
 */
@SuppressLint("NewApi")
public class AppManageActivity extends TabableActivity  {
	public static final String TAG = "AppManageActivity";

	public static final int SOFT_MANANGE_VIEW = 0;
	public static final int TASK_LIST_VIEW = 1;
	public static final int FAVOR_APP_VIEW = 2;
	
	//view pager
	private ViewPager mViewPager;
	public int VIEW_COUNT = 0;	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appmanage_activity);

		initPaper();
		registerView(SOFT_MANANGE_VIEW);
		registerView(TASK_LIST_VIEW);
		registerView(FAVOR_APP_VIEW);
	}

	@Override
	protected void initCustomActionbar() {
		ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle(R.string.download_manage);
//        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.installed_software)/*home*/
                .setTabListener(mBarTabListener)
                .setTag(VIEW_COUNT));
        VIEW_COUNT++;
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.download_task)
                .setTabListener(mBarTabListener)
                .setTag(VIEW_COUNT)); 
        VIEW_COUNT++;
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.my_favoliten)/*digg*/
                .setTabListener(mBarTabListener)
                .setTag(VIEW_COUNT));
        VIEW_COUNT++;
	}
	

	@Override
	protected int getShowViewMark(View trigger) {
		return 0;
	}

    @Override
    public void subHandleMessage(Message msg) {
        LogUtil.d(TAG, "AppMgrActivity:subHandleMsg: " + msg);

        switch (msg.what) {
            case Constants.M_UNINSTALL_APK:
            case Constants.M_INSTALL_APK:
            case Constants.M_DOWNLOAD_PROGRESS:
            case Constants.M_DOWNLOAD_CANCEL:
            case Constants.M_DOWNLOAD_COMPLETED:
            case Constants.M_DOWNLOAD_FAIL:
            case Constants.M_DOWNLOAD_STOP:
            case Constants.M_QUICK_DOWNLOAD_APP:
            case Constants.M_QUICK_PAYMENT_APP:
            case Constants.M_DOWNLOAD_RETRY:
            case Constants.M_BATCH_DOWNLOAD_APP_OK:
                int currentMark = getCurrentTabMark();
                CommonView viewPage = (CommonView) getViewByMarkFromCache(currentMark);
                if (viewPage != null) {
                    viewPage.flushView(msg.what);
                }
                break;
        }
    }

	@Override
	protected View createContentView(int viewMark) {
		switch (viewMark) {
		case SOFT_MANANGE_VIEW:
			InstalledAppList listView = new InstalledAppList(this);
			// 锟斤拷锟斤拷锟斤拷锟斤拷斜锟斤拷锟揭拷锟斤拷锟饺★拷谋锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷母锟斤拷锟�
			listView.initLoadleList(marketContext.getTaskMarkPool()
					.getInitSoftwareSummaryTaskMark(), true, true, true);
			return listView;

		case TASK_LIST_VIEW:
			return new TaskManageView(this);

		case FAVOR_APP_VIEW:
			AppFavorView favorApk = new AppFavorView(this);
			// 锟斤拷锟斤拷锟斤拷锟斤拷斜锟斤拷锟揭拷锟斤拷锟饺★拷谋锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟酵拷锟斤拷锟�
			ATaskMark favorTaskMark = taskMarkPool.getAppFavorTaskMark();
			favorApk.initLoadleList(favorTaskMark, true, true, true);
			return favorApk;

		default:
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);    
    }

	//for view pager
	private void initPaper() {
		mViewPager =(ViewPager) findViewById(R.id.pager);  
		
        mViewPager.setAdapter(new MyViewPagerAdapter());  
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	
	private final TabListener mBarTabListener = new ActionBar.TabListener(){  
        private final static String TAG = "MyBarTabListener";  
          
        @Override  
        public void onTabReselected(ActionBar.Tab arg0, FragmentTransaction arg1) {  
        }
        
        @Override  
        public void onTabSelected(ActionBar.Tab arg0, FragmentTransaction arg1) {  
            //当更改ActionBar上的Tab页有更改时，ViewPager显示相关的页面

            if (mViewPager != null) {
                mViewPager.setCurrentItem(arg0.getPosition());
            }

            if(arg0.getPosition() == FAVOR_APP_VIEW) {
	            if (!marketContext.isSessionLocalValid()) {
					marketContext.handleSessionTimeOut(true);
					Toast.makeText(AppManageActivity.this,
							getResources().getString(
									R.string.use_personal_center_must_login), 500)
							.show();
				}
            }
        }
        
        @Override  
        public void onTabUnselected(ActionBar.Tab arg0, FragmentTransaction arg1) {  
        }
    };

	//page adapter
	public class MyViewPagerAdapter extends PagerAdapter{
          
        public MyViewPagerAdapter() {
        }
  
        @Override
        public void destroyItem(ViewGroup container, int position, Object object)   {     
            container.removeView(getViewByMarkFromCache(position));//mListViews.get(position));  
        }
  
        @Override  
        public Object instantiateItem(ViewGroup container, int position) {
             View view = getViewByMarkFromCache(position);//every instantiateItem must invoke showChooseView..
             container.addView(view, 0);  //TODO correct??
             return view;  
        }
  
        @Override  
        public int getCount() {
            return  VIEW_COUNT;
        }

        @Override  
        public boolean isViewFromObject(View arg0, Object arg1) {             
            return arg0==arg1;  
        }
    }

	public class MyOnPageChangeListener implements OnPageChangeListener{
        @Override
        public void onPageScrollStateChanged(int state) {  }
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {  }
        @Override
        public void onPageSelected(int position) {
        	ActionBar actionBar = getSupportActionBar();
        	actionBar.setSelectedNavigationItem(position);
            //do showChoosedView will make mCurrentTab correct
            showChoosedView(position);
            //Toast.makeText(MarketMainFrame.this, "您选择了"+ mViewPager.getCurrentItem()+"页卡", Toast.LENGTH_SHORT).show();  
        }
    }

}
