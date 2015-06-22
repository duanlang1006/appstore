package com.kapps.market.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.kapps.market.AppDetailFrame;
import com.kapps.market.MApplication;
import com.kapps.market.R;
import com.kapps.market.TaskMarkPool;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.MImageType;
import com.kapps.market.cache.AppCahceManager;
import com.kapps.market.cache.AssertCacheManager;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.MarketServiceWraper;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppAdvertiseTaskMark;
import com.kapps.market.task.mark.AppImageTaskMark;
import com.kapps.market.task.mark.MultipleTaskMark;
import com.kapps.market.util.Constants;

import java.util.ArrayList;

/**
 * 图片轮播
 *
 * @author shuizhu
 *
 */
public class ConverFlowBase extends FrameLayout implements IResultReceiver {

    public static final String TAG = "ConverFlowBase";
    protected static final int CONVERFLOW_APP = 81;
    protected static final int CONVERFLOW_GAME = 90;
    protected int mType = CONVERFLOW_APP;
    private ArrayList<RangeInfo> rList = new ArrayList<RangeInfo>();

    private ViewPager mViewpager = null;                //广告位
    private ImageView mDotImages[] = null;

    private Drawable emptyDrawable;

    // 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
    protected AppCahceManager appCahceManager;
    protected AssertCacheManager assertCacheManager;
    protected TaskMarkPool taskMarkPool;
    protected MarketServiceWraper serviceWraper;
    protected AppAdvertiseTaskMark taskMark;
    protected volatile boolean inInit;

    // 应锟斤拷循锟斤拷指锟斤拷
    private int appRIndex = 0;

    // 锟斤拷示锟斤拷一锟斤拷
    public static final int NEXT_SHOT_DELAY = 3000;

    private static final int MAX_ADV = 5;
    private int mAdvCount = 0;//current/
    private int SHOW_NEXT_SLOT_MSG = 11;
    private int INIT_MSG = 10;
    private NextSlotHandle nextSlotHandle;

    private boolean hasStop = false;
    private PagerAdapter mPageAdapter;
    private LayoutParams mLayoutParams;
    private LinearLayout mDotViewGroupDots;

    public ConverFlowBase(Context context, AttributeSet attrSet) {
        super(context, attrSet);

        // 应锟斤拷锟斤拷锟�
        MApplication mApplication = MApplication.getInstance();
        emptyDrawable = mApplication.emptyADAppIcon;
        appCahceManager = mApplication.getAppCahceManager();
        assertCacheManager = mApplication.getAssertCacheManager();
        taskMarkPool = mApplication.getTaskMarkPool();
        serviceWraper = mApplication.getServiceWraper();

        initAdType();

//        if (appCahceManager.getAppItemCount(taskMark) == 0) {
//            PageInfo pageInfo = taskMark.getPageInfo();
//            if (pageInfo != null) {
//                serviceWraper.getAppAdvertiseByType(this, taskMark,
//                        taskMark.getPopType(), pageInfo.getNextPageIndex(),
//                        pageInfo.getPageSize());
//                serviceWraper.forceTakeoverTask(this, taskMark);
//            }
//            else {
//                Log.e("temp", "loadAdv of app, pageInfo==null");
//            }
//        }
//        else {
//            Log.e("temp", "loadAdv of app, appItemCount > 0");
//        }

        // 锟皆讹拷锟斤拷锟斤拷
        nextSlotHandle = new NextSlotHandle();
        nextSlotHandle.sendEmptyMessage(INIT_MSG);
    }

    protected void initAdType() {
        //
    }

    public void refresh() {
        final int newAdvCount = appCahceManager.getAppItemCount(taskMark);
        Log.d("temp", "refresh: mAdvcount= "+mAdvCount+". newAdvCount="+newAdvCount);
        if (mAdvCount == 0 || rList.size() == 0) {
            nextSlotHandle.sendEmptyMessage(INIT_MSG);
        }
    }

    // 锟斤拷始锟斤拷片锟斤拷息
    protected void initPieceInfo(int w, int h) {
        Log.d("temp", "initPieceInfo-->in");
        // Log.d(TAG, "init piece info w: " + w + " h:" + h);
        if (w == 0 || h == 0) {
            return;
        }
        inInit = true;

        // 确锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷幕锟斤拷锟斤拷锟斤拷锟�
        mAdvCount = appCahceManager.getAppItemCount(taskMark);
        Log.d("temp", "initPieceInfo-->in. mAdvCount="+mAdvCount);

        rList.clear();

        // 确锟斤拷锟杰癸拷要锟斤拷片
        RangeInfo rangeInfo = null;
        if (mLayoutParams == null) {
            mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        //TODO, how to define 5?
        for (int index = 0; index < mAdvCount; index++) {
            AppItem appItem = appCahceManager.getAppItemByMarkIndex(taskMark, index);

            if (appItem.getCategoryId() != mType) {
                Log.d("temp", "type != null!!.categid=" + appItem.getCategoryId());
                continue;
            }

            rangeInfo = new RangeInfo(index);
            rangeInfo.appItem = appItem;
            ImageView image = new ImageView(getContext());
            image.setScaleType(ScaleType.FIT_XY);//fill view
            image.setLayoutParams(mLayoutParams);
            image.setImageDrawable(emptyDrawable);
            image.setTag(rangeInfo);
            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    RangeInfo r = (RangeInfo) v.getTag();
                    handleShowAppDetail(r.appItem);
                }
            });
            rangeInfo.drawable = null;
            rangeInfo.view = image;

//            updateRangeAppInfo(index);

            rList.add(rangeInfo);
            if (rList.size() >= MAX_ADV) break;
        }
        Log.d("temp", "init rangeInfo. rlist.size="+rList.size());//type != null!!.categid="+appItem.getCategoryId());

        //init for show
        initViewPagerAndDots();

        if (mPageAdapter != null) {
            mPageAdapter.notifyDataSetChanged();
        }

        // 锟斤拷始锟斤拷锟斤拷锟斤拷图片
        for (int i=0; i < rList.size(); i++) {
            updateRangeAppInfo(rList.get(i));
        }

        // 锟斤拷始锟斤拷锟斤拷锟斤拷锟斤拷
        // Log.d("initPieceInfo: ", "image task size: " +
        // mTaskMark.getTaskMarkList().size());
        MultipleTaskMark mTaskMark = new MultipleTaskMark();
        if (mTaskMark.getTaskMarkList().size() != 0) {
//            Log.d("tem")
            serviceWraper.scheduleAppImageResourceTask(this, mTaskMark, null);
        }

        inInit = false;
    }

    @Override
    public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
        if (taskMark instanceof AppImageTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
            Log.d("temp", "converflow: type="+mType+". imagetask: rlist="+rList.size());
            for (RangeInfo rInfo : rList) {
                // 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟姐，锟斤拷锟斤拷锟斤拷芏锟斤拷rinfo锟斤拷示同一锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷斜锟斤拷锟斤拷锟斤拷锟斤拷锟叫★拷
                if (rInfo.appItem != null && rInfo.appItem.getId() == ((AppImageTaskMark) taskMark).getId()) {
                    Drawable d = assertCacheManager.getAdvertiseIconFromCache(rInfo.appItem.getIconId());
                    if (d != null) {
                        rInfo.view.setImageDrawable(d);
                        rInfo.updated = true;
                        rInfo.view.postInvalidate();
                    }
                    break;
                }
            }
        } else if (taskMark instanceof AppAdvertiseTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
            Log.d("temp", "converflow: type="+mType+",.will do init.");
            nextSlotHandle.sendEmptyMessage(INIT_MSG);
        }
    }

    // 锟斤拷锟斤拷图片
    private void updateRangeAppInfo(RangeInfo rangeInfo) {
//        RangeInfo rangeInfo = rList.get(index);
//        if (rangeInfo == null) return;

        if (!rangeInfo.updated) {
            Log.d("temp", "updateRangeAppInfo::image didn't updated");
//            AppItem appItem = appCahceManager.getAppItemByMarkIndex(taskMark, index);
//            if (appItem == null) return;
//            if (appItem.getCategoryId() != mType) {
//                return;
//            }
//            Log.d("temp", "updateRangeAppInfo::appItem not null");
//            rangeInfo.appItem = appItem;
            Drawable d = assertCacheManager.getAdvertiseIconFromCache(rangeInfo.appItem.getIconId());
            if (d != null) {
                Log.d("temp", "updateRangeAppInfo::d != null");
                rangeInfo.updated = true;
                rangeInfo.view.setImageDrawable(d);
                rangeInfo.view.postInvalidate();
            } else {
                Log.d("temp", "updateRangeAppInfo:: d == null, get from net");
                AppImageTaskMark imageTaskMark = taskMarkPool.createAppImageTaskMark(rangeInfo.appItem.getId(),
                        rangeInfo.appItem.getAdIcon(), MImageType.APP_ADVERTISE_ICON);
                serviceWraper.getAppImageResource(this, imageTaskMark, null, imageTaskMark.getId(),
                        imageTaskMark.getUrl(), imageTaskMark.getType());
            }
        }
    }


    // 锟斤拷示锟斤拷锟斤拷锟斤拷锟较�
    public void handleShowAppDetail(AppItem appItem) {
        if (appItem != null) {
            Intent intent = new Intent(getContext(), AppDetailFrame.class);
            intent.putExtra(Constants.APP_ID, appItem.getId());
            ((Activity) getContext()).startActivityForResult(intent, Constants.ACTIVITY_RCODE_APPDETAIL);
        }
    }

    private void initDot() {
        if (mDotViewGroupDots == null) {
            mDotViewGroupDots = (LinearLayout) findViewById(R.id.viewGroupDots);
        }
        if (mDotViewGroupDots.getChildCount() == rList.size()) return;

        mDotViewGroupDots.removeAllViews();//prevent reinit.

        mDotImages = new ImageView[rList.size()];

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                18, 18);
        layoutParams.setMargins(4, 3, 4, 3);

        for (int i = 0; i < rList.size(); i++) {
            ImageView dot = new ImageView(getContext());

            dot.setLayoutParams(layoutParams);
            mDotImages[i] = dot;
            mDotImages[i].setTag(i);
            mDotImages[i].setOnClickListener(onClickDots);

            if (i == 0) {
                mDotImages[i].setBackgroundResource(R.drawable.dot_selected);
            } else {
                mDotImages[i].setBackgroundResource(R.drawable.dot_unselected);
            }

            mDotViewGroupDots.addView(mDotImages[i]);
        }
    }

    private void initViewPagerAndDots() {
        if (mViewpager == null) {
            mViewpager = (ViewPager) findViewById(R.id.viewpager);	//广告位
            mPageAdapter = new MyPagerAdapter();
            mViewpager.setAdapter(mPageAdapter);
            mViewpager.setOnPageChangeListener(pageChangeListener);
        }

        initDot();
    }

    OnClickListener onClickDots = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();
            if (position < 0 || position >= rList.size()) {
                return;
            }
            mViewpager.setCurrentItem(position, true);
            mDotImages[position].setBackgroundResource(R.drawable.dot_selected);
        }
    };

    class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return rList.size();
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView(rList.get(position).view);
        }

        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewPager) container).addView(rList.get(position).view, 0);
            return rList.get(position).view;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    };

    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < mDotImages.length; i++) {
                mDotImages[position].setBackgroundResource(R.drawable.dot_selected);
                if (position != i) {
                    mDotImages[i].setBackgroundResource(R.drawable.dot_unselected);
                }
            }

            updateRangeAppInfo(rList.get(position));
        }
    };

    // 锟斤拷锟斤拷图片锟斤拷锟斤拷锟斤拷锟斤拷息
    private class RangeInfo {
        public boolean updated = false;

        public Drawable drawable = null;

        private int index;

        // 锟斤拷前应锟斤拷要锟斤拷示锟斤拷图片
        public ImageView view = null;
        // 锟斤拷前锟斤拷应锟斤拷应锟斤拷id
        public AppItem appItem;

        public RangeInfo(int index) {
            this.index = index;
        }
    }

    // 锟斤拷一锟斤拷
    private class NextSlotHandle extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == INIT_MSG) {
                removeMessages(msg.what);
                removeMessages(SHOW_NEXT_SLOT_MSG);
                initPieceInfo(1, 1);
                sendEmptyMessageDelayed(SHOW_NEXT_SLOT_MSG, NEXT_SHOT_DELAY);
            }
            else
            if (msg.what == SHOW_NEXT_SLOT_MSG) {
                removeMessages(msg.what);

                if (!hasStop){
                    if (mViewpager != null) {
                        if (rList.size() > 0) {
                            appRIndex = (appRIndex + 1) % rList.size();
                            mViewpager.setCurrentItem(appRIndex);
                        }
                        else {
//                            refresh();
                        }
                    }

                    sendEmptyMessageDelayed(SHOW_NEXT_SLOT_MSG, NEXT_SHOT_DELAY);
                }
            }
        }
    }

}
